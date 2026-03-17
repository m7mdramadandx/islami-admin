import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/feedback/presentation/bloc/feedback_bloc.dart';
import 'package:islami_admin/features/feedback/presentation/bloc/feedback_event.dart';
import 'package:islami_admin/features/feedback/presentation/bloc/feedback_state.dart';
import 'package:islami_admin/injection_container.dart';

class FeedbackPage extends StatelessWidget {
  const FeedbackPage({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (context) => sl<FeedbackBloc>()..add(GetFeedbackMessagesEvent()),
      child: Scaffold(
        backgroundColor: AppColors.colorBackground,
        appBar: AppBar(
          title: const Text(
            'User Feedback',
            style: TextStyle(fontWeight: FontWeight.bold),
          ),
          elevation: 0,
          backgroundColor: Colors.transparent,
          surfaceTintColor: Colors.transparent,
          centerTitle: true,
        ),
        body: BlocBuilder<FeedbackBloc, FeedbackState>(
          builder: (context, state) {
            if (state is FeedbackLoading) {
              return const Center(
                child: CircularProgressIndicator(strokeWidth: 3),
              );
            } else if (state is FeedbackLoaded) {
              final messages = state.messages;
              if (messages.isEmpty) {
                return _buildEmptyState(context);
              }
              return RefreshIndicator(
                onRefresh: () async {
                  context.read<FeedbackBloc>().add(GetFeedbackMessagesEvent());
                },
                child: ListView.builder(
                  padding: const EdgeInsets.all(20),
                  itemCount: messages.length,
                  itemBuilder: (context, index) {
                    final message = messages[index];
                    return _FeedbackCard(message: message);
                  },
                ),
              );
            } else if (state is FeedbackError) {
              return _buildErrorState(context, state.message);
            }
            return const SizedBox.shrink();
          },
        ),
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.feedback_outlined, size: 80, color: Colors.grey[400]),
          const SizedBox(height: 16),
          Text(
            'No feedback messages yet',
            style: Theme.of(
              context,
            ).textTheme.titleMedium?.copyWith(color: Colors.grey[600]),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(BuildContext context, String message) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.error_outline,
              color: AppColors.failureRed,
              size: 60,
            ),
            const SizedBox(height: 16),
            Text(
              'Oops! Something went wrong',
              style: Theme.of(
                context,
              ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Text(
              message,
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey[600]),
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: () {
                context.read<FeedbackBloc>().add(GetFeedbackMessagesEvent());
              },
              icon: const Icon(Icons.refresh),
              label: const Text('Try Again'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(
                  horizontal: 32,
                  vertical: 12,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _FeedbackCard extends StatelessWidget {
  final dynamic message;

  const _FeedbackCard({required this.message});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(20),
        child: IntrinsicHeight(
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Container(width: 6, color: AppColors.colorAccent),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(20.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Wrap(
                            spacing: 8,
                            runSpacing: 8,
                            children: [
                              _buildInfoBadge(
                                'v${message.appVersion}',
                                AppColors.colorAccent,
                              ),
                              _buildInfoBadge(
                                message.deviceName,
                                AppColors.info,
                              ),
                            ],
                          ),
                          Text(
                            message.date,
                            style: TextStyle(
                              color: Colors.grey[500],
                              fontSize: 12,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          _buildContactInfo(
                            Icons.email_outlined,
                            message.email,
                          ),
                          const SizedBox(width: 16),
                          _buildContactInfo(
                            Icons.phone_outlined,
                            message.phone,
                          ),
                        ],
                      ),
                      const Divider(height: 24),
                      Text(
                        message.msg,
                        style: const TextStyle(
                          fontSize: 16,
                          height: 1.5,
                          color: AppColors.text,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInfoBadge(String label, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: color,
          fontWeight: FontWeight.bold,
          fontSize: 12,
        ),
      ),
    );
  }

  Widget _buildContactInfo(IconData icon, String value) {
    return Expanded(
      child: Row(
        children: [
          Icon(icon, size: 14, color: AppColors.natural600),
          const SizedBox(width: 4),
          Flexible(
            child: Text(
              value,
              style: const TextStyle(
                fontSize: 12,
                color: AppColors.natural600,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
