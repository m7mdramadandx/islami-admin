import 'package:flutter/material.dart';
import 'package:islami_admin/core/utils/colors.dart';
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
  final _fcmTokenController = TextEditingController();
  final _routeTypeController = TextEditingController();
  final _routeIdController = TextEditingController();
  final _extraRouteIdController = TextEditingController();
  final _minVersionController = TextEditingController();

  final List<Map<String, String>> _templates = [
    {
      'title': 'أذكار الصباح',
      'body': 'حان الآن موعد أذكار الصباح.. نور يومك بذكر الله.',
      'icon': 'wb_sunny',
    },
    {
      'title': 'أذكار المساء',
      'body': 'حان الآن موعد أذكار المساء.. حصن نفسك بذكر الله.',
      'icon': 'nightlight_round',
    },
    {
      'title': 'تذكير بالصلاة',
      'body': 'قال رسول الله ﷺ: "أول ما يحاسب عليه العبد يوم القيامة الصلاة".',
      'icon': 'mosque',
    },
    {
      'title': 'حديث اليوم',
      'body': 'تم إضافة حديث نبوي جديد.. اطلع عليه الآن.',
      'icon': 'menu_book',
    },
  ];

  bool _isSending = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Notifications'), centerTitle: true),
      body: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Left Side: Form and Templates
          Expanded(
            flex: 3,
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildHeader(),
                  const SizedBox(height: 40),
                  _buildTemplatesSection(),
                  const SizedBox(height: 40),
                  _buildComposeForm(),
                ],
              ),
            ),
          ),
          // Right Side: Preview (Hidden on small screens)
          if (MediaQuery.of(context).size.width > 900)
            Expanded(
              flex: 1,
              child: Center(child: _buildNotificationPreview()),
            ),
        ],
      ),
    );
  }

  Widget _buildHeader() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Push Notifications Center 🚀',
          style: TextStyle(
            fontSize: 32,
            fontWeight: FontWeight.bold,
            color: AppColors.colorPrimary,
          ),
        ),
        const SizedBox(height: 12),
        Text(
          'Compose and send instant updates to all your users or specific groups.',
          style: TextStyle(
            fontSize: 16,
            color: AppColors.subText,
          ),
        ),
      ],
    );
  }

  Widget _buildTemplatesSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(
              Icons.auto_awesome_outlined,
              color: AppColors.colorAccent,
              size: 22,
            ),
            const SizedBox(width: 8),
            Text(
              'Quick Templates',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: AppColors.colorPrimary,
              ),
            ),
          ],
        ),
        const SizedBox(height: 20),
        SizedBox(
          height: 150,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: _templates.length,
            itemBuilder: (context, index) {
              final template = _templates[index];
              return SizedBox(
                width: 240,
                child: _buildTemplateCard(template),
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _buildTemplateCard(Map<String, String> template) {
    return Card(
      margin: const EdgeInsets.only(right: 16),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: () {
          setState(() {
            _titleController.text = template['title']!;
            _bodyController.text = template['body']!;
          });
        },
        child: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: AppColors.colorPrimary.withValues(alpha: 0.05),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  _getIconData(template['icon']!),
                  color: AppColors.colorPrimary,
                  size: 20,
                ),
              ),
              const Spacer(),
              Text(
                template['title']!,
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 15,
                  color: AppColors.colorPrimary,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                template['body']!,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  fontSize: 12,
                  color: AppColors.subText,
                  height: 1.4,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  IconData _getIconData(String name) {
    switch (name) {
      case 'wb_sunny':
        return Icons.wb_sunny_rounded;
      case 'nightlight_round':
        return Icons.nightlight_round;
      case 'mosque':
        return Icons.mosque_rounded;
      case 'menu_book':
        return Icons.menu_book_rounded;
      default:
        return Icons.notifications_rounded;
    }
  }

  Widget _buildComposeForm() {
    return Card(
      clipBehavior: Clip.antiAlias,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(32)),
      child: Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildTextField(
            controller: _titleController,
            label: 'Notification Title',
            hint: 'E.g., Ramadan Reminder',
            icon: Icons.title_rounded,
          ),
          const SizedBox(height: 24),
          _buildTextField(
            controller: _bodyController,
            label: 'Message Content',
            hint: 'Write your message here...',
            icon: Icons.notes_rounded,
            maxLines: 4,
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(
                child: _buildTextField(
                  controller: _imageUrlController,
                  label: 'Image URL (Optional)',
                  hint: 'https://...',
                  icon: Icons.image_rounded,
                ),
              ),
              const SizedBox(width: 24),
              Expanded(
                child: _buildTextField(
                  controller: _topicController,
                  label: 'Topic / Channel',
                  hint: 'all_users',
                  icon: Icons.tag_rounded,
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(
                child: _buildTextField(
                  controller: _fcmTokenController,
                  label: 'Direct FCM Token (Optional)',
                  hint: 'fcm token...',
                  icon: Icons.key_rounded,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            'Target priority: FCM Token > Topic broadcast.',
            style: TextStyle(
              fontSize: 12,
              color: AppColors.subText,
            ),
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(
                child: _buildTextField(
                  controller: _routeTypeController,
                  label: 'Route Type (Optional)',
                  hint: '1',
                  icon: Icons.alt_route_rounded,
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: 24),
              Expanded(
                child: _buildTextField(
                  controller: _routeIdController,
                  label: 'Route ID (Optional)',
                  hint: '5',
                  icon: Icons.numbers_rounded,
                  keyboardType: TextInputType.number,
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(
                child: _buildTextField(
                  controller: _extraRouteIdController,
                  label: 'Extra Route ID (Optional)',
                  hint: '0',
                  icon: Icons.data_object_rounded,
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: 24),
              Expanded(
                child: _buildTextField(
                  controller: _minVersionController,
                  label: 'Min Version (Optional)',
                  hint: '1.0.0',
                  icon: Icons.system_update_rounded,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            'These values are sent in FCM data payload for deep-link/navigation handling.',
            style: TextStyle(
              fontSize: 12,
              color: AppColors.subText,
            ),
          ),
          const SizedBox(height: 40),
          SizedBox(
            width: double.infinity,
            height: 60,
            child: ElevatedButton(
              onPressed: _isSending ? null : _sendNotification,
              style: ElevatedButton.styleFrom(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
              ),
              child: _isSending
                  ? SizedBox(
                      height: 22,
                      width: 22,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        color: AppColors.whiteSolid,
                      ),
                    )
                  : Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.send_rounded, size: 20),
                        const SizedBox(width: 12),
                        Text(
                          _resolveSendButtonLabel(),
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
            ),
          ),
        ],
      ),
      ),
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String label,
    required String hint,
    required IconData icon,
    TextInputType? keyboardType,
    int maxLines = 1,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: AppColors.text,
          ),
        ),
        const SizedBox(height: 10),
        TextField(
          controller: controller,
          keyboardType: keyboardType,
          maxLines: maxLines,
          onChanged: (_) => setState(() {}),
          decoration: InputDecoration(
            hintText: hint,
            prefixIcon: Icon(icon, size: 20),
            filled: true,
          ),
        ),
      ],
    );
  }

  Widget _buildNotificationPreview() {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          'Live Preview',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: AppColors.grey,
          ),
        ),
        const SizedBox(height: 24),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 24,
                      height: 24,
                      decoration: BoxDecoration(
                        color: AppColors.colorPrimary,
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        Icons.mosque,
                        size: 14,
                        color: AppColors.whiteSolid,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      'ISLAMI APP',
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: AppColors.grey,
                        letterSpacing: 1.2,
                      ),
                    ),
                    const Spacer(),
                    Text(
                      'now',
                      style: TextStyle(
                        fontSize: 12,
                        color: AppColors.grey,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  _titleController.text.isEmpty
                      ? 'Notification Title'
                      : _titleController.text,
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                    color: AppColors.text,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  _bodyController.text.isEmpty
                      ? 'Your message will appear here...'
                      : _bodyController.text,
                  style: TextStyle(
                    fontSize: 14,
                    color: AppColors.subText,
                    height: 1.4,
                  ),
                ),
                if (_imageUrlController.text.isNotEmpty)
                  Container(
                    margin: const EdgeInsets.only(top: 12),
                    height: 150,
                    width: double.infinity,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(16),
                      image: DecorationImage(
                        image: NetworkImage(_imageUrlController.text),
                        fit: BoxFit.cover,
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  void _sendNotification() async {
    if (_titleController.text.isEmpty || _bodyController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter both title and body')),
      );
      return;
    }

    final routeTypeText = _routeTypeController.text.trim();
    final routeIdText = _routeIdController.text.trim();
    final extraRouteIdText = _extraRouteIdController.text.trim();
    final minVersionText = _minVersionController.text.trim();

    final routeType = routeTypeText.isEmpty
        ? null
        : int.tryParse(routeTypeText);
    final routeId = routeIdText.isEmpty ? null : int.tryParse(routeIdText);
    final extraRouteId = extraRouteIdText.isEmpty
        ? null
        : int.tryParse(extraRouteIdText);

    final hasInvalidNumber =
        (routeTypeText.isNotEmpty && routeType == null) ||
        (routeIdText.isNotEmpty && routeId == null) ||
        (extraRouteIdText.isNotEmpty && extraRouteId == null);
    if (hasInvalidNumber) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Route Type, Route ID, and Extra Route ID must be valid numbers.',
          ),
        ),
      );
      return;
    }

    setState(() => _isSending = true);
    try {
      await NotificationService.sendNotification(
        title: _titleController.text,
        body: _bodyController.text,
        imageUrl: _imageUrlController.text.isEmpty
            ? null
            : _imageUrlController.text,
        topic: _topicController.text.isEmpty
            ? 'all_users'
            : _topicController.text,
        fcmToken: _fcmTokenController.text.trim().isEmpty
            ? null
            : _fcmTokenController.text.trim(),
        routeType: routeType,
        routeID: routeId,
        extraRouteID: extraRouteId,
        minVersion: minVersionText.isEmpty ? null : minVersionText,
      );
      if (mounted) {
        _showSuccessDialog();
        _clearForm();
      }
    } catch (e) {
      if (mounted) {
        debugPrintStack(stackTrace: StackTrace.current, label: e.toString());
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error: $e'),
            backgroundColor: AppColors.failureRed,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _isSending = false);
    }
  }

  void _showSuccessDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.check_circle_rounded,
              color: AppColors.success,
              size: 64,
            ),
            const SizedBox(height: 24),
            Text(
              'Notification Sent!',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(_resolveSuccessDialogMessage()),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Close'),
            ),
          ],
        ),
      ),
    );
  }

  void _clearForm() {
    _titleController.text = "أذكار اليوم";
    _bodyController.text =
        "🙏 أسْتَغْفِرُ اللهَ العَظِيمَ الَّذِي لاَ إلَهَ إلاَّ هُوَ، الحَيُّ القَيُّومُ، وَأتُوبُ إلَيهِ ";
    _imageUrlController.clear();
    _topicController.clear();
    _fcmTokenController.clear();
    _routeTypeController.clear();
    _routeIdController.clear();
    _extraRouteIdController.clear();
    _minVersionController.clear();
    setState(() {});
  }

  String _resolveSendButtonLabel() {
    if (_fcmTokenController.text.trim().isNotEmpty) {
      return 'Send to Device Token';
    }
    return 'Broadcast Notification';
  }

  String _resolveSuccessDialogMessage() {
    if (_fcmTokenController.text.trim().isNotEmpty) {
      return 'Your notification was sent to the specified FCM token.';
    }
    return 'Your broadcast has been successfully sent to users.';
  }
}
