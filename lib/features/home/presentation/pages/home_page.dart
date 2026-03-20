import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
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
              'https://ui-avatars.com/api/?name=Admin&background=162853&color=fff',
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
            const SizedBox(height: 24),
            _buildStatsSection(context),
          ],
        ),
      ),
    );
  }

  Widget _buildHeaderSection(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        gradient: const LinearGradient(
          colors: [Color(0xFF162853), Color(0xFF2A4A8F)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Welcome back, Admin',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 28,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'A unified control center for users, content quality, and delivery.',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 15,
              color: Colors.white.withValues(alpha: 0.85),
            ),
          ),
          const SizedBox(height: 18),
          FilledButton.icon(
            onPressed: () => context.read<HomeBloc>().add(GetHomeStatsEvent()),
            icon: const Icon(Icons.refresh_rounded),
            label: const Text('Refresh Dashboard'),
            style: FilledButton.styleFrom(
              backgroundColor: Colors.white,
              foregroundColor: AppColors.colorPrimary,
              padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 14),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatsSection(BuildContext context) {
    return BlocBuilder<HomeBloc, HomeState>(
      builder: (context, state) {
        if (state is HomeLoading) {
          return const Center(
            child: Padding(
              padding: EdgeInsets.symmetric(vertical: 40),
              child: CircularProgressIndicator(),
            ),
          );
        } else if (state is HomeLoaded) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildKpiGrid(context, state.stats),
              const SizedBox(height: 20),
              _buildInsightsPanel(context, state.stats),
              const SizedBox(height: 28),
              _buildSectionTitle('Content Management'),
              const SizedBox(height: 14),
              _buildQuickActionsGrid(context),
              const SizedBox(height: 28),
              _buildSectionTitle('Recent Activity'),
              const SizedBox(height: 14),
              _buildRecentActivitySection(context),
            ],
          );
        } else if (state is HomeError) {
          return Center(
            child: Text(
              'Error loading statistics: ${state.message}',
              style: const TextStyle(color: Colors.red),
            ),
          );
        }
        return const SizedBox.shrink();
      },
    );
  }

  Widget _buildKpiGrid(BuildContext context, HomeStats stats) {
    return LayoutBuilder(
      builder: (context, constraints) {
        return GridView.count(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisCount: constraints.maxWidth > 900
              ? 4
              : (constraints.maxWidth > 600 ? 2 : 1),
          crossAxisSpacing: 20,
          mainAxisSpacing: 20,
          childAspectRatio: constraints.maxWidth > 900 ? 2.1 : 1.9,
          children: [
            _buildStatCard(title: 'Total Users', value: _formatNumber(stats.totalUsers), icon: Icons.people_alt_rounded, color: Colors.blue, trend: 'Registered accounts'),
            _buildStatCard(title: 'Active Today', value: _formatNumber(stats.activeToday), icon: Icons.bolt_rounded, color: Colors.orange, trend: 'GA4 DAU when available'),
            _buildStatCard(title: 'DAU / WAU / MAU', value: '${_formatNumber(stats.analyticsDau)} / ${_formatNumber(stats.analyticsWau)} / ${_formatNumber(stats.analyticsMau)}', icon: Icons.groups_rounded, color: Colors.deepPurple, trend: 'Analytics users'),
            _buildStatCard(title: 'Events (30d)', value: _formatNumber(stats.analyticsTotalEvents30d), icon: Icons.timeline_rounded, color: Colors.brown, trend: 'Total GA4 events'),
            _buildStatCard(title: 'Feedback', value: _formatNumber(stats.totalFeedback), icon: Icons.chat_bubble_rounded, color: Colors.green, trend: 'Messages received'),
            _buildStatCard(title: 'Hadith Entries', value: _formatNumber(stats.totalHadith), icon: Icons.format_quote_rounded, color: Colors.amber.shade700, trend: 'Managed in Firestore'),
            _buildStatCard(title: 'Quran Surahs', value: _formatNumber(stats.quranSurahs), icon: Icons.menu_book_rounded, color: Colors.teal, trend: 'Core content size'),
            _buildStatCard(title: 'Quran Ayahs', value: _formatNumber(stats.quranAyahs), icon: Icons.auto_stories_rounded, color: Colors.indigo, trend: 'Canonical total'),
            _buildStatCard(title: 'Active Rate', value: '${stats.activeUsersPercentage.toStringAsFixed(1)}%', icon: Icons.trending_up_rounded, color: Colors.pink, trend: 'Today / total users'),
            _buildStatCard(title: 'Feedback Density', value: '${stats.feedbackRatePerThousandUsers.toStringAsFixed(1)} / 1k', icon: Icons.insights_rounded, color: Colors.cyan.shade700, trend: 'Feedback per 1000 users'),
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
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.grey.shade100),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.shade50,
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(icon, color: color, size: 24),
              ),
              Text(
                trend,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 11,
                  color: color,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                value,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: AppColors.colorPrimary,
                ),
              ),
              Text(
                title,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 13,
                  color: Colors.grey.shade500,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildInsightsPanel(BuildContext context, HomeStats stats) {
    final engagementStatus = stats.activeUsersPercentage >= 10
        ? 'Healthy engagement'
        : 'Engagement needs attention';
    final feedbackStatus = stats.feedbackRatePerThousandUsers >= 15
        ? 'High user feedback volume'
        : 'Low feedback volume';
    final contentCoverage = stats.totalHadith + stats.quranSurahs;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.grey.shade100),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionTitle('Insights & Health'),
          const SizedBox(height: 12),
          _buildInsightRow(
            icon: Icons.health_and_safety_rounded,
            color: Colors.teal,
            title: engagementStatus,
            subtitle:
                'Current active rate is ${stats.activeUsersPercentage.toStringAsFixed(1)}%.',
          ),
          const SizedBox(height: 10),
          _buildInsightRow(
            icon: Icons.mark_chat_read_rounded,
            color: Colors.blue,
            title: feedbackStatus,
            subtitle:
                '${stats.feedbackRatePerThousandUsers.toStringAsFixed(1)} feedback messages per 1000 users.',
          ),
          const SizedBox(height: 10),
          _buildInsightRow(
            icon: Icons.notifications_active_rounded,
            color: Colors.deepOrange,
            title: 'Notification funnel (30d)',
            subtitle:
                'Sent: ${_formatNumber(stats.analyticsNotificationSent30d)}, opened: ${_formatNumber(stats.analyticsNotificationOpened30d)}.',
          ),
          const SizedBox(height: 10),
          _buildInsightRow(
            icon: Icons.touch_app_rounded,
            color: Colors.indigo,
            title: 'Content and feedback events (30d)',
            subtitle:
                'Content views: ${_formatNumber(stats.analyticsContentViewed30d)}, feedback submits: ${_formatNumber(stats.analyticsFeedbackSubmitted30d)}.',
          ),
          const SizedBox(height: 10),
          _buildInsightRow(
            icon: Icons.inventory_2_rounded,
            color: Colors.deepPurple,
            title: 'Content inventory snapshot',
            subtitle:
                '$contentCoverage managed records across Quran + Hadith modules.',
          ),
          const SizedBox(height: 10),
          _buildInsightRow(
            icon: Icons.system_update_rounded,
            color: Colors.orange,
            title: 'App version in dashboard',
            subtitle: 'Latest tagged version: ${stats.appVersion}.',
          ),
        ],
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
            color: color.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: color, size: 20),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 14,
                  fontWeight: FontWeight.w700,
                  color: AppColors.colorPrimary,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                subtitle,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 13,
                  color: Colors.grey.shade600,
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
      style: GoogleFonts.plusJakartaSans(
        fontSize: 18,
        fontWeight: FontWeight.bold,
        color: AppColors.colorPrimary,
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
        Colors.teal,
      ),
      _ActionItem(
        'Hadith',
        Icons.format_quote_rounded,
        '/hadith-management',
        Colors.amber,
      ),
      _ActionItem('Azkar', Icons.spa_rounded, '/azkar', Colors.green),
      _ActionItem(
        'Duas',
        Icons.volunteer_activism_rounded,
        '/duas',
        Colors.pink,
      ),
      _ActionItem(
        'Users',
        Icons.group_rounded,
        '/user-management',
        Colors.indigo,
      ),
      _ActionItem(
        'Notifications',
        Icons.notifications_active_rounded,
        '/notification-management',
        Colors.orange,
      ),
      _ActionItem('Feedback', Icons.feedback_rounded, '/feedback', Colors.cyan),
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
            childAspectRatio: 3.4,
          ),
          itemCount: actions.length,
          itemBuilder: (context, index) {
            final action = actions[index];
            return InkWell(
              onTap: () => context.go(action.route),
              borderRadius: BorderRadius.circular(16),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: Colors.grey.shade100),
                ),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: action.color.withValues(alpha: 0.1),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(action.icon, color: action.color, size: 24),
                    ),
                    const SizedBox(width: 16),
                    Text(
                      action.title,
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                        color: AppColors.colorPrimary,
                      ),
                    ),
                    const Spacer(),
                    Icon(
                      Icons.chevron_right_rounded,
                      color: Colors.grey.shade400,
                    ),
                  ],
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
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.grey.shade100),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Recent Activity',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: AppColors.colorPrimary,
                ),
              ),
              TextButton(
                onPressed: () => context.go('/notification-management'),
                child: const Text('Open Notifications'),
              ),
            ],
          ),
          const SizedBox(height: 16),
          _buildActivityItem(
            'New Hadith Added',
            'Admin added a new hadith to the collection',
            '2 mins ago',
            Icons.add_circle_outline_rounded,
            Colors.green,
          ),
          const Divider(height: 32),
          _buildActivityItem(
            'User Feedback',
            'New feedback received from user "Ahmed"',
            '45 mins ago',
            Icons.message_outlined,
            Colors.blue,
          ),
          const Divider(height: 32),
          _buildActivityItem(
            'Notification Sent',
            'Ramadan reminder sent to all users',
            '3 hours ago',
            Icons.send_rounded,
            Colors.orange,
          ),
        ],
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
            color: color.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: color, size: 20),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 15,
                  fontWeight: FontWeight.w600,
                  color: AppColors.colorPrimary,
                ),
              ),
              Text(
                subtitle,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 13,
                  color: Colors.grey.shade500,
                ),
              ),
            ],
          ),
        ),
        Text(
          time,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 12,
            color: Colors.grey.shade400,
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
