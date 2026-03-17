import 'package:equatable/equatable.dart';
import 'package:islami_admin/features/feedback/domain/entities/feedback_message.dart';

abstract class FeedbackState extends Equatable {
  const FeedbackState();

  @override
  List<Object> get props => [];
}

class FeedbackInitial extends FeedbackState {}

class FeedbackLoading extends FeedbackState {}

class FeedbackLoaded extends FeedbackState {
  final List<FeedbackMessage> messages;

  const FeedbackLoaded({required this.messages});

  @override
  List<Object> get props => [messages];
}

class FeedbackError extends FeedbackState {
  final String message;

  const FeedbackError({required this.message});

  @override
  List<Object> get props => [message];
}
