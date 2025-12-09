
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:islami_admin/features/duas/domain/usecases/get_duas.dart';
import 'package:islami_admin/features/duas/domain/usecases/save_duas.dart';
import 'package:islami_admin/features/duas/presentation/bloc/duas_event.dart';
import 'package:islami_admin/features/duas/presentation/bloc/duas_state.dart';

class DuasBloc extends Bloc<DuasEvent, DuasState> {
  final GetDuas getDuas;
  final SaveDuas saveDuas;

  DuasBloc({required this.getDuas, required this.saveDuas}) : super(DuasInitial()) {
    on<FetchDuas>((event, emit) async {
      emit(DuasLoading());
      try {
        final content = await getDuas();
        emit(DuasLoaded(content));
      } catch (e) {
        emit(DuasError(e.toString()));
      }
    });

    on<SaveDuasEvent>((event, emit) async {
      emit(DuasSaving());
      try {
        await saveDuas(event.content);
        emit(DuasSaved());
      } catch (e) {
        emit(DuasError(e.toString()));
      }
    });
  }
}
