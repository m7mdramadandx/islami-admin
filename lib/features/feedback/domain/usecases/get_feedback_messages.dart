import 'package:dartz/dartz.dart';
import 'package:islami_admin/core/errors/failures.dart';
import 'package:islami_admin/features/feedback/domain/entities/feedback_message.dart';
import 'package:islami_admin/features/feedback/domain/repositories/feedback_repository.dart';

class GetFeedbackMessages {
  final FeedbackRepository repository;

  GetFeedbackMessages(this.repository);

  Future<Either<Failure, List<FeedbackMessage>>> call() async {
    return await repository.getFeedbackMessages();
  }
}
