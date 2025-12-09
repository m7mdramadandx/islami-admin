
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:islami_admin/features/auth/presentation/pages/login_page.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_bloc.dart';
import 'package:islami_admin/features/azkar/presentation/pages/azkar_page.dart';
import 'package:islami_admin/features/duas/presentation/bloc/duas_bloc.dart';
import 'package:islami_admin/features/duas/presentation/pages/duas_page.dart';
import 'package:islami_admin/features/hadith/presentation/pages/hadith_management_page.dart';
import 'package:islami_admin/features/home/presentation/pages/home_page.dart';
import 'package:islami_admin/features/notification/presentation/pages/notification_management_page.dart';
import 'package:islami_admin/features/quran/presentation/pages/quran_management_page.dart';
import 'package:islami_admin/features/user/presentation/pages/user_management_page.dart';
import 'package:islami_admin/injection_container.dart' as di;

final router = GoRouter(
  routes: [
    GoRoute(
      path: '/',
      builder: (context, state) => const LoginPage(),
    ),
    GoRoute(
      path: '/home',
      builder: (context, state) => const HomePage(),
    ),
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
      path: '/azkar',
      builder: (context, state) => BlocProvider(
        create: (_) => di.sl<AzkarBloc>(),
        child: const AzkarPage(),
      ),
    ),
    GoRoute(
      path: '/duas',
      builder: (context, state) => BlocProvider(
        create: (_) => di.sl<DuasBloc>(),
        child: const DuasPage(),
      ),
    ),
  ],
);
