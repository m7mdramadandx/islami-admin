
import 'package:get_it/get_it.dart';
import 'package:islami_admin/features/duas/data/datasources/duas_remote_data_source.dart';
import 'package:islami_admin/features/duas/data/repositories/duas_repository_impl.dart';
import 'package:islami_admin/features/duas/domain/repositories/duas_repository.dart';
import 'package:islami_admin/features/duas/domain/usecases/get_duas.dart';
import 'package:islami_admin/features/duas/domain/usecases/save_duas.dart';
import 'package:islami_admin/features/duas/presentation/bloc/duas_bloc.dart';
import 'package:firebase_storage/firebase_storage.dart';

final sl = GetIt.instance;

void initDuasFeature() {
  // Bloc
  sl.registerFactory(() => DuasBloc(getDuas: sl(), saveDuas: sl()));

  // Usecases
  sl.registerLazySingleton(() => GetDuas(sl()));
  sl.registerLazySingleton(() => SaveDuas(sl()));

  // Repository
  sl.registerLazySingleton<DuasRepository>(
      () => DuasRepositoryImpl(remoteDataSource: sl()));

  // Datasources
  sl.registerLazySingleton<DuasRemoteDataSource>(
      () => DuasRemoteDataSourceImpl(sl()));

  // External
  sl.registerLazySingleton(() => FirebaseStorage.instance);
}
