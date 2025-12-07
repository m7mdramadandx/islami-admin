
import 'package:dartz/dartz.dart';
import 'package:firebase_auth/firebase_auth.dart' as firebase;
import 'package:islami_admin/core/errors/failures.dart';
import 'package:islami_admin/features/auth/data/data_sources/auth_remote_data_source.dart';
import 'package:islami_admin/features/auth/data/models/user_model.dart';
import 'package:islami_admin/features/auth/domain/entities/user_entity.dart';
import 'package:islami_admin/features/auth/domain/repositories/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  final AuthRemoteDataSource remoteDataSource;

  AuthRepositoryImpl({required this.remoteDataSource});

  @override
  Stream<UserEntity?> get authStateChanges {
    return remoteDataSource.authStateChanges.map((firebaseUser) {
      return firebaseUser != null ? UserModel.fromFirebaseUser(firebaseUser) : null;
    });
  }

  @override
  Future<Either<Failure, UserEntity>> login(String email, String password) async {
    try {
      final userCredential = await remoteDataSource.login(email, password);
      final user = userCredential.user!;
      return Right(UserModel.fromFirebaseUser(user));
    } on firebase.FirebaseAuthException catch (e) {
      return Left(ServerFailure(e.message ?? 'An unknown error occurred.'));
    }
  }

  @override
  Future<Either<Failure, void>> logout() async {
    try {
      await remoteDataSource.logout();
      return const Right(null);
    } catch (_) {
      return Left(ServerFailure('Logout failed.'));
    }
  }
}
