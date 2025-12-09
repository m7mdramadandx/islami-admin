
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:islami_admin/features/auth/presentation/bloc/auth_bloc.dart';

class CustomDrawer extends StatelessWidget {
  const CustomDrawer({
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return Drawer(
      child: ListView(
        padding: EdgeInsets.zero,
        children: [
          const DrawerHeader(
            decoration: BoxDecoration(
              color: Colors.blue,
            ),
            child: Text(
              'Islami Admin',
              style: TextStyle(
                color: Colors.white,
                fontSize: 24,
              ),
            ),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.home_outlined,
            text: 'Home',
            onTap: () => context.go('/home'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.book_outlined,
            text: 'Quran Management',
            onTap: () => context.go('/quran-management'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.format_quote_outlined,
            text: 'Hadith Management',
            onTap: () => context.go('/hadith-management'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.people_outline,
            text: 'User Management',
            onTap: () => context.go('/user-management'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.notifications_outlined,
            text: 'Notification',
            onTap: () => context.go('/notification-management'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.spa_outlined,
            text: 'Azkar Management',
            onTap: () => context.go('/azkar'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.handshake_outlined,
            text: 'Duas Management',
            onTap: () => context.go('/duas'),
          ),
          const Divider(),
          _buildDrawerItem(
            context,
            icon: Icons.logout,
            text: 'Logout',
            onTap: () {
              context.read<AuthBloc>().add(LogoutEvent());
              context.go('/');
            },
          ),
        ],
      ),
    );
  }

  Widget _buildDrawerItem(BuildContext context, {required IconData icon, required String text, required GestureTapCallback onTap}) {
    return ListTile(
      leading: Icon(icon),
      title: Text(text),
      onTap: onTap,
    );
  }
}
