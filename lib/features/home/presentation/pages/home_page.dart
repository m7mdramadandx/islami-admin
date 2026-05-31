import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/home/domain/entities/home_stats.dart';
import 'package:islami_admin/features/home/presentation/bloc/home_bloc.dart';
import 'package:islami_admin/features/home/presentation/bloc/home_event.dart';
import 'package:islami_admin/features/home/presentation/bloc/home_state.dart';
import 'package:islami_admin/features/home/presentation/widgets/custom_drawer.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Dashboard'),
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_none_rounded),
            onPressed: () {},
          ),
          const SizedBox(width: 8),
          const CircleAvatar(
            radius: 18,
            backgroundImage: NetworkImage(
              'https://ui-avatars.com/api/?name=Admin&background=4A6B52&color=fff',
            ),
          ),
          const SizedBox(width: 16),
        ],
      ),
      drawer: const CustomDrawer(),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildHeaderSection(context),
            const SizedBox(height: 32),
            _buildStatsSection(context),
          ],
        ),
      ),
    );
  }

  Widget _buildHeaderSection(BuildContext context) {
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [AppColors.colorPrimary, AppColors.colorSecondary],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: AppColors.colorPrimary.withValues(alpha: 0.2),
            blurRadius: 24,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(28),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Welcome back, Admin',
                        style: TextStyle(
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                          color: AppColors.whiteSolid,
                          letterSpacing: -0.5,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Your unified control center for managing content, users, and engagement.',
                        style: TextStyle(
                          fontSize: 15,
                          color: AppColors.whiteSolid.withValues(alpha: 0.85),
                          height: 1.5,
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.whiteSolid.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Icon(
                    Icons.dashboard_rounded,
                    color: AppColors.whiteSolid,
                    size: 28,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: () => context.read<HomeBloc>().add(GetHomeStatsEvent()),
              icon: const Icon(Icons.refresh_rounded),
              label: const Text('Refresh Dashboard'),
              style: FilledButton.styleFrom(
                backgroundColor: AppColors.whiteSolid,
                foregroundColor: AppColors.colorPrimary,
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatsSection(BuildContext context) {
    return BlocBuilder<HomeBloc, HomeState>(
      builder: (context, state) {
        if (state is HomeLoading) {
          return const Center(
            child: Padding(
              padding: EdgeInsets.symmetric(vertical: 60),
              child: CircularProgressIndicator(),
            ),
          );
        } else if (state is HomeLoaded) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildKpiGrid(state.stats),
              const SizedBox(height: 32),
              _buildInsightsPanel(state.stats),
              const SizedBox(height: 32),
              _buildSectionTitle('Content Management'),
              const SizedBox(height: 16),
              _buildQuickActionsGrid(context),
              const SizedBox(height: 32),
              _buildSectionTitle('Recent Activity'),
              const SizedBox(height: 16),
              _buildRecentActivitySection(context),
              const SizedBox(height: 20),
            ],
          );
        } else if (state is HomeError) {
          return Center(
            child: Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: AppColors.failureRed.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.failureRed.withValues(alpha: 0.3)),
              ),
              child: Row(
                children: [
                  Icon(Icons.error_outline, color: AppColors.failureRed, size: 28),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Text(
                      'Error loading statistics: ${state.message}',
                      style: TextStyle(
                        color: AppColors.failureRed,
                        fontSize: 15,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          );
        }
        return const SizedBox.shrink();
      },
    );
  }

  Widget _buildKpiGrid(HomeStats stats) {
    return LayoutBuilder(
      builder: (context, constraints) {
        return GridView.count(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisCount: constraints.maxWidth > 900
              ? 4
              : (constraints.maxWidth > 600 ? 2 : 1),
          crossAxisSpacing: 24,
          mainAxisSpacing: 24,
          childAspectRatio: constraints.maxWidth > 900 ? 2.2 : 1.95,
          children: [
            _buildStatCard(
              title: 'Total Users',
              value: _formatNumber(stats.totalUsers),
              icon: Icons.people_alt_rounded,
              color: AppColors.info,
              trend: 'Registered accounts',
            ),
            _buildStatCard(
              title: 'Feedback',
              value: _formatNumber(stats.totalFeedback),
              icon: Icons.chat_bubble_rounded,
              color: AppColors.success,
              trend: 'Messages received',
            ),
            _buildStatCard(
              title: 'Hadith Entries',
              value: _formatNumber(stats.totalHadith),
              icon: Icons.format_quote_rounded,
              color: AppColors.gold,
              trend: 'Managed in Firestore',
            ),
            _buildStatCard(
              title: 'Quran Surahs',
              value: _formatNumber(stats.quranSurahs),
              icon: Icons.menu_book_rounded,
              color: AppColors.green,
              trend: 'Core content size',
            ),
            _buildStatCard(
              title: 'Quran Ayahs',
              value: _formatNumber(stats.quranAyahs),
              icon: Icons.auto_stories_rounded,
              color: AppColors.blue,
              trend: 'Canonical total',
            ),
            _buildStatCard(
              title: 'Feedback Density',
              value:
                  '${stats.feedbackRatePerThousandUsers.toStringAsFixed(1)} / 1k',
              icon: Icons.insights_rounded,
              color: AppColors.info,
              trend: 'Feedback per 1000 users',
            ),
          ],
        );
      },
    );
  }

  Widget _buildStatCard({
    required String title,
    required String value,
    required IconData icon,
    required Color color,
    required String trend,
  }) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border, width: 1),
      ),
      child: Material(
        color: Colors.transparent,
        child: Padding(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: color.withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(icon, color: color, size: 26),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: color.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: Text(
                      trend,
                      style: TextStyle(
                        fontSize: 11,
                        color: color,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    value,
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.w800,
                      color: AppColors.colorPrimary,
                      letterSpacing: -0.5,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    title,
                    style: TextStyle(
                      fontSize: 14,
                      color: AppColors.subText,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInsightsPanel(HomeStats stats) {
    final feedbackStatus = stats.feedbackRatePerThousandUsers >= 15
        ? 'High user feedback volume'
        : 'Low feedback volume';
    final contentCoverage = stats.totalHadith + stats.quranSurahs;

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border, width: 1),
      ),
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: AppColors.info.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Icon(
                    Icons.show_chart_rounded,
                    color: AppColors.info,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                _buildSectionTitle('Insights & Health'),
              ],
            ),
            const SizedBox(height: 20),
            _buildInsightRow(
              icon: Icons.mark_chat_read_rounded,
              color: AppColors.info,
              title: feedbackStatus,
              subtitle:
                  '${stats.feedbackRatePerThousandUsers.toStringAsFixed(1)} feedback messages per 1000 users.',
            ),
            const SizedBox(height: 18),
            Divider(color: AppColors.border.withValues(alpha: 0.5), height: 1),
            const SizedBox(height: 18),
            _buildInsightRow(
              icon: Icons.inventory_2_rounded,
              color: AppColors.success,
              title: 'Content inventory snapshot',
              subtitle:
                  '$contentCoverage managed records across Quran + Hadith modules.',
            ),
            const SizedBox(height: 18),
            Divider(color: AppColors.border.withValues(alpha: 0.5), height: 1),
            const SizedBox(height: 18),
            _buildInsightRow(
              icon: Icons.system_update_rounded,
              color: AppColors.gold,
              title: 'App version in dashboard',
              subtitle: 'Latest tagged version: ${stats.appVersion}.',
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInsightRow({
    required IconData icon,
    required Color color,
    required String title,
    required String subtitle,
  }) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: color.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: color, size: 20),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w700,
                  color: AppColors.colorPrimary,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                subtitle,
                style: TextStyle(
                  fontSize: 13,
                  color: AppColors.subText,
                  height: 1.4,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: TextStyle(
        fontSize: 20,
        fontWeight: FontWeight.w800,
        color: AppColors.colorPrimary,
        letterSpacing: -0.3,
      ),
    );
  }

  String _formatNumber(int value) {
    if (value >= 1000000) {
      return '${(value / 1000000).toStringAsFixed(1)}M';
    }
    if (value >= 1000) {
      return '${(value / 1000).toStringAsFixed(1)}K';
    }
    return value.toString();
  }

  Widget _buildQuickActionsGrid(BuildContext context) {
    final actions = [
      _ActionItem(
        'Quran',
        Icons.menu_book_rounded,
        '/quran-management',
        AppColors.green,
      ),
      _ActionItem(
        'Hadith',
        Icons.format_quote_rounded,
        '/hadith-management',
        AppColors.gold,
      ),
      _ActionItem('Azkar', Icons.spa_rounded, '/azkar', AppColors.success),
      _ActionItem(
        'Duas',
        Icons.volunteer_activism_rounded,
        '/duas',
        AppColors.colorAccent,
      ),
      _ActionItem(
        'Users',
        Icons.group_rounded,
        '/user-management',
        AppColors.info,
      ),
      _ActionItem(
        'Notifications',
        Icons.notifications_active_rounded,
        '/notification-management',
        AppColors.colorPrimary,
      ),
      _ActionItem(
        'Feedback',
        Icons.feedback_rounded,
        '/feedback',
        AppColors.blue,
      ),
    ];

    return LayoutBuilder(
      builder: (context, constraints) {
        return GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: constraints.maxWidth > 1200
                ? 4
                : (constraints.maxWidth > 800 ? 3 : 2),
            crossAxisSpacing: 16,
            mainAxisSpacing: 16,
            childAspectRatio: 3.2,
          ),
          itemCount: actions.length,
          itemBuilder: (context, index) {
            final action = actions[index];
            return Material(
              color: Colors.transparent,
              child: InkWell(
                onTap: () => context.go(action.route),
                borderRadius: BorderRadius.circular(14),
                child: Container(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(color: AppColors.border, width: 1),
                    color: action.color.withValues(alpha: 0.02),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(10),
                          decoration: BoxDecoration(
                            color: action.color.withValues(alpha: 0.12),
                            shape: BoxShape.circle,
                          ),
                          child: Icon(action.icon, color: action.color, size: 24),
                        ),
                        const SizedBox(width: 14),
                        Text(
                          action.title,
                          style: TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.w700,
                            color: AppColors.colorPrimary,
                          ),
                        ),
                        const Spacer(),
                        Icon(
                          Icons.arrow_forward_rounded,
                          color: action.color,
                          size: 18,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildRecentActivitySection(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border, width: 1),
      ),
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: AppColors.blue.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Icon(
                        Icons.history_rounded,
                        color: AppColors.blue,
                        size: 20,
                      ),
                    ),
                    const SizedBox(width: 12),
                    _buildSectionTitle('Recent Activity'),
                  ],
                ),
                TextButton.icon(
                  onPressed: () => context.go('/notification-management'),
                  icon: const Icon(Icons.arrow_forward_rounded, size: 18),
                  label: const Text('View All'),
                  style: TextButton.styleFrom(
                    foregroundColor: AppColors.colorPrimary,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            _buildActivityItem(
              'New Hadith Added',
              'Admin added a new hadith to the collection',
              '2 mins ago',
              Icons.add_circle_outline_rounded,
              AppColors.success,
            ),
            Divider(height: 28, color: AppColors.border.withValues(alpha: 0.5)),
            _buildActivityItem(
              'User Feedback',
              'New feedback received from user "Ahmed"',
              '45 mins ago',
              Icons.message_outlined,
              AppColors.info,
            ),
            Divider(height: 28, color: AppColors.border.withValues(alpha: 0.5)),
            _buildActivityItem(
              'Notification Sent',
              'Ramadan reminder sent to all users',
              '3 hours ago',
              Icons.send_rounded,
              AppColors.gold,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildActivityItem(
    String title,
    String subtitle,
    String time,
    IconData icon,
    Color color,
  ) {
    return Row(
      children: [
        Container(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: color.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: color, size: 20),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w700,
                  color: AppColors.colorPrimary,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                subtitle,
                style: TextStyle(
                  fontSize: 13,
                  color: AppColors.subText,
                ),
              ),
            ],
          ),
        ),
        Text(
          time,
          style: TextStyle(
            fontSize: 12,
            color: AppColors.grey,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }
}

class _ActionItem {
  final String title;
  final IconData icon;
  final String route;
  final Color color;

  _ActionItem(this.title, this.icon, this.route, this.color);
}
