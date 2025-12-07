const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize the Firebase Admin SDK. This is required to send notifications.
admin.initializeApp();

/**
 * A callable Cloud Function to send FCM notifications.
 *
 * This function is invoked from the Flutter app.
 * It constructs and sends a notification using the Firebase Admin SDK.
 * This approach is secure because the FCM Server Key is never exposed to the client.
 *
 * @param {object} data - The data passed from the client app.
 * @param {string} data.title - The title of the notification.
 * @param {string} data.body - The body of the notification.
 * @param {string} [data.imageUrl] - An optional URL for an image in the notification.
 * @param {string} [data.topic] - The topic to send the notification to. Defaults to 'all_users'.
 * @param {functions.https.CallableContext} context - The context of the function call.
 * @returns {Promise<{success: boolean, messageId?: string, error?: string}>} - A promise that resolves with the result.
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
  const targetTopic = topic && topic.isNotEmpty ? topic : "all_users";

  // 3. Construct the FCM message payload
  const message = {
    notification: {
      title: title,
      body: body,
    },
    // Add image to notification if provided
    ...(imageUrl && {android: {notification: {imageUrl: imageUrl}}}),
    // Add APNS (Apple Push Notification Service) specific payload for image
    ...(imageUrl && {
      apns: {payload: {aps: {"mutable-content": 1}, fcm_options: {image: imageUrl}}},
    }),
    topic: targetTopic,
  };

  functions.logger.info("Attempting to send FCM message:", message);

  // 4. Send the message using the Admin SDK
  try {
    const response = await admin.messaging().send(message);
    functions.logger.info("Successfully sent message:", response);
    return {success: true, messageId: response};
  } catch (error) {
    functions.logger.error("Error sending message:", error);
    throw new functions.https.HttpsError(
        "internal",
        "Error sending notification",
        error,
    );
  }
});
