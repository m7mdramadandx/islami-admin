
import 'package:equatable/equatable.dart';

abstract class AzkarEvent extends Equatable {
  const AzkarEvent();

  @override
  List<Object> get props => [];
}

class FetchAzkar extends AzkarEvent {}

class SaveAzkarEvent extends AzkarEvent {
  final String content;

  const SaveAzkarEvent(this.content);

  @override
  List<Object> get props => [content];
}
