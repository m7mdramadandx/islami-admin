
import 'package:equatable/equatable.dart';

abstract class DuasState extends Equatable {
  const DuasState();

  @override
  List<Object> get props => [];
}

class DuasInitial extends DuasState {}

class DuasLoading extends DuasState {}

class DuasLoaded extends DuasState {
  final String content;

  const DuasLoaded(this.content);

  @override
  List<Object> get props => [content];
}

class DuasSaving extends DuasState {}

class DuasSaved extends DuasState {}

class DuasError extends DuasState {
  final String message;

  const DuasError(this.message);

  @override
  List<Object> get props => [message];
}
