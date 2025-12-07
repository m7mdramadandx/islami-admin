
import 'package:dartz/dartz.dart';
import 'package:islami_admin/core/errors/failures.dart';
import 'package:islami_admin/core/usecases/usecase.dart';
import 'package:islami_admin/features/auth/domain/repositories/auth_repository.dart';

class Logout implements UseCase<void, NoParams> {
  final AuthRepository repository;

  Logout(this.repository);

  @override
  Future<Either<Failure, void>> call(NoParams params) async {
    return await repository.logout();
  }
}
