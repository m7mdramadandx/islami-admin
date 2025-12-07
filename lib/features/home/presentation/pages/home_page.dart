import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:islami_admin/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:islami_admin/features/home/presentation/widgets/custom_drawer.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Admin Panel'),
        elevation: 0,
        backgroundColor: Theme.of(context).colorScheme.primary,
        foregroundColor: Theme.of(context).colorScheme.onPrimary,
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () {
              context.read<AuthBloc>().add(LogoutEvent());
            },
          ),
        ],
      ),
      drawer: const CustomDrawer(),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: GridView.count(
          crossAxisCount: 2,
          crossAxisSpacing: 16,
          mainAxisSpacing: 16,
          children: [
            _buildDashboardCard(
              context: context,
              icon: Icons.people,
              label: 'Users',
              onTap: () {
                GoRouter.of(context).go('/user-management');
              },
            ),
            _buildDashboardCard(
              context: context,
              icon: Icons.article,
              label: 'Articles',
              onTap: () {},
            ),
            _buildDashboardCard(
              context: context,
              icon: Icons.category,
              label: 'Categories',
              onTap: () {},
            ),
            _buildDashboardCard(
              context: context,
              icon: Icons.comment,
              label: 'Comments',
              onTap: () {},
            ),
            _buildDashboardCard(
              context: context,
              icon: Icons.book,
              label: 'Hadith',
              onTap: () {
                GoRouter.of(context).go('/hadith-management');
              },
            ),
            _buildDashboardCard(
              context: context,
              icon: Icons.mosque,
              label: 'Quran',
              onTap: () {
                GoRouter.of(context).go('/quran-management');
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDashboardCard({
    required BuildContext context,
    required IconData icon,
    required String label,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Card(
        elevation: 4,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 50,
            ),
            const SizedBox(height: 10),
            Text(
              label,
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
