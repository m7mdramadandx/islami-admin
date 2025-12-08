
import 'package:islami_admin/features/azkar/domain/repositories/azkar_repository.dart';

class SaveAzkar {
  final AzkarRepository repository;

  SaveAzkar(this.repository);

  Future<void> call(String content) async {
    await repository.saveAzkar(content);
  }
}
