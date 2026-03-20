import 'package:islami_admin/features/home/domain/entities/home_stats.dart';

abstract class HomeRepository {
  Future<HomeStats> getHomeStats();
}
