const functions = require("firebase-functions");
const admin = require("firebase-admin");
const {BigQuery} = require("@google-cloud/bigquery");

admin.initializeApp();

const FCM_PERMISSION = "cloudmessaging.messages.create";

/**
 * A callable Cloud Function to send FCM notifications.
 */
exports.sendFcmNotification = functions.https.onCall(async (data, context) => {
  // 1. Validate the request data
  const {
    title,
    body,
    imageUrl,
    topic,
    fcmToken,
    routeType,
    routeID,
    extraRouteID,
    minVersion,
  } = data;
  if (!title || !body) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        'The function must be called with "title" and "body".',
    );
  }

  // 2. Resolve notification target
  const directToken = typeof fcmToken === "string" ? fcmToken.trim() : "";
  const targetTopic = typeof topic === "string" ? topic.trim() : "";

  let targetTokens = [];
  if (directToken.length > 0) {
    targetTokens = [directToken];
  }
  if (targetTokens.length === 0 && targetTopic.length === 0) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        "Provide either fcmToken or topic.",
    );
  }

  // 3. Construct the FCM message payload
  const dataPayload = {
    payloadType: "data_only_v2",
    title: String(title ?? ""),
    body: String(body ?? ""),
    imageUrl: String(imageUrl ?? ""),
    routeType: routeType == null ? "" : String(routeType),
    routeID: routeID == null ? "0" : String(routeID),
    extraRouteID: extraRouteID == null ? "0" : String(extraRouteID),
    minVersion: minVersion == null ? "" : String(minVersion),
  };

  // FCM HTTP v1 often rejects messages with no `notification` (e.g. topic sends).
  // Keep full `data` for your Android handler; `notification` satisfies the API + tray.
  const notificationBlock = {
    title: String(title ?? ""),
    body: String(body ?? ""),
  };

  const message = {
    data: dataPayload,
    notification: notificationBlock,
    android: {
      priority: "high",
      ...(imageUrl
        ? {notification: {image: String(imageUrl)}}
        : {}),
    },
    apns: {
      headers: {
        "apns-priority": "10",
      },
      payload: {
        aps: {
          "content-available": 1,
        },
      },
      ...(imageUrl ? {fcmOptions: {imageUrl: String(imageUrl)}} : {}),
    },
    ...(targetTokens.length === 0 ? {topic: targetTopic} : {}),
  };

  functions.logger.info("Attempting to send FCM message", {
    mode: targetTokens.length > 0 ? "direct" : "topic",
    topic: targetTokens.length > 0 ? null : targetTopic,
    tokenCount: targetTokens.length,
    hasImage: Boolean(imageUrl),
  });

  // 4. Send the message
  try {
    if (targetTokens.length === 1) {
      const response = await admin.messaging().send({
        ...message,
        token: targetTokens[0],
      });
      functions.logger.info("Successfully sent direct token message:", response);
      return {success: true, mode: "token", messageId: response};
    }

    const response = await admin.messaging().send(message);
    functions.logger.info("Successfully sent topic message:", response);
    return {success: true, mode: "topic", messageId: response};
  } catch (error) {
    functions.logger.error("FCM Send Error:", error);

    const errorMessage = error.message || "Unknown FCM error";

    throw new functions.https.HttpsError(
        "internal",
        `FCM Error: ${errorMessage}`,
        {
          code: error.code,
          details: error.message,
        }
    );
  }
});

/**
 * Returns dashboard analytics metrics from GA4 BigQuery export.
 *
 * Required setup:
 * 1) Enable Firebase Analytics -> BigQuery export.
 * 2) Store dataset config in Firestore:
 *    - config/analytics:
 *      - bigQueryDatasetId: "analytics_123456789"
 *      - bigQueryLocation: "US" (optional)
 */
exports.getAnalyticsStats = functions.https.onCall(async () => {
  try {
    const projectId = process.env.GCLOUD_PROJECT || process.env.GCP_PROJECT;
    const analyticsConfigDoc = await admin
        .firestore()
        .collection("config")
        .doc("analytics")
        .get();
    const analyticsConfig = analyticsConfigDoc.data() || {};
    const datasetId = analyticsConfig.bigQueryDatasetId || process.env.ANALYTICS_BQ_DATASET;
    const location = analyticsConfig.bigQueryLocation || "US";

    if (!projectId || !datasetId) {
      return {
        configured: false,
        reason: "Missing projectId or bigQueryDatasetId",
        dau: 0,
        wau: 0,
        mau: 0,
        totalEvents30d: 0,
        notificationSent30d: 0,
        notificationOpened30d: 0,
        feedbackSubmitted30d: 0,
        contentViewed30d: 0,
      };
    }

    const bigquery = new BigQuery({projectId});
    const query = `
      WITH base AS (
        SELECT
          event_name,
          user_pseudo_id,
          PARSE_DATE('%Y%m%d', event_date) AS event_dt
        FROM \`${projectId}.${datasetId}.events_*\`
        WHERE _TABLE_SUFFIX BETWEEN
          FORMAT_DATE('%Y%m%d', DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY))
          AND FORMAT_DATE('%Y%m%d', CURRENT_DATE())
      )
      SELECT
        COUNT(DISTINCT IF(event_dt = CURRENT_DATE(), user_pseudo_id, NULL)) AS dau,
        COUNT(DISTINCT IF(event_dt >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY), user_pseudo_id, NULL)) AS wau,
        COUNT(DISTINCT user_pseudo_id) AS mau,
        COUNT(1) AS totalEvents30d,
        COUNTIF(event_name = 'notification_sent') AS notificationSent30d,
        COUNTIF(event_name = 'notification_open') AS notificationOpened30d,
        COUNTIF(event_name = 'feedback_submitted') AS feedbackSubmitted30d,
        COUNTIF(event_name IN ('content_view', 'screen_view')) AS contentViewed30d
      FROM base
    `;

    const [rows] = await bigquery.query({query, location});
    const row = rows[0] || {};

    const toInt = (value) => Number.parseInt(value || 0, 10) || 0;
    return {
      configured: true,
      projectId,
      datasetId,
      dau: toInt(row.dau),
      wau: toInt(row.wau),
      mau: toInt(row.mau),
      totalEvents30d: toInt(row.totalEvents30d),
      notificationSent30d: toInt(row.notificationSent30d),
      notificationOpened30d: toInt(row.notificationOpened30d),
      feedbackSubmitted30d: toInt(row.feedbackSubmitted30d),
      contentViewed30d: toInt(row.contentViewed30d),
    };
  } catch (error) {
    functions.logger.error("Analytics query failed", {
      message: error.message,
      code: error.code,
      stack: error.stack,
    });

    throw new functions.https.HttpsError(
        "internal",
        `Analytics query failed: ${error.message}`,
    );
  }
});
