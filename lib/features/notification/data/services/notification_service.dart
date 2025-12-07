import 'dart:async';
import 'dart:developer' as developer;
import 'package:cloud_functions/cloud_functions.dart';

class NotificationService {
  /// Sends a notification by invoking a secure Cloud Function.
  ///
  /// This is the recommended approach for sending FCM notifications.
  /// The app calls a backend function, which then securely sends the notification.
  /// This avoids exposing the FCM server key on the client.
  static Future<void> sendNotification({
    required String title,
    required String body,
    String? imageUrl,
    String? topic,
  }) async {
    try {
      // Get a reference to the callable Cloud Function.
      final HttpsCallable callable = FirebaseFunctions.instance.httpsCallable(
        'sendFcmNotification',
      );

      // Prepare the data to be sent to the function.
      final Map<String, dynamic> data = {
        'title': title,
        'body': body,
        if (imageUrl != null && imageUrl.isNotEmpty) 'imageUrl': imageUrl,
        if (topic != null && topic.isNotEmpty) 'topic': topic,
      };

      developer.log('Calling Cloud Function with data: $data');

      // Invoke the function and wait for the result.
      final result = await callable.call(data);

      developer.log('Cloud Function result: ${result.data}');

      if (result.data['success'] != true) {
        throw Exception(
            'Cloud Function returned an error: ${result.data['error']}');
      }
    } on FirebaseFunctionsException catch (e) {
      developer.log(
        'Error calling Cloud Function: ${e.code} - ${e.message}',
        error: e,
      );
      throw Exception('Failed to send notification: ${e.message}');
    } catch (e) {
      developer.log('An unexpected error occurred: $e', error: e);
      throw Exception('An unexpected error occurred while sending notification.');
    }
  }
}
