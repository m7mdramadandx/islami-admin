/// Preset labels for notification deep links; [id] is sent in FCM `routeID` from the admin panel.
enum NotificationRouteType {
  routeDefault(0, 'Default'),
  prayerTime(1, 'Prayer time'),
  quran(2, 'Quran'),
  stories(3, 'Stories'),
  azkar(4, 'Azkar'),
  sebha(5, 'Sebha'),
  calendar(6, 'Calendar'),
  qibla(7, 'Qibla'),
  familyTree(9, 'Family tree'),
  more(13, 'More'),
  ramadan(14, 'Ramadan'),
  mosques(15, 'Mosques'),
  azan(16, 'Azan'),
  quoteOfDay(17, 'Quote of the day'),
  duas(18, 'Duas'),
  settings(19, 'Settings');

  const NotificationRouteType(this.id, this.label);

  final int id;
  final String label;

  /// Suggested notification title when this route is chosen (Arabic, app-style).
  String get suggestedTitle => switch (this) {
    NotificationRouteType.routeDefault => 'تطبيق إسلامي',
    NotificationRouteType.prayerTime => 'مواقيت الصلاة',
    NotificationRouteType.quran => 'القرآن الكريم',
    NotificationRouteType.stories => 'قصص مفيدة',
    NotificationRouteType.azkar => 'أذكار اليوم',
    NotificationRouteType.sebha => 'المسبحة',
    NotificationRouteType.calendar => 'التقويم الهجري',
    NotificationRouteType.qibla => 'اتجاه القبلة',
    NotificationRouteType.familyTree => 'شجرة العائلة',
    NotificationRouteType.more => 'اكتشف المزيد',
    NotificationRouteType.ramadan => 'رمضان كريم',
    NotificationRouteType.mosques => 'مساجد قريبة',
    NotificationRouteType.azan => 'حان وقت الأذان',
    NotificationRouteType.quoteOfDay => 'اقتباس اليوم',
    NotificationRouteType.duas => 'أدعية مختارة',
    NotificationRouteType.settings => 'الإعدادات',
  };

  /// Suggested notification body when this route is chosen.
  String get suggestedBody => switch (this) {
    NotificationRouteType.routeDefault =>
      'افتح التطبيق وتابع يومك مع الذكر والعبادة.',
    NotificationRouteType.prayerTime =>
      'تعرّف على أوقات الصلوات وتنبيهاتها في منطقتك.',
    NotificationRouteType.quran =>
      'اقرأ وردك من القرآن الكريم وتدبّر آيات الرحمن.',
    NotificationRouteType.stories =>
      'قصة جديدة في انتظارك — عبرة وأمل في بضع دقائق.',
    NotificationRouteType.azkar =>
      'حصّن يومك بأذكار الصباح والمساء وذكر الله.',
    NotificationRouteType.sebha =>
      'خذ لحظة هدوء وسبّح الله بقلبك.',
    NotificationRouteType.calendar =>
      'اطلع على المناسبات والأيام المهمة في التقويم.',
    NotificationRouteType.qibla =>
      'حدّد اتجاه القبلة بسهولة قبل الصلاة.',
    NotificationRouteType.familyTree =>
      'سجّل لحظاتك وروابطك مع العائلة في مكان واحد.',
    NotificationRouteType.more =>
      'استكشف أقسام التطبيق وما الجديد لك.',
    NotificationRouteType.ramadan =>
      'نفحات رمضان: أذكار وأدعية لهذا الشهر الفضيل.',
    NotificationRouteType.mosques =>
      'اعثر على مساجد ومواقيت قريبة منك.',
    NotificationRouteType.azan =>
      'تنبيه: اقترب موعد الصلاة — استعد بوضوء وخشوع.',
    NotificationRouteType.quoteOfDay =>
      'كلمة طيبة قد تصنع فرقاً في يومك — اطلع الآن.',
    NotificationRouteType.duas =>
      'ادعُ الله بأدعية مختارة تناسب يومك.',
    NotificationRouteType.settings =>
      'خصّص تجربتك من الإعدادات والتفضيلات.',
  };
}
