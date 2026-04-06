import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:islami_admin/features/home/domain/entities/home_stats.dart';

abstract class HomeRemoteDataSource {
  Future<HomeStats> getHomeStats();
}

class HomeRemoteDataSourceImpl implements HomeRemoteDataSource {
  final FirebaseFirestore firestore;

  HomeRemoteDataSourceImpl({required this.firestore});

  @override
  Future<HomeStats> getHomeStats() async {
    try {
      // Run in parallel: previously these were sequential, so latency summed
      // (e.g. 4 × 300ms ≈ 1.2s). Now wall time is roughly one round-trip.
      final results = await Future.wait([
        firestore.collection('users').count().get(),
        firestore.collection('hadiths').count().get(),
        firestore
            .collection('root')
            .doc('feedback')
            .collection('messages')
            .count()
            .get(),
        firestore.collection('config').doc('app').get(),
      ]);

      final usersSnapshot = results[0] as AggregateQuerySnapshot;
      final hadithSnapshot = results[1] as AggregateQuerySnapshot;
      final feedbackSnapshot = results[2] as AggregateQuerySnapshot;
      final appConfigSnapshot = results[3] as DocumentSnapshot<Map<String, dynamic>>;

      final totalUsers = usersSnapshot.count ?? 0;
      final totalHadith = hadithSnapshot.count ?? 0;
      final totalFeedback = feedbackSnapshot.count ?? 0;

      final appConfig = appConfigSnapshot.data();
      final appVersion = (appConfig?['version'] as String?)?.trim();

      final feedbackRatePerThousandUsers = totalUsers == 0
          ? 0.0
          : (totalFeedback / totalUsers) * 1000;

      return HomeStats(
        totalUsers: totalUsers,
        totalFeedback: totalFeedback,
        totalHadith: totalHadith,
        quranSurahs: 114,
        quranAyahs: 6236,
        feedbackRatePerThousandUsers: feedbackRatePerThousandUsers,
        appVersion: appVersion != null && appVersion.isNotEmpty
            ? appVersion
            : 'unknown',
      );
    } catch (e) {
      throw Exception('Failed to fetch home stats: $e');
    }
  }
}
