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
  const {title, body, imageUrl, topic} = data;
  if (!title || !body) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        'The function must be called with "title" and "body".',
    );
  }

  // 2. Define the notification target
  const targetTopic = (topic && topic.trim().length > 0) ? topic : "all_users";

  // 3. Construct the FCM message payload
  const message = {
    notification: {
      title: title,
      body: body,
    },
    android: {
      notification: {
        // Correct field name for Android is 'image'
        ...(imageUrl && {image: imageUrl}),
      },
    },
    apns: {
      payload: {
        aps: {
          "mutable-content": 1,
        },
        fcm_options: {
          image: imageUrl,
        },
      },
    },
    topic: targetTopic,
  };

  functions.logger.info("Attempting to send FCM message to topic:", targetTopic);

  // 4. Send the message
  try {
    const response = await admin.messaging().send(message);
    functions.logger.info("Successfully sent message:", response);
    return {success: true, messageId: response};
  } catch (error) {
    functions.logger.error("FCM Send Error:", error);

    // Provide a more helpful error message to the client
    let errorMessage = error.message || "Unknown FCM error";
    if (error.code === "messaging/permission-denied") {
      errorMessage = "Permission denied. Ensure the Firebase Cloud Messaging API is enabled and the service account has 'Firebase Cloud Messaging API Admin' role.";
    }

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
