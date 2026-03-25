import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/feedback/domain/entities/feedback_message.dart';
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
        appBar: AppBar(
          title: const Text('User Feedback'),
          centerTitle: true,
          actions: [
            Builder(
              builder: (context) => IconButton(
                icon: const Icon(Icons.refresh_rounded),
                onPressed: () {
                  context.read<FeedbackBloc>().add(GetFeedbackMessagesEvent());
                },
              ),
            ),
            const SizedBox(width: 8),
          ],
        ),
        body: BlocBuilder<FeedbackBloc, FeedbackState>(
          builder: (context, state) {
            if (state is FeedbackLoading) {
              return const Center(child: CircularProgressIndicator());
            } else if (state is FeedbackLoaded) {
              final messages = state.messages;
              if (messages.isEmpty) {
                return _buildEmptyState(context);
              }
              return _buildFeedbackList(context, messages);
            } else if (state is FeedbackError) {
              return _buildErrorState(context, state.message);
            }
            return const SizedBox.shrink();
          },
        ),
      ),
    );
  }

  Widget _buildFeedbackList(
    BuildContext context,
    List<FeedbackMessage> messages,
  ) {
    return RefreshIndicator(
      color: AppColors.colorPrimary,
      onRefresh: () async {
        context.read<FeedbackBloc>().add(GetFeedbackMessagesEvent());
      },
      child: ListView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 24),
        itemCount: messages.length,
        itemBuilder: (context, index) {
          return Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: _FeedbackCard(message: messages[index]),
          );
        },
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Card(
        margin: const EdgeInsets.all(24),
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircleAvatar(
                radius: 40,
                backgroundColor: AppColors.buttonSecondary,
                child: Icon(
                  Icons.feedback_outlined,
                  size: 40,
                  color: AppColors.grey,
                ),
              ),
              const SizedBox(height: 24),
              Text(
                'No feedback yet',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: AppColors.colorPrimary,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Feedback from users will appear here.',
                textAlign: TextAlign.center,
                style: TextStyle(color: AppColors.subText),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildErrorState(BuildContext context, String message) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Card(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(
                  Icons.error_outline_rounded,
                  color: AppColors.failureRed,
                  size: 64,
                ),
                const SizedBox(height: 24),
                Text(
                  'Failed to load feedback',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: AppColors.colorPrimary,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  message,
                  textAlign: TextAlign.center,
                  style: TextStyle(color: AppColors.subText),
                ),
                const SizedBox(height: 24),
                ElevatedButton(
                  onPressed: () {
                    context.read<FeedbackBloc>().add(
                      GetFeedbackMessagesEvent(),
                    );
                  },
                  child: Text(
                    'Try Again',
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _FeedbackCard extends StatelessWidget {
  final FeedbackMessage message;

  const _FeedbackCard({required this.message});

  @override
  Widget build(BuildContext context) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Row(
                        children: [
                          CircleAvatar(
                            backgroundColor: AppColors.colorPrimary.withValues(
                              alpha: 0.12,
                            ),
                            child: Icon(
                              Icons.person_rounded,
                              color: AppColors.colorPrimary,
                              size: 20,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  message.email.isNotEmpty
                                      ? message.email
                                      : 'Anonymous User',
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    color: AppColors.colorPrimary,
                                    fontSize: 15,
                                  ),
                                ),
                                Text(
                                  message.date,
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: AppColors.subText,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    _buildBadge(message.appVersion, AppColors.info),
                  ],
                ),
                const SizedBox(height: 20),
                Text(
                  message.msg,
                  style: TextStyle(
                    fontSize: 15,
                    height: 1.6,
                    color: AppColors.text,
                  ),
                ),
              ],
            ),
          ),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            color: AppColors.buttonSecondary,
            child: Row(
              children: [
                _buildDeviceTag(message.deviceName),
                const Spacer(),
                if (message.phone.isNotEmpty) _buildPhoneTag(message.phone),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBadge(String label, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        'v$label',
        style: TextStyle(
          color: color,
          fontSize: 11,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  Widget _buildDeviceTag(String device) {
    return Row(
      children: [
        Icon(Icons.smartphone_rounded, size: 14, color: AppColors.grey),
        const SizedBox(width: 6),
        Text(
          device,
          style: TextStyle(
            fontSize: 12,
            color: AppColors.subText,
            fontWeight: FontWeight.w500,
          ),
          overflow: TextOverflow.ellipsis,
        ),
      ],
    );
  }

  Widget _buildPhoneTag(String phone) {
    return Row(
      children: [
        Icon(Icons.phone_rounded, size: 14, color: AppColors.grey),
        const SizedBox(width: 6),
        Text(
          phone,
          style: TextStyle(
            fontSize: 12,
            color: AppColors.subText,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }
}
