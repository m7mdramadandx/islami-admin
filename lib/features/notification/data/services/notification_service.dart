import 'dart:developer' as developer;

import 'package:cloud_functions/cloud_functions.dart';

class NotificationService {
  /// Sends a notification by invoking a secure Cloud Function.
  ///
  /// Uses top-level key [payload] (not `body`) so the HTTPS callable transport
  /// does not confuse it with HTTP body handling. [payload] includes both
  /// `title` and `body` (message text). FCM [data] still exposes a JSON string
  /// under key `body` for background parsing.
  static Future<void> sendNotification({
    required String title,
    required String body,
    String? imageUrl,
    String? topic,
    String? fcmToken,
    int? routeID,
    int? extraRouteID,
    String? minVersion,
  }) async {
    try {
      final HttpsCallable callable = FirebaseFunctions.instance.httpsCallable(
        'sendFcmNotification',
      );

      final Map<String, dynamic> payload = {
        'title': title,
        'body': body,
        if (imageUrl != null && imageUrl.isNotEmpty) 'imageUrl': imageUrl,
        if (topic != null && topic.isNotEmpty) 'topic': topic,
        if (fcmToken != null && fcmToken.isNotEmpty) 'fcmToken': fcmToken,
        if (routeID != null) 'routeID': routeID,
        if (extraRouteID != null) 'extraRouteID': extraRouteID,
        if (minVersion != null && minVersion.isNotEmpty) 'minVersion': minVersion,
      };

      developer.log('Calling Cloud Function with payload: $payload');

      final result = await callable.call(<String, dynamic>{
        'payload': payload,
      });

      developer.log('Cloud Function result: ${result.data}');

      if (result.data['success'] != true) {
        throw Exception(
          'Cloud Function returned an error: ${result.data['error']}',
        );
      }
      developer.log(
        'Notification sent mode: ${result.data['mode'] ?? 'unknown'} '
        'messageId: ${result.data['messageId'] ?? 'n/a'}',
      );
    } on FirebaseFunctionsException catch (e) {
      developer.log(
        'Error calling Cloud Function: ${e.code} - ${e.message}',
        error: e,
      );
      throw Exception('Failed to send notification: ${e.message}');
    } catch (e) {
      developer.log('An unexpected error occurred: $e', error: e);
      throw Exception(
        'An unexpected error occurred while sending notification.',
      );
    }
  }
}
