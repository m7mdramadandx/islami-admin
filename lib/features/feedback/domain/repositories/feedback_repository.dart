import 'package:dartz/dartz.dart';
import 'package:islami_admin/core/errors/failures.dart';
import 'package:islami_admin/features/feedback/domain/entities/feedback_message.dart';

abstract class FeedbackRepository {
  Future<Either<Failure, List<FeedbackMessage>>> getFeedbackMessages();
}
