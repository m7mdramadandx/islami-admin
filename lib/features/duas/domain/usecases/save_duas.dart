
import 'package:islami_admin/features/duas/domain/repositories/duas_repository.dart';

class SaveDuas {
  final DuasRepository repository;

  SaveDuas(this.repository);

  Future<void> call(String content) {
    return repository.saveDuas(content);
  }
}
