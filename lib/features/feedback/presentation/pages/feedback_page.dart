import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_fonts/google_fonts.dart';
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
      onRefresh: () async {
        context.read<FeedbackBloc>().add(GetFeedbackMessagesEvent());
      },
      child: ListView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 24),
        itemCount: messages.length,
        itemBuilder: (context, index) {
          return _FeedbackCard(message: messages[index]);
        },
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: Colors.grey.shade100,
              shape: BoxShape.circle,
            ),
            child: Icon(
              Icons.feedback_outlined,
              size: 48,
              color: Colors.grey.shade400,
            ),
          ),
          const SizedBox(height: 24),
          Text(
            'No feedback yet',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: AppColors.colorPrimary,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Feedback from users will appear here.',
            style: GoogleFonts.plusJakartaSans(color: Colors.grey.shade500),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(BuildContext context, String message) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.error_outline_rounded,
              color: Colors.redAccent,
              size: 64,
            ),
            const SizedBox(height: 24),
            Text(
              'Failed to load feedback',
              style: GoogleFonts.plusJakartaSans(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: AppColors.colorPrimary,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              message,
              textAlign: TextAlign.center,
              style: GoogleFonts.plusJakartaSans(color: Colors.grey.shade600),
            ),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: () {
                context.read<FeedbackBloc>().add(GetFeedbackMessagesEvent());
              },
              child: const Text('Try Again'),
            ),
          ],
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
    return Container(
      margin: const EdgeInsets.only(bottom: 20),
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
        children: [
          Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        CircleAvatar(
                          backgroundColor: AppColors.colorPrimary.withOpacity(
                            0.1,
                          ),
                          child: const Icon(
                            Icons.person_rounded,
                            color: AppColors.colorPrimary,
                            size: 20,
                          ),
                        ),
                        const SizedBox(width: 12),
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              message.email.isNotEmpty
                                  ? message.email
                                  : 'Anonymous User',
                              style: GoogleFonts.plusJakartaSans(
                                fontWeight: FontWeight.bold,
                                color: AppColors.colorPrimary,
                                fontSize: 15,
                              ),
                            ),
                            Text(
                              message.date,
                              style: GoogleFonts.plusJakartaSans(
                                fontSize: 12,
                                color: Colors.grey.shade500,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                    _buildBadge(message.appVersion, Colors.blue),
                  ],
                ),
                const SizedBox(height: 20),
                Text(
                  message.msg,
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 15,
                    height: 1.6,
                    color: AppColors.text,
                  ),
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            decoration: BoxDecoration(
              color: Colors.grey.shade50,
              borderRadius: const BorderRadius.vertical(
                bottom: Radius.circular(20),
              ),
            ),
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
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        'v$label',
        style: GoogleFonts.plusJakartaSans(
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
        Icon(Icons.smartphone_rounded, size: 14, color: Colors.grey.shade400),
        const SizedBox(width: 6),
        Text(
          device,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 12,
            color: Colors.grey.shade500,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }

  Widget _buildPhoneTag(String phone) {
    return Row(
      children: [
        Icon(Icons.phone_rounded, size: 14, color: Colors.grey.shade400),
        const SizedBox(width: 6),
        Text(
          phone,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 12,
            color: Colors.grey.shade500,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }
}
