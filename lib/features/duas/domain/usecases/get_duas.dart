
import 'package:islami_admin/features/duas/domain/repositories/duas_repository.dart';

class GetDuas {
  final DuasRepository repository;

  GetDuas(this.repository);

  Future<String> call() {
    return repository.getDuas();
  }
}
