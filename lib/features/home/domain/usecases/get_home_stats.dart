import 'package:islami_admin/features/home/domain/entities/home_stats.dart';
import 'package:islami_admin/features/home/domain/repositories/home_repository.dart';

class GetHomeStats {
  final HomeRepository repository;

  GetHomeStats(this.repository);

  Future<HomeStats> call() async {
    return await repository.getHomeStats();
  }
}
