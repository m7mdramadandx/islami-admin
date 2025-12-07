import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:islami_admin/core/usecases/usecase.dart';
import 'package:islami_admin/features/auth/domain/entities/user_entity.dart';
import 'package:islami_admin/features/auth/domain/usecases/get_auth_state_changes.dart';
import 'package:islami_admin/features/auth/domain/usecases/login.dart';
import 'package:islami_admin/features/auth/domain/usecases/logout.dart';

part 'auth_event.dart';
part 'auth_state.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final Login loginUseCase;
  final Logout logoutUseCase;
  final GetAuthStateChanges getAuthStateChangesUseCase;

  late final StreamSubscription<UserEntity?> _userSubscription;

  AuthBloc({
    required this.loginUseCase,
    required this.logoutUseCase,
    required this.getAuthStateChangesUseCase,
  }) : super(AuthInitial()) {
    on<LoginEvent>(_onLoginEvent);
    on<LogoutEvent>(_onLogoutEvent);
    on<AuthStatusChanged>(_onAuthStatusChanged);

    _userSubscription = getAuthStateChangesUseCase(
      NoParams(),
    ).listen((user) => add(AuthStatusChanged(user)));
  }

  Future<void> _onLoginEvent(LoginEvent event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    final failureOrUser = await loginUseCase(
      LoginParams(email: event.email, password: event.password),
    );
    failureOrUser.fold(
      (failure) => emit(AuthError(failure.message)),
      (user) => emit(AuthAuthenticated(user)),
    );
  }

  Future<void> _onLogoutEvent(
    LogoutEvent event,
    Emitter<AuthState> emit,
  ) async {
    await logoutUseCase(NoParams());
    emit(AuthUnauthenticated());
  }

  void _onAuthStatusChanged(AuthStatusChanged event, Emitter<AuthState> emit) {
    if (event.user != null) {
      emit(AuthAuthenticated(event.user!));
    } else {
      emit(AuthUnauthenticated());
    }
  }

  @override
  Future<void> close() {
    _userSubscription.cancel();
    return super.close();
  }
}
