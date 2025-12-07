import 'package:islami_admin/core/usecases/usecase.dart';
import 'package:islami_admin/features/auth/domain/entities/user_entity.dart';
import 'package:islami_admin/features/auth/domain/repositories/auth_repository.dart';

class GetAuthStateChanges {
  final AuthRepository repository;

  GetAuthStateChanges(this.repository);

  Stream<UserEntity?> call(NoParams params) {
    return repository.authStateChanges;
  }
}
