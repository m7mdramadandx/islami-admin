const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize the Firebase Admin SDK.
admin.initializeApp();

/**
 * A callable Cloud Function to send FCM notifications.
 */
exports.sendFcmNotification = functions.https.onCall(async (data, context) => {
  // 1. Validate the request data
  const {title, body, imageUrl, topic} = data;
  if (!title || !body) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        'The function must be called with arguments "title" and "body".',
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
    // Add image to notification if provided
    ...(imageUrl && {android: {notification: {imageUrl: imageUrl}}}),
    ...(imageUrl && {
      apns: {payload: {aps: {"mutable-content": 1}, fcm_options: {image: imageUrl}}},
    }),
    topic: targetTopic,
  };

  functions.logger.info("Attempting to send FCM message to topic:", targetTopic);

  // 4. Send the message using the Admin SDK
  try {
    const response = await admin.messaging().send(message);
    functions.logger.info("Successfully sent message:", response);
    return {success: true, messageId: response};
  } catch (error) {
    // Log the full error object for debugging in Firebase Console
    functions.logger.error("FCM Send Error Details:", {
      message: error.message,
      code: error.code,
      stack: error.stack,
    });

    // Throw a more descriptive error to the client
    throw new functions.https.HttpsError(
        "internal",
        `FCM Error: [${error.code}] ${error.message}`,
        {details: error.message}
    );
  }
});
