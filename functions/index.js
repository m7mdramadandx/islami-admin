const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

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
