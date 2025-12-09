
import 'package:firebase_auth/firebase_auth.dart';
import 'package:get_it/get_it.dart';
import 'package:islami_admin/features/auth/data/data_sources/auth_remote_data_source.dart';
import 'package:islami_admin/features/auth/data/repositories/auth_repository_impl.dart';
import 'package:islami_admin/features/auth/domain/repositories/auth_repository.dart';
import 'package:islami_admin/features/auth/domain/usecases/get_auth_state_changes.dart';
import 'package:islami_admin/features/auth/domain/usecases/login.dart';
import 'package:islami_admin/features/auth/domain/usecases/logout.dart';
import 'package:islami_admin/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:islami_admin/features/azkar/azkar_injection.dart';
import 'package:islami_admin/features/duas/duas_injection.dart';

final sl = GetIt.instance;

void init() {
  // Features
  initAuthFeature();
  initAzkarFeature();
  initDuasFeature();
}

void initAuthFeature() {
  // Blocs
  sl.registerFactory(
    () => AuthBloc(
      loginUseCase: sl(),
      logoutUseCase: sl(),
      getAuthStateChangesUseCase: sl(),
    ),
  );

  // Use cases
  sl.registerLazySingleton(() => Login(sl()));
  sl.registerLazySingleton(() => Logout(sl()));
  sl.registerLazySingleton(() => GetAuthStateChanges(sl()));

  // Repositories
  sl.registerLazySingleton<AuthRepository>(
    () => AuthRepositoryImpl(remoteDataSource: sl()),
  );

  // Data Sources
  sl.registerLazySingleton<AuthRemoteDataSource>(
    () => AuthRemoteDataSourceImpl(firebaseAuth: sl()),
  );

  // External
  sl.registerLazySingleton(() => FirebaseAuth.instance);
}
