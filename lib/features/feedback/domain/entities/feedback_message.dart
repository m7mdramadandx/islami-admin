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
