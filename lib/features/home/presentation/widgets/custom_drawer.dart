import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:islami_admin/core/theme/theme_controller.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/auth/presentation/bloc/auth_bloc.dart';

class CustomDrawer extends StatelessWidget {
  const CustomDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    final currentRoute = GoRouterState.of(context).uri.path;

    return Drawer(
      backgroundColor: Theme.of(context).drawerTheme.backgroundColor,
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
                Padding(
                  padding: const EdgeInsets.fromLTRB(12, 10, 12, 2),
                  child: ListTile(
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10),
                    ),
                    leading: Icon(
                      ThemeController.isDark
                          ? Icons.light_mode_rounded
                          : Icons.dark_mode_rounded,
                      color: AppColors.colorPrimary,
                    ),
                    title: Text(
                      ThemeController.isDark
                          ? 'Switch to Light'
                          : 'Switch to Dark',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: AppColors.text,
                      ),
                    ),
                    onTap: () {
                      Navigator.of(context).pop();
                      // Defer theme rebuild: toggling during the same pointer/mouse
                      // update phase triggers a known web assertion in MouseTracker.
                      WidgetsBinding.instance.addPostFrameCallback((_) {
                        ThemeController.toggleTheme();
                      });
                    },
                  ),
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
              color: AppColors.whiteSolid.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              Icons.mosque_rounded,
              color: AppColors.whiteSolid,
              size: 32,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'Islami Admin',
            style: TextStyle(
              color: AppColors.whiteSolid,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          Text(
            'Control Panel',
            style: TextStyle(
              color: AppColors.whiteSolid.withValues(alpha: 0.6),
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
        style: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.bold,
          color: AppColors.grey,
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
        selectedTileColor:
            AppColors.colorPrimary.withValues(alpha: 0.05),
        leading: Icon(
          icon,
          color: isSelected ? AppColors.colorPrimary : AppColors.subText,
          size: 22,
        ),
        title: Text(
          text,
          style: TextStyle(
            fontSize: 14,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
            color: isSelected ? AppColors.colorPrimary : AppColors.text,
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
        leading: Icon(
          Icons.logout_rounded,
          color: AppColors.failureRed,
          size: 22,
        ),
        title: Text(
          'Logout',
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: AppColors.failureRed,
          ),
        ),
      ),
    );
  }
}
