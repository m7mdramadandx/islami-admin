import 'dart:async';

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:islami_admin/features/auth/presentation/pages/login_page.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_bloc.dart';
import 'package:islami_admin/features/azkar/presentation/pages/azkar_page.dart';
import 'package:islami_admin/features/hadith/presentation/pages/hadith_management_page.dart';
import 'package:islami_admin/features/home/presentation/pages/home_page.dart';
import 'package:islami_admin/features/notification/presentation/pages/notification_management_page.dart';
import 'package:islami_admin/features/quran/presentation/pages/quran_management_page.dart';
import 'package:islami_admin/features/user/presentation/pages/user_management_page.dart';
import 'package:islami_admin/injection_container.dart' as di;

import 'firebase_options.dart';

void main() async {
  await runZonedGuarded(() async {
    WidgetsFlutterBinding.ensureInitialized();
    await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform,
    );
    di.init();

    FlutterError.onError = FirebaseCrashlytics.instance.recordFlutterError;

    runApp(const IslamiAdmin());
  }, (error, stack) => FirebaseCrashlytics.instance.recordError(error, stack));
}

class IslamiAdmin extends StatelessWidget {
  const IslamiAdmin({super.key});

  @override
  Widget build(BuildContext context) {
    final ColorScheme colorScheme = ColorScheme(
      brightness: Brightness.light,
      primary: AppColors.colorPrimary,
      onPrimary: AppColors.whiteSolid,
      secondary: AppColors.colorSecondary,
      onSecondary: AppColors.whiteSolid,
      error: AppColors.failureRed,
      onError: AppColors.whiteSolid,
      surface: AppColors.colorBackground,
      onSurface: AppColors.text,
    );

    return MultiBlocProvider(
      providers: [BlocProvider(create: (_) => di.sl<AuthBloc>())],
      child: MaterialApp.router(
        routerConfig: _router,
        title: 'islami-admin',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          useMaterial3: true,
          colorScheme: colorScheme,
          visualDensity: VisualDensity.adaptivePlatformDensity,
          textTheme: GoogleFonts.cairoTextTheme(Theme.of(context).textTheme),
          elevatedButtonTheme: ElevatedButtonThemeData(
            style: ElevatedButton.styleFrom(
              minimumSize: const Size(double.infinity, 50),
              backgroundColor: colorScheme.primary,
              foregroundColor: colorScheme.onPrimary,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              elevation: 5,
              shadowColor: colorScheme.primary.withAlpha(102),
            ),
          ),
          inputDecorationTheme: InputDecorationTheme(
            filled: true,
            fillColor: AppColors.natural100,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide.none,
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide.none,
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: colorScheme.primary),
            ),
          ),
          iconTheme: IconThemeData(color: colorScheme.primary, size: 24.0),
          progressIndicatorTheme: ProgressIndicatorThemeData(
            color: colorScheme.primary,
          ),
          switchTheme: SwitchThemeData(
            thumbColor: WidgetStateProperty.resolveWith((states) {
              if (states.contains(WidgetState.selected)) {
                return colorScheme.primary;
              }
              return AppColors.natural500;
            }),
            trackColor: WidgetStateProperty.resolveWith((states) {
              if (states.contains(WidgetState.selected)) {
                return colorScheme.primary.withAlpha(128);
              }
              return AppColors.natural200;
            }),
          ),
          dropdownMenuTheme: DropdownMenuThemeData(
            inputDecorationTheme: InputDecorationTheme(
              filled: true,
              fillColor: AppColors.natural100,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide.none,
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide.none,
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(color: colorScheme.primary),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

final _router = GoRouter(
  routes: [
    GoRoute(path: '/', builder: (context, state) => const LoginPage()),
    GoRoute(path: '/home', builder: (context, state) => const HomePage()),
    GoRoute(
      path: '/hadith-management',
      builder: (context, state) => const HadithManagementPage(),
    ),
    GoRoute(
      path: '/user-management',
      builder: (context, state) => const UserManagementPage(),
    ),
    GoRoute(
      path: '/quran-management',
      builder: (context, state) => const QuranManagementPage(),
    ),
    GoRoute(
      path: '/notification-management',
      builder: (context, state) => const NotificationManagementPage(),
    ),
    GoRoute(
      path: '/azkar-management',
      builder: (context, state) => BlocProvider(
        create: (_) => di.sl<AzkarBloc>(),
        child: const AzkarPage(),
      ),
    ),
  ],
);
