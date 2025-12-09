
import 'package:equatable/equatable.dart';

abstract class DuasEvent extends Equatable {
  const DuasEvent();

  @override
  List<Object> get props => [];
}

class FetchDuas extends DuasEvent {}

class SaveDuasEvent extends DuasEvent {
  final String content;

  const SaveDuasEvent(this.content);

  @override
  List<Object> get props => [content];
}
