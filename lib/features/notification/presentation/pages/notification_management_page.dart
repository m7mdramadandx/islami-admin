import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
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
      appBar: AppBar(
        title: const Text('Notifications'),
        centerTitle: true,
        backgroundColor: Colors.white,
        elevation: 0,
      ),
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
              flex: 2,
              child: Container(
                color: Colors.grey.shade50,
                child: Center(child: _buildNotificationPreview()),
              ),
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
          style: GoogleFonts.plusJakartaSans(
            fontSize: 32,
            fontWeight: FontWeight.bold,
            color: AppColors.colorPrimary,
          ),
        ),
        const SizedBox(height: 12),
        Text(
          'Compose and send instant updates to all your users or specific groups.',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 16,
            color: Colors.grey.shade600,
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
            const Icon(
              Icons.auto_awesome_outlined,
              color: AppColors.colorAccent,
              size: 20,
            ),
            const SizedBox(width: 8),
            Text(
              'Quick Templates',
              style: GoogleFonts.plusJakartaSans(
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
              return _buildTemplateCard(template);
            },
          ),
        ),
      ],
    );
  }

  Widget _buildTemplateCard(Map<String, String> template) {
    return Container(
      width: 240,
      margin: const EdgeInsets.only(right: 20),
      child: InkWell(
        onTap: () {
          setState(() {
            _titleController.text = template['title']!;
            _bodyController.text = template['body']!;
          });
        },
        borderRadius: BorderRadius.circular(24),
        child: Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(24),
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
              Container(
                padding: const EdgeInsets.all(10),
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
                style: GoogleFonts.plusJakartaSans(
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
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 12,
                  color: Colors.grey.shade500,
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
    return Container(
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(32),
        border: Border.all(color: Colors.grey.shade100),
      ),
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
                  ? const CircularProgressIndicator(color: Colors.white)
                  : Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.send_rounded, size: 20),
                        const SizedBox(width: 12),
                        Text(
                          'Broadcast Notification',
                          style: GoogleFonts.plusJakartaSans(
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
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String label,
    required String hint,
    required IconData icon,
    int maxLines = 1,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: Colors.grey.shade700,
          ),
        ),
        const SizedBox(height: 10),
        TextField(
          controller: controller,
          maxLines: maxLines,
          onChanged: (_) => setState(() {}),
          decoration: InputDecoration(
            hintText: hint,
            prefixIcon: Icon(icon, size: 20),
            filled: true,
            fillColor: Colors.grey.shade50,
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
          style: GoogleFonts.plusJakartaSans(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: Colors.grey.shade400,
          ),
        ),
        const SizedBox(height: 24),
        Container(
          width: 320,
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(28),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.1),
                blurRadius: 30,
                offset: const Offset(0, 10),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    width: 24,
                    height: 24,
                    decoration: const BoxDecoration(
                      color: AppColors.colorPrimary,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.mosque,
                      color: Colors.white,
                      size: 14,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'ISLAMI APP',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: Colors.grey.shade400,
                      letterSpacing: 1.2,
                    ),
                  ),
                  const Spacer(),
                  Text(
                    'now',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      color: Colors.grey.shade400,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Text(
                _titleController.text.isEmpty
                    ? 'Notification Title'
                    : _titleController.text,
                style: GoogleFonts.plusJakartaSans(
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
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 14,
                  color: Colors.grey.shade600,
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
      );
      if (mounted) {
        _showSuccessDialog();
        _clearForm();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
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
            const Icon(
              Icons.check_circle_rounded,
              color: Colors.green,
              size: 64,
            ),
            const SizedBox(height: 24),
            Text(
              'Notification Sent!',
              style: GoogleFonts.plusJakartaSans(
                fontSize: 22,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            const Text('Your broadcast has been successfully sent to users.'),
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
    setState(() {});
  }
}
