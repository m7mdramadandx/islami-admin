import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/auth/presentation/bloc/auth_bloc.dart';

class CustomDrawer extends StatelessWidget {
  const CustomDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    final currentRoute = GoRouterState.of(context).uri.path;

    return Drawer(
      backgroundColor: Colors.white,
      child: Column(
        children: [
          _buildHeader(context),
          Expanded(
            child: ListView(
              padding: const EdgeInsets.symmetric(vertical: 8),
              children: [
                _buildSectionTitle('MAIN MENU'),
                _buildDrawerItem(
                  context,
                  icon: Icons.dashboard_rounded,
                  text: 'Dashboard',
                  route: '/home',
                  isSelected: currentRoute == '/home',
                ),
                _buildDrawerItem(
                  context,
                  icon: Icons.people_rounded,
                  text: 'Users',
                  route: '/user-management',
                  isSelected: currentRoute == '/user-management',
                ),
                _buildSectionTitle('CONTENT'),
                _buildDrawerItem(
                  context,
                  icon: Icons.menu_book_rounded,
                  text: 'Quran',
                  route: '/quran-management',
                  isSelected: currentRoute == '/quran-management',
                ),
                _buildDrawerItem(
                  context,
                  icon: Icons.format_quote_rounded,
                  text: 'Hadith',
                  route: '/hadith-management',
                  isSelected: currentRoute == '/hadith-management',
                ),
                _buildDrawerItem(
                  context,
                  icon: Icons.spa_rounded,
                  text: 'Azkar',
                  route: '/azkar',
                  isSelected: currentRoute == '/azkar',
                ),
                _buildDrawerItem(
                  context,
                  icon: Icons.volunteer_activism_rounded,
                  text: 'Duas',
                  route: '/duas',
                  isSelected: currentRoute == '/duas',
                ),
                _buildSectionTitle('COMMUNICATION'),
                _buildDrawerItem(
                  context,
                  icon: Icons.notifications_active_rounded,
                  text: 'Notifications',
                  route: '/notification-management',
                  isSelected: currentRoute == '/notification-management',
                ),
                _buildDrawerItem(
                  context,
                  icon: Icons.feedback_rounded,
                  text: 'Feedback',
                  route: '/feedback',
                  isSelected: currentRoute == '/feedback',
                ),
              ],
            ),
          ),
          const Divider(height: 1),
          _buildLogoutItem(context),
        ],
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(24, 60, 24, 24),
      color: AppColors.colorPrimary,
      width: double.infinity,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Icon(
              Icons.mosque_rounded,
              color: Colors.white,
              size: 32,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'Islami Admin',
            style: GoogleFonts.plusJakartaSans(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          Text(
            'Control Panel',
            style: GoogleFonts.plusJakartaSans(
              color: Colors.white.withOpacity(0.6),
              fontSize: 13,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(24, 20, 24, 8),
      child: Text(
        title,
        style: GoogleFonts.plusJakartaSans(
          fontSize: 11,
          fontWeight: FontWeight.bold,
          color: Colors.grey.shade400,
          letterSpacing: 1.2,
        ),
      ),
    );
  }

  Widget _buildDrawerItem(
    BuildContext context, {
    required IconData icon,
    required String text,
    required String route,
    required bool isSelected,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 2),
      child: ListTile(
        onTap: () => context.go(route),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        selected: isSelected,
        selectedTileColor: AppColors.colorPrimary.withOpacity(0.05),
        leading: Icon(
          icon,
          color: isSelected ? AppColors.colorPrimary : Colors.grey.shade600,
          size: 22,
        ),
        title: Text(
          text,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
            color: isSelected ? AppColors.colorPrimary : Colors.grey.shade700,
          ),
        ),
      ),
    );
  }

  Widget _buildLogoutItem(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(12.0),
      child: ListTile(
        onTap: () {
          context.read<AuthBloc>().add(LogoutEvent());
          context.go('/');
        },
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        leading: const Icon(
          Icons.logout_rounded,
          color: Colors.redAccent,
          size: 22,
        ),
        title: Text(
          'Logout',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: Colors.redAccent,
          ),
        ),
      ),
    );
  }
}
