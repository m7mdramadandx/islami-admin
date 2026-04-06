import 'package:equatable/equatable.dart';

class HomeStats extends Equatable {
  final int totalUsers;
  final int totalFeedback;
  final int totalHadith;
  final int quranSurahs;
  final int quranAyahs;
  final double feedbackRatePerThousandUsers;
  final String appVersion;

  const HomeStats({
    required this.totalUsers,
    required this.totalFeedback,
    required this.totalHadith,
    required this.quranSurahs,
    required this.quranAyahs,
    required this.feedbackRatePerThousandUsers,
    required this.appVersion,
  });

  @override
  List<Object?> get props => [
    totalUsers,
    totalFeedback,
    totalHadith,
    quranSurahs,
    quranAyahs,
    feedbackRatePerThousandUsers,
    appVersion,
  ];
}
