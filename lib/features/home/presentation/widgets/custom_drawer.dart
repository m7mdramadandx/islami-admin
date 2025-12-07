import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:islami_admin/features/auth/presentation/bloc/auth_bloc.dart';

class CustomDrawer extends StatelessWidget {
  const CustomDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    return Drawer(
      child: Column(
        children: [
          _buildDrawerHeader(context),
          _buildDrawerItem(
            icon: Icons.dashboard,
            text: 'Dashboard',
            onTap: () => Navigator.of(context).pop(),
          ),
          _buildDrawerItem(
            icon: Icons.people,
            text: 'Users',
            onTap: () {
              GoRouter.of(context).go('/user-management');
            },
          ),
          _buildDrawerItem(
            icon: Icons.notifications,
            text: 'Notifications',
            onTap: () {
              GoRouter.of(context).go('/notification-management');
            },
          ),
          const Divider(),
          _buildDrawerItem(
            icon: Icons.logout,
            text: 'Logout',
            onTap: () {
              context.read<AuthBloc>().add(LogoutEvent());
            },
          ),
        ],
      ),
    );
  }

  Widget _buildDrawerHeader(BuildContext context) {
    return DrawerHeader(
      decoration: BoxDecoration(color: Theme.of(context).colorScheme.primary),
      child: Row(
        children: [
          Icon(
            Icons.mosque,
            size: 40,
            color: Theme.of(context).colorScheme.onPrimary,
          ),
          const SizedBox(width: 16),
          Text(
            'Islami Admin',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Theme.of(context).colorScheme.onPrimary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDrawerItem({
    required IconData icon,
    required String text,
    required GestureTapCallback onTap,
  }) {
    return ListTile(leading: Icon(icon), title: Text(text), onTap: onTap);
  }
}
