import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:islami_admin/features/home/domain/usecases/get_home_stats.dart';
import 'package:islami_admin/features/home/presentation/bloc/home_event.dart';
import 'package:islami_admin/features/home/presentation/bloc/home_state.dart';

class HomeBloc extends Bloc<HomeEvent, HomeState> {
  final GetHomeStats getHomeStats;

  HomeBloc({required this.getHomeStats}) : super(HomeInitial()) {
    on<GetHomeStatsEvent>((event, emit) async {
      emit(HomeLoading());
      try {
        final stats = await getHomeStats();
        emit(HomeLoaded(stats));
      } catch (e) {
        emit(HomeError(e.toString()));
      }
    });
  }
}
