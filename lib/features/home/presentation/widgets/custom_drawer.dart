
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

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
            icon: Icons.home,
            text: 'Home',
            onTap: () => context.go('/home'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.book,
            text: 'Quran Management',
            onTap: () => context.go('/quran-management'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.read_more, // Placeholder for Hadith
            text: 'Hadith Management',
            onTap: () => context.go('/hadith-management'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.person,
            text: 'User Management',
            onTap: () => context.go('/user-management'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.notifications,
            text: 'Notification',
            onTap: () => context.go('/notification-management'),
          ),
          _buildDrawerItem(
            context,
            icon: Icons.shield, // Placeholder for Azkar
            text: 'Azkar Management',
            onTap: () => context.go('/azkar-management'),
          ),
          const Divider(),
          _buildDrawerItem(
            context,
            icon: Icons.logout,
            text: 'Logout',
            onTap: () {
              // Handle logout
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
