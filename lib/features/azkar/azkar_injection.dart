
import 'package:get_it/get_it.dart';
import 'package:islami_admin/features/azkar/data/datasources/azkar_remote_data_source.dart';
import 'package:islami_admin/features/azkar/data/repositories/azkar_repository_impl.dart';
import 'package:islami_admin/features/azkar/domain/repositories/azkar_repository.dart';
import 'package:islami_admin/features/azkar/domain/usecases/get_azkar.dart';
import 'package:islami_admin/features/azkar/domain/usecases/save_azkar.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_bloc.dart';
import 'package:firebase_storage/firebase_storage.dart';

final sl = GetIt.instance;

void initAzkarFeature() {
  // Bloc
  sl.registerFactory(() => AzkarBloc(getAzkar: sl(), saveAzkar: sl()));

  // Usecases
  sl.registerLazySingleton(() => GetAzkar(sl()));
  sl.registerLazySingleton(() => SaveAzkar(sl()));

  // Repository
  sl.registerLazySingleton<AzkarRepository>(
      () => AzkarRepositoryImpl(remoteDataSource: sl()));

  // Datasources
  sl.registerLazySingleton<AzkarRemoteDataSource>(
      () => AzkarRemoteDataSourceImpl(sl()));

  // External
  sl.registerLazySingleton(() => FirebaseStorage.instance);
}
