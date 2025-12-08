
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:islami_admin/features/azkar/domain/usecases/get_azkar.dart';
import 'package:islami_admin/features/azkar/domain/usecases/save_azkar.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_event.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_state.dart';

class AzkarBloc extends Bloc<AzkarEvent, AzkarState> {
  final GetAzkar getAzkar;
  final SaveAzkar saveAzkar;

  AzkarBloc({required this.getAzkar, required this.saveAzkar}) : super(AzkarInitial()) {
    on<FetchAzkar>((event, emit) async {
      emit(AzkarLoading());
      try {
        final content = await getAzkar();
        emit(AzkarLoaded(content));
      } catch (e) {
        emit(AzkarError(e.toString()));
      }
    });

    on<SaveAzkarEvent>((event, emit) async {
      emit(AzkarSaving());
      try {
        await saveAzkar(event.content);
        emit(AzkarSaved());
      } catch (e) {
        emit(AzkarError(e.toString()));
      }
    });
  }
}
