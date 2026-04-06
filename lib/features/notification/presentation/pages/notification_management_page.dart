import 'dart:math';

import 'package:flutter/material.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/notification/data/services/notification_service.dart';
import 'package:islami_admin/features/notification/domain/notification_route_type.dart';

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
  final _topicController = TextEditingController(text: "general");
  final _fcmTokenController = TextEditingController();
  final _extraRouteIdController = TextEditingController();
  final _minVersionController = TextEditingController();

  final List<Map<String, String>> _templates = [
    {
      'title': 'أذكار الصباح',
      'body': 'حان الآن موعد أذكار الصباح.. نور يومك بذكر الله.',
      'icon': 'wb_sunny',
      'routeId': '4',
    },
    {
      'title': 'أذكار المساء',
      'body': 'حان الآن موعد أذكار المساء.. حصن نفسك بذكر الله.',
      'icon': 'nightlight_round',
      'routeId': '4',
    },
    {
      'title': 'تذكير بالصلاة',
      'body': 'قال رسول الله ﷺ: "أول ما يحاسب عليه العبد يوم القيامة الصلاة".',
      'icon': 'mosque',
      'routeId': '1',
    },
    {
      'title': 'حديث اليوم',
      'body': 'تم إضافة حديث نبوي جديد.. اطلع عليه الآن.',
      'icon': 'menu_book',
      'routeId': '3',
    },
    {
      'title': 'آية وتأمل',
      'body': 'اقرأ آية اليوم من القرآن الكريم وتمهل في تدبر معانيها.',
      'icon': 'auto_stories',
      'routeId': '2',
    },
    {
      'title': 'دعاء اليوم',
      'body': 'لا تنسَ أن ترفع يديك بأدعية مختارة تناسب يومك.',
      'icon': 'volunteer_activism',
      'routeId': '18',
    },
    {
      'title': 'سبحة إلكترونية',
      'body': 'خذ لحظة هدوء.. سبّح الله بقلبك ولسانك.',
      'icon': 'blur_circular',
      'routeId': '5',
    },
    {
      'title': 'قصة مؤثرة',
      'body': 'قصة جديدة في انتظارك — استمد العبرة والأمل.',
      'icon': 'auto_stories',
      'routeId': '3',
    },
    {
      'title': 'اقتباس اليوم',
      'body': 'كلمة طيبة قد تصنع فرقاً في يومك.. اطلع على اقتباس اليوم.',
      'icon': 'format_quote',
      'routeId': '17',
    },
    {
      'title': 'التقويم الهجري',
      'body': 'تعرف على المناسبات والأيام المهمة في التقويم.',
      'icon': 'calendar_today',
      'routeId': '6',
    },
    {
      'title': 'اتجاه القبلة',
      'body': 'حدّث اتجاه القبلة قبل الصلاة بسهولة.',
      'icon': 'explore',
      'routeId': '7',
    },
    {
      'title': 'شجرة العائلة',
      'body': 'سجّل ذكرى أو صلة رحم — شارك لحظة مع أحبائك.',
      'icon': 'family_restroom',
      'routeId': '9',
    },
    {
      'title': 'رمضان كريم',
      'body': 'نفحات رمضان: تزود بالأدعية والأذكار في هذا الشهر الفضيل.',
      'icon': 'dark_mode',
      'routeId': '14',
    },
    {
      'title': 'مساجد قريبة',
      'body': 'اكتشف مساجد ومواقيت حولك لتسهيل حضور الجماعة.',
      'icon': 'location_city',
      'routeId': '15',
    },
    {
      'title': 'حان وقت الأذان',
      'body': 'تنبيه: اقترب موعد الصلاة — استعد بوضوء وخشوع.',
      'icon': 'volume_up',
      'routeId': '16',
    },
    {
      'title': 'تحديث التطبيق',
      'body': 'تحسينات جديدة في انتظارك! حدّث للحصول على أفضل تجربة.',
      'icon': 'system_update',
      'routeId': '19',
    },
    {
      'title': 'المزيد من المحتوى',
      'body': 'استكشف أقسام التطبيق واكتشف ما الجديد.',
      'icon': 'more_horiz',
      'routeId': '13',
    },
    {
      'title': 'تنبيه لطيف 🌿',
      'body': 'لحظة من السكينة: سبحان الله، والحمد لله، ولا إله إلا الله.',
      'icon': 'spa',
      'routeId': '4',
    },
    {
      'title': 'وقفة مع القرآن',
      'body': 'وردك اليومي من القرآن ينتظرك — وردة خير من الدنيا وما فيها.',
      'icon': 'menu_book',
      'routeId': '2',
    },
    {
      'title': 'مساء الخير',
      'body': 'أمسيتم بخير — اختموا يومكم بذكر ودعاء.',
      'icon': 'nights_stay',
      'routeId': '0',
    },
    // يوم الجمعة — Jumu'ah defaults
    {
      'title': 'جمعة مباركة',
      'body':
          'تقبّل الله منا ومنكم صالح الأعمال، وجمعة مباركة ملؤها السكينة والبركة.',
      'icon': 'mosque',
      'routeId': '1',
    },
    {
      'title': 'تذكير: صلاة الجمعة',
      'body':
          'اقترب موعد الجمعة — تيَمَّم بالوضوء المبكر والذهاب إلى المسجد قبل الخطبة.',
      'icon': 'today',
      'routeId': '15',
    },
    {
      'title': 'سورة الكهف',
      'body':
          'لا تنسَ قراءة سورة الكهف اليوم؛ نورٌ بين الجمعتين بإذن الله تعالى.',
      'icon': 'menu_book',
      'routeId': '2',
    },
    {
      'title': 'ساعة استجابة يوم الجمعة',
      'body':
          'من فضل يوم الجمعة ساعة لا يوافقها عبد مسلم فيسأل الله خيراً إلا أعطاه — اغتنم الدعاء.',
      'icon': 'volunteer_activism',
      'routeId': '18',
    },
    {
      'title': 'الصلاة على النبي ﷺ',
      'body':
          'أكثروا من الصلاة عليه يوم الجمعة؛ فإن صلاتكم معروضة عليه.',
      'icon': 'spa',
      'routeId': '4',
    },
    {
      'title': 'خير يوم الجمعة',
      'body':
          'خير يوم طلعت فيه الشمس يوم الجمعة — فيه ساعة لا يوافقها عبد مؤمن يدعو الله إلا استجاب له.',
      'icon': 'format_quote',
      'routeId': '1',
    },
  ];

  final Random _random = Random();

  bool _isSending = false;

  /// Preset for FCM `routeID` (labels match app sections; value = id).
  NotificationRouteType? _selectedRouteIdPreset;

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
          style: TextStyle(fontSize: 16, color: AppColors.subText),
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
            Expanded(
              child: Text(
                'Quick Templates',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: AppColors.colorPrimary,
                ),
              ),
            ),
            FilledButton.tonalIcon(
              onPressed: _pickRandomTemplate,
              icon: const Icon(Icons.shuffle_rounded, size: 20),
              label: const Text('Random'),
              style: FilledButton.styleFrom(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 10,
                ),
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
              return SizedBox(width: 240, child: _buildTemplateCard(template));
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
        onTap: () => _applyTemplate(template),
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

  void _applyTemplate(Map<String, String> template) {
    setState(() {
      _titleController.text = template['title']!;
      _bodyController.text = template['body']!;
      _selectedRouteIdPreset = _presetFromRouteIdString(template['routeId']);
    });
  }

  void _pickRandomTemplate() {
    _applyTemplate(_templates[_random.nextInt(_templates.length)]);
  }

  NotificationRouteType? _presetFromRouteIdString(String? raw) {
    if (raw == null || raw.isEmpty) return null;
    final id = int.tryParse(raw);
    if (id == null) return null;
    for (final e in NotificationRouteType.values) {
      if (e.id == id) return e;
    }
    return null;
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
      case 'auto_stories':
        return Icons.auto_stories_rounded;
      case 'volunteer_activism':
        return Icons.volunteer_activism_rounded;
      case 'blur_circular':
        return Icons.blur_circular_rounded;
      case 'format_quote':
        return Icons.format_quote_rounded;
      case 'calendar_today':
        return Icons.calendar_today_rounded;
      case 'explore':
        return Icons.explore_rounded;
      case 'family_restroom':
        return Icons.family_restroom_rounded;
      case 'dark_mode':
        return Icons.dark_mode_rounded;
      case 'location_city':
        return Icons.location_city_rounded;
      case 'volume_up':
        return Icons.volume_up_rounded;
      case 'system_update':
        return Icons.system_update_rounded;
      case 'more_horiz':
        return Icons.more_horiz_rounded;
      case 'spa':
        return Icons.spa_rounded;
      case 'nights_stay':
        return Icons.nights_stay_rounded;
      case 'today':
        return Icons.today_rounded;
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
              style: TextStyle(fontSize: 12, color: AppColors.subText),
            ),
            const SizedBox(height: 24),
            _buildRouteIdDropdown(),
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
              style: TextStyle(fontSize: 12, color: AppColors.subText),
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

  Widget _buildRouteIdDropdown() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Route ID (optional)',
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: AppColors.text,
          ),
        ),
        const SizedBox(height: 10),
        InputDecorator(
          decoration: InputDecoration(
            hintText: 'Not set',
            prefixIcon: Icon(Icons.numbers_rounded, size: 20),
            filled: true,
            contentPadding: const EdgeInsets.symmetric(horizontal: 12),
          ),
          child: DropdownButtonHideUnderline(
            child: DropdownButton<NotificationRouteType?>(
              value: _selectedRouteIdPreset,
              isExpanded: true,
              hint: Text('Not set', style: TextStyle(color: AppColors.subText)),
              items: [
                const DropdownMenuItem<NotificationRouteType?>(
                  value: null,
                  child: Text('Not set'),
                ),
                ...NotificationRouteType.values.map(
                  (e) => DropdownMenuItem<NotificationRouteType?>(
                    value: e,
                    child: Text(
                      '${e.label} (routeID: ${e.id})',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ),
              ],
              onChanged: (value) {
                setState(() {
                  _selectedRouteIdPreset = value;
                  if (value != null) {
                    _titleController.text = value.suggestedTitle;
                    _bodyController.text = value.suggestedBody;
                  }
                });
              },
            ),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'Choosing a route fills the title and message above; you can still edit them.',
          style: TextStyle(fontSize: 12, color: AppColors.subText),
        ),
      ],
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
                      style: TextStyle(fontSize: 12, color: AppColors.grey),
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

    final extraRouteIdText = _extraRouteIdController.text.trim();
    final minVersionText = _minVersionController.text.trim();

    final routeId = _selectedRouteIdPreset?.id;
    final extraRouteId = extraRouteIdText.isEmpty
        ? null
        : int.tryParse(extraRouteIdText);

    if (extraRouteIdText.isNotEmpty && extraRouteId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Extra Route ID must be a valid number.')),
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
        topic: _topicController.text.isEmpty ? null : _topicController.text,
        fcmToken: _fcmTokenController.text.trim().isEmpty
            ? null
            : _fcmTokenController.text.trim(),
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
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
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
    _selectedRouteIdPreset = null;
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
