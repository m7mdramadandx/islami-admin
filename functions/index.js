const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Callable: sendFcmNotification
 *
 * Client must send (after SDK unwrap), one of:
 * - { payload: { title, body, topic?, fcmToken?, ... } }  ← admin app (recommended)
 * - { body: { title, body, ... } }                         ← legacy
 * - { title, body, ... }                                   ← flat legacy
 *
 * `title` and `body` (message text) are required strings inside that object.
 */
function normalizeCallableData(data) {
  if (data == null || typeof data !== "object") return null;
  if (data.payload != null && typeof data.payload === "object" &&
      !Array.isArray(data.payload)) {
    return data.payload;
  }
  if (data.body != null && typeof data.body === "object" &&
      !Array.isArray(data.body)) {
    return data.body;
  }
  if (data.data != null && typeof data.data === "object") {
    const inner = data.data;
    if (inner.payload != null && typeof inner.payload === "object") {
      return inner.payload;
    }
    if (inner.body != null && typeof inner.body === "object") {
      return inner.body;
    }
    return inner;
  }
  return data;
}

/**
 * A callable Cloud Function to send FCM notifications.
 */
exports.sendFcmNotification = functions.https.onCall(async (data, context) => {
  const input = normalizeCallableData(data);
  if (!input || typeof input !== "object") {
    throw new functions.https.HttpsError(
        "invalid-argument",
        "Invalid request: expected an object with payload (title, body, topic or fcmToken).",
    );
  }

  // 1. Validate the request data
  const title = String(input.title ?? "").trim();
  const bodyText = String(input.body ?? "").trim();
  const {
    imageUrl,
    topic,
    fcmToken,
    routeType,
    routeID,
    extraRouteID,
    minVersion,
  } = input;

  if (!title || !bodyText) {
    functions.logger.warn("sendFcmNotification missing title/body", {
      keys: Object.keys(input),
      hasTitle: Boolean(input.title),
      hasBody: Boolean(input.body),
    });
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

  // 3. FCM data: single JSON object under key "body" (string) for background handlers.
  // Android: parse remoteMessage.data["body"] then jsonDecode → title, body, routeID, ...
  const backgroundBundle = {
    title,
    body: bodyText,
    imageUrl: String(imageUrl ?? ""),
    routeType: routeType == null ? "" : String(routeType),
    routeID: routeID == null ? "0" : String(routeID),
    extraRouteID: extraRouteID == null ? "0" : String(extraRouteID),
    minVersion: minVersion == null ? "" : String(minVersion),
  };
  const dataPayload = {
    payloadType: "background_bundle_v1",
    body: JSON.stringify(backgroundBundle),
  };

  // FCM HTTP v1 often rejects messages with no `notification` (e.g. topic sends).
  // Keep full `data` for your Android handler; `notification` satisfies the API + tray.
  const notificationBlock = {
    title,
    body: bodyText,
  };

  const message = {
    data: backgroundBundle,
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
