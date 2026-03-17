import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:islami_admin/features/feedback/data/datasources/feedback_remote_data_source.dart';
import 'package:islami_admin/features/feedback/data/repositories/feedback_repository_impl.dart';
import 'package:islami_admin/features/feedback/domain/repositories/feedback_repository.dart';
import 'package:islami_admin/features/feedback/domain/usecases/get_feedback_messages.dart';
import 'package:islami_admin/features/feedback/presentation/bloc/feedback_bloc.dart';
import 'package:islami_admin/injection_container.dart';

void initFeedbackFeature() {
  // BLoC
  sl.registerFactory(() => FeedbackBloc(getFeedbackMessages: sl()));

  // Use cases
  sl.registerLazySingleton(() => GetFeedbackMessages(sl()));

  // Repository
  sl.registerLazySingleton<FeedbackRepository>(
    () => FeedbackRepositoryImpl(remoteDataSource: sl()),
  );

  // Data sources
  sl.registerLazySingleton<FeedbackRemoteDataSource>(
    () => FeedbackRemoteDataSourceImpl(firestore: sl()),
  );

  // External
  if (!sl.isRegistered<FirebaseFirestore>()) {
    sl.registerLazySingleton(() => FirebaseFirestore.instance);
  }
}
