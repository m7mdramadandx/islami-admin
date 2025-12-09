
import 'package:islami_admin/features/duas/data/datasources/duas_remote_data_source.dart';
import 'package:islami_admin/features/duas/domain/repositories/duas_repository.dart';

class DuasRepositoryImpl implements DuasRepository {
  final DuasRemoteDataSource remoteDataSource;

  DuasRepositoryImpl({required this.remoteDataSource});

  @override
  Future<String> getDuas() {
    return remoteDataSource.getDuas();
  }

  @override
  Future<void> saveDuas(String content) {
    return remoteDataSource.saveDuas(content);
  }
}
