import 'dart:async';

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:islami_admin/core/routing/app_router.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:islami_admin/injection_container.dart' as di;

import 'firebase_options.dart';

void main() async {
  runZonedGuarded<Future<void>>(() async {
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
    final ColorScheme colorScheme = ColorScheme.fromSeed(
      seedColor: AppColors.colorPrimary,
      primary: AppColors.colorPrimary,
      secondary: AppColors.colorAccent,
      surface: Colors.white,
      background: AppColors.colorBackground,
      error: AppColors.failureRed,
      brightness: Brightness.light,
    );

    return MultiBlocProvider(
      providers: [BlocProvider(create: (_) => di.sl<AuthBloc>())],
      child: MaterialApp.router(
        routerConfig: AppRouter.router,
        title: 'Islami Admin',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          useMaterial3: true,
          colorScheme: colorScheme,
          scaffoldBackgroundColor: colorScheme.background,
          textTheme: GoogleFonts.plusJakartaSansTextTheme(
            Theme.of(context).textTheme,
          ),
          appBarTheme: AppBarTheme(
            backgroundColor: Colors.white,
            foregroundColor: AppColors.colorPrimary,
            elevation: 0,
            centerTitle: false,
            titleTextStyle: GoogleFonts.plusJakartaSans(
              color: AppColors.colorPrimary,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
            iconTheme: const IconThemeData(color: AppColors.colorPrimary),
          ),
          cardTheme: CardThemeData(
            color: Colors.white,
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
              side: BorderSide(color: Colors.grey.shade200),
            ),
          ),
          drawerTheme: const DrawerThemeData(
            backgroundColor: Colors.white,
            elevation: 0,
          ),
          elevatedButtonTheme: ElevatedButtonThemeData(
            style: ElevatedButton.styleFrom(
              elevation: 0,
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              backgroundColor: AppColors.colorPrimary,
              foregroundColor: Colors.white,
              textStyle: GoogleFonts.plusJakartaSans(
                fontWeight: FontWeight.w600,
                fontSize: 16,
              ),
            ),
          ),
          inputDecorationTheme: InputDecorationTheme(
            filled: true,
            fillColor: Colors.grey.shade50,
            contentPadding: const EdgeInsets.symmetric(
              horizontal: 16,
              vertical: 16,
            ),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.grey.shade200),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.grey.shade200),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: AppColors.colorPrimary,
                width: 2,
              ),
            ),
            labelStyle: GoogleFonts.plusJakartaSans(
              color: Colors.grey.shade600,
            ),
            hintStyle: GoogleFonts.plusJakartaSans(color: Colors.grey.shade400),
          ),
        ),
      ),
    );
  }
}
