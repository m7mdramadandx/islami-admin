import 'package:equatable/equatable.dart';

class FeedbackMessage extends Equatable {
  final String id;
  final String appVersion;
  final String deviceName;
  final String email;
  final String phone;
  final String date;
  final String msg;

  const FeedbackMessage({
    required this.id,
    required this.appVersion,
    required this.deviceName,
    required this.email,
    required this.phone,
    required this.date,
    required this.msg,
  });

  factory FeedbackMessage.fromFirestore(Map<String, dynamic> json, String id) {
    return FeedbackMessage(
      id: id,
      appVersion: json['app_version'] ?? '',
      deviceName: json['deviceName'] ?? json['device_name'] ?? 'Unknown',
      email: json['email'] ?? 'No Email',
      phone: json['phone'] ?? 'No Phone',
      date: json['date'] ?? '',
      msg: json['msg'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'app_version': appVersion,
      'deviceName': deviceName,
      'email': email,
      'phone': phone,
      'date': date,
      'msg': msg,
    };
  }

  @override
  List<Object?> get props => [
    id,
    appVersion,
    deviceName,
    email,
    phone,
    date,
    msg,
  ];
}
