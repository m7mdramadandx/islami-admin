import 'package:equatable/equatable.dart';

class HomeStats extends Equatable {
  final int totalUsers;
  final int activeToday;
  final int totalFeedback;
  final int totalHadith;
  final int analyticsDau;
  final int analyticsWau;
  final int analyticsMau;
  final int analyticsTotalEvents30d;
  final int analyticsNotificationSent30d;
  final int analyticsNotificationOpened30d;
  final int analyticsFeedbackSubmitted30d;
  final int analyticsContentViewed30d;
  final int quranSurahs;
  final int quranAyahs;
  final double activeUsersPercentage;
  final double feedbackRatePerThousandUsers;
  final String appVersion;

  const HomeStats({
    required this.totalUsers,
    required this.activeToday,
    required this.totalFeedback,
    required this.totalHadith,
    required this.analyticsDau,
    required this.analyticsWau,
    required this.analyticsMau,
    required this.analyticsTotalEvents30d,
    required this.analyticsNotificationSent30d,
    required this.analyticsNotificationOpened30d,
    required this.analyticsFeedbackSubmitted30d,
    required this.analyticsContentViewed30d,
    required this.quranSurahs,
    required this.quranAyahs,
    required this.activeUsersPercentage,
    required this.feedbackRatePerThousandUsers,
    required this.appVersion,
  });

  @override
  List<Object?> get props => [
    totalUsers,
    activeToday,
    totalFeedback,
    totalHadith,
    analyticsDau,
    analyticsWau,
    analyticsMau,
    analyticsTotalEvents30d,
    analyticsNotificationSent30d,
    analyticsNotificationOpened30d,
    analyticsFeedbackSubmitted30d,
    analyticsContentViewed30d,
    quranSurahs,
    quranAyahs,
    activeUsersPercentage,
    feedbackRatePerThousandUsers,
    appVersion,
  ];
}
