
import 'package:equatable/equatable.dart';

abstract class AzkarState extends Equatable {
  const AzkarState();

  @override
  List<Object> get props => [];
}

class AzkarInitial extends AzkarState {}

class AzkarLoading extends AzkarState {}

class AzkarLoaded extends AzkarState {
  final String content;

  const AzkarLoaded(this.content);

  @override
  List<Object> get props => [content];
}

class AzkarSaving extends AzkarState {}

class AzkarSaved extends AzkarState {}

class AzkarError extends AzkarState {
  final String message;

  const AzkarError(this.message);

  @override
  List<Object> get props => [message];
}
