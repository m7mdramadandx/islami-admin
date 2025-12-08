
import 'package:islami_admin/features/azkar/data/datasources/azkar_remote_data_source.dart';
import 'package:islami_admin/features/azkar/domain/repositories/azkar_repository.dart';

class AzkarRepositoryImpl implements AzkarRepository {
  final AzkarRemoteDataSource remoteDataSource;

  AzkarRepositoryImpl({required this.remoteDataSource});

  @override
  Future<String> getAzkar() async {
    return await remoteDataSource.getAzkar();
  }

  @override
  Future<void> saveAzkar(String content) async {
    await remoteDataSource.saveAzkar(content);
  }
}
