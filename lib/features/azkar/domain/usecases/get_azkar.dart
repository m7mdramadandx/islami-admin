
import 'package:islami_admin/features/azkar/domain/repositories/azkar_repository.dart';

class GetAzkar {
  final AzkarRepository repository;

  GetAzkar(this.repository);

  Future<String> call() async {
    return await repository.getAzkar();
  }
}
