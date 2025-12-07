import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;

// IMPORTANT: This is for demonstration purposes only.
// In a production app, you should use a backend service (like Cloud Functions)
// to send notifications. Exposing your server key in a client app is a security risk.
const String _fcmServerKey = 'PASTE_YOUR_FCM_LEGACY_SERVER_KEY_HERE';

class NotificationService {
  static const String _fcmUrl = 'https://fcm.googleapis.com/fcm/send';

  static bool isConfigured() {
    return _fcmServerKey != 'PASTE_YOUR_FCM_LEGACY_SERVER_KEY_HERE';
  }

  static Future<void> sendNotification({
    required String title,
    required String body,
    String? imageUrl,
    String? topic,
  }) async {
    final String target = topic != null && topic.isNotEmpty
        ? '/topics/$topic'
        : '/topics/all_users';

    final Map<String, dynamic> message = {
      'to': target,
      'notification': {
        'title': title,
        'body': body,
        if (imageUrl != null && imageUrl.isNotEmpty) 'image': imageUrl,
      },
      'data': {'click_action': 'FLUTTER_NOTIFICATION_CLICK'},
    };

    final response = await http.post(
      Uri.parse(_fcmUrl),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'key=$_fcmServerKey',
      },
      body: json.encode(message),
    );

    if (response.statusCode != 200) {
      throw Exception('Error sending notification: ${response.body}');
    }
  }
}
