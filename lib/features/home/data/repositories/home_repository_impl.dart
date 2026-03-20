import 'package:islami_admin/features/home/data/datasources/home_remote_data_source.dart';
import 'package:islami_admin/features/home/domain/entities/home_stats.dart';
import 'package:islami_admin/features/home/domain/repositories/home_repository.dart';

class HomeRepositoryImpl implements HomeRepository {
  final HomeRemoteDataSource remoteDataSource;

  HomeRepositoryImpl({required this.remoteDataSource});

  @override
  Future<HomeStats> getHomeStats() {
    return remoteDataSource.getHomeStats();
  }
}
