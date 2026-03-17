import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:islami_admin/features/feedback/domain/usecases/get_feedback_messages.dart';
import 'feedback_event.dart';
import 'feedback_state.dart';

class FeedbackBloc extends Bloc<FeedbackEvent, FeedbackState> {
  final GetFeedbackMessages getFeedbackMessages;

  FeedbackBloc({required this.getFeedbackMessages}) : super(FeedbackInitial()) {
    on<GetFeedbackMessagesEvent>((event, emit) async {
      emit(FeedbackLoading());
      final result = await getFeedbackMessages();
      result.fold(
        (failure) => emit(FeedbackError(message: failure.toString())),
        (messages) => emit(FeedbackLoaded(messages: messages)),
      );
    });
  }
}
