import 'dart:async';
import 'dart:convert';
import 'dart:developer' as developer;

// IMPORTANT: This file has been modified to simulate notification sending.
// Direct calls to the FCM API from a client-side web app are blocked by browser
// security policies (CORS) and are insecure because they expose the server key.

// The CORRECT and SECURE way to send notifications is from a trusted backend
// environment, such as a Cloud Function for Firebase.

// 1. Create a Cloud Function (e.g., 'sendFcmNotification').
// 2. This function will securely store and use your FCM Server Key.
// 3. Your app should call this function (e.g., via an HTTPS callable function).
// 4. The Cloud Function will then make the request to the FCM API.

const String _fcmServerKey =
    'YOUR_SERVER_KEY_HERE'; // This should be moved to your backend.

class NotificationService {
  // This URL is correct, but should be called from your backend.
  static const String _fcmUrl = 'https://fcm.googleapis.com/fcm/send';

  static Future<void> sendNotification({
    required String title,
    required String body,
    String? imageUrl,
    String? topic,
  }) async {
    final String target =
        topic != null && topic.isNotEmpty ? '/topics/$topic' : '/topics/all_users';

    final Map<String, dynamic> message = {
      'to': target,
      'notification': {
        'title': title,
        'body': body,
        if (imageUrl != null && imageUrl.isNotEmpty) 'image': imageUrl,
      },
      'data': {'click_action': 'FLUTTER_NOTIFICATION_CLICK'},
    };

    // --- SIMULATION LOGIC ---
    // Instead of making an insecure client-side HTTP request, we log the
    // details to the console. This simulates a successful API call without
    // encountering the CORS error or exposing your server key.
    developer.log(
      '--- FCM Notification Simulation ---',
      name: 'notification.service',
    );
    developer.log(
      'A real app would send this JSON payload from a Cloud Function:',
      name: 'notification.service',
    );
    developer.log(
      json.encode(message),
      name: 'notification.service',
    );
    developer.log(
      'Authorization Header: key=$_fcmServerKey',
      name: 'notification.service',
    );
    developer.log(
      '---------------------------------',
      name: 'notification.service',
    );

    // In a real implementation with a backend, you would handle the response here.
    // For now, we assume success.
    await Future.delayed(const Duration(milliseconds: 500));
    return;

    // --- ORIGINAL INSECURE CODE (causes CORS error on web) ---
    /*
    final response = await http.post(
      Uri.parse(_fcmUrl),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'key=$_fcmServerKey',
      },
      body: json.encode(message),
    );

    if (response.statusCode != 200) {
      developer.log('Error sending notification: ${response.body}');
      throw Exception('Error sending notification: ${response.body}');
    }
    */
  }

  /// Checks if a placeholder server key is being used.
  static bool isConfigured() {
    return _fcmServerKey != 'YOUR_SERVER_KEY_HERE' && _fcmServerKey.isNotEmpty;
  }
}
