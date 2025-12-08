import 'package:flutter/material.dart';
import 'package:islami_admin/features/notification/data/services/notification_service.dart';

class NotificationManagementPage extends StatefulWidget {
  const NotificationManagementPage({super.key});

  @override
  State<NotificationManagementPage> createState() =>
      _NotificationManagementPageState();
}

class _NotificationManagementPageState
    extends State<NotificationManagementPage> {
  final _titleController = TextEditingController(text: "أذكار اليوم");
  final _bodyController = TextEditingController(
    text:
        "🙏 أسْتَغْفِرُ اللهَ العَظِيمَ الَّذِي لاَ إلَهَ إلاَّ هُوَ، الحَيُّ القَيُّومُ، وَأتُوبُ إلَيهِ ",
  );
  final _imageUrlController = TextEditingController();
  final _topicController = TextEditingController();

  final List<Map<String, String>> _savedNotifications = [
    {
      'title': 'New Article Alert!',
      'body':
          'Check out our latest article on the importance of daily prayers.',
    },
    {
      'title': 'Jummah Reminder',
      'body': 'Don\'t forget to attend Jummah prayers today!',
    },
    {
      'title': 'Daily Hadith',
      'body': 'A new hadith has been added to the collection.',
    },
  ];

  Map<String, String>? _selectedSavedNotification;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Notifications')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            _buildSectionTitle('Send New Notification'),
            const SizedBox(height: 16),
            TextField(
              controller: _titleController,
              decoration: const InputDecoration(labelText: 'Title'),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _bodyController,
              decoration: const InputDecoration(labelText: 'Body'),
              maxLines: 3,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _imageUrlController,
              decoration: const InputDecoration(
                labelText: 'Image URL (Optional)',
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _topicController,
              decoration: const InputDecoration(
                labelText: 'Topic (e.g., news, events)',
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: _sendNotification,
              child: const Text('Send Notification'),
            ),
            const Divider(height: 40),
            _buildSectionTitle('Send Saved Notification'),
            const SizedBox(height: 16),
            DropdownButtonFormField<Map<String, String>>(
              hint: const Text('Select a notification template'),
              items: _savedNotifications.map((notification) {
                return DropdownMenuItem(
                  value: notification,
                  child: Text(notification['title']!),
                );
              }).toList(),
              onChanged: (value) {
                setState(() {
                  _selectedSavedNotification = value;
                  if (value != null) {
                    _titleController.text = value['title']!;
                    _bodyController.text = value['body']!;
                  }
                });
              },
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: _selectedSavedNotification != null
                  ? _sendNotification
                  : null,
              child: const Text('Send Selected Notification'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: Theme.of(
        context,
      ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
    );
  }

  void _sendNotification() async {
    if (!mounted) return;

    final title = _titleController.text;
    final body = _bodyController.text;
    final imageUrl = _imageUrlController.text;
    final topic = _topicController.text;

    if (title.isEmpty || body.isEmpty) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Title and body cannot be empty.'),
          backgroundColor: Colors.orange,
        ),
      );
      return;
    }

    try {
      await NotificationService.sendNotification(
        title: title,
        body: body,
        imageUrl: imageUrl.isNotEmpty ? imageUrl : null,
        topic: topic.isNotEmpty ? topic : null,
      );
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Notification sent successfully!'),
          backgroundColor: Colors.green,
        ),
      );
      _clearForm();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error sending notification: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  void _clearForm() {
    _titleController.clear();
    _bodyController.clear();
    _imageUrlController.clear();
    _topicController.clear();
    setState(() {
      _selectedSavedNotification = null;
    });
  }
}
