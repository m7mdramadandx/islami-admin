import 'package:dartz/dartz.dart';
import 'package:islami_admin/core/errors/failures.dart';
import 'package:islami_admin/features/auth/domain/entities/user_entity.dart';

abstract class AuthRepository {
  Future<Either<Failure, UserEntity>> login(String email, String password);
  Future<Either<Failure, void>> logout();
  Stream<UserEntity?> get authStateChanges;
}
