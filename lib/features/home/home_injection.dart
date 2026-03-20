import 'package:islami_admin/features/home/data/datasources/home_remote_data_source.dart';
import 'package:islami_admin/features/home/data/repositories/home_repository_impl.dart';
import 'package:islami_admin/features/home/domain/repositories/home_repository.dart';
import 'package:islami_admin/features/home/domain/usecases/get_home_stats.dart';
import 'package:islami_admin/features/home/presentation/bloc/home_bloc.dart';
import 'package:islami_admin/injection_container.dart';

void initHomeFeature() {
  // Bloc
  sl.registerFactory(() => HomeBloc(getHomeStats: sl()));

  // Use cases
  sl.registerLazySingleton(() => GetHomeStats(sl()));

  // Repositories
  sl.registerLazySingleton<HomeRepository>(
    () => HomeRepositoryImpl(remoteDataSource: sl()),
  );

  // Data Sources
  sl.registerLazySingleton<HomeRemoteDataSource>(
    () => HomeRemoteDataSourceImpl(firestore: sl()),
  );
}
