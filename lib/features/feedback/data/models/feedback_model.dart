import 'package:islami_admin/features/feedback/domain/entities/feedback_message.dart';

class FeedbackModel extends FeedbackMessage {
  const FeedbackModel({
    required super.id,
    required super.appVersion,
    required super.deviceName,
    required super.email,
    required super.phone,
    required super.date,
    required super.msg,
  });

  factory FeedbackModel.fromFirestore(Map<String, dynamic> json, String id) {
    return FeedbackModel(
      id: id,
      appVersion: json['app_version'] ?? '',
      deviceName: json['deviceName'] ?? json['device_name'] ?? 'Unknown',
      email: json['email'] ?? 'No Email',
      phone: json['phone'] ?? 'No Phone',
      date: json['date'] ?? '',
      msg: json['msg'] ?? '',
    );
  }
}
