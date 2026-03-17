import 'package:dartz/dartz.dart';
import 'package:islami_admin/core/errors/failures.dart';
import 'package:islami_admin/features/feedback/data/datasources/feedback_remote_data_source.dart';
import 'package:islami_admin/features/feedback/domain/entities/feedback_message.dart';
import 'package:islami_admin/features/feedback/domain/repositories/feedback_repository.dart';

class FeedbackRepositoryImpl implements FeedbackRepository {
  final FeedbackRemoteDataSource remoteDataSource;

  FeedbackRepositoryImpl({required this.remoteDataSource});

  @override
  Future<Either<Failure, List<FeedbackMessage>>> getFeedbackMessages() async {
    try {
      final messages = await remoteDataSource.getFeedbackMessages();
      return Right(messages);
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }
}
