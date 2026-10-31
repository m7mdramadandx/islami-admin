package com.islami.presentation.notifications

/**
 * Screens a notification can open. Ids mirror `RouteType` in the Islami app
 * (core/domain/model/type/RouteType.kt) and must stay in sync with it.
 */
enum class NotificationRoute(
    val id: Int,
    val label: String,
    val defaultTitle: String? = null,
    val defaultBody: String? = null
) {
    PRAYER_TIME(1, "Prayer times", "تذكير بالصلاة", "حان الآن موعد الصلاة، قم إلى صلاتك 🕌"),
    QURAN(2, "Quran", "آية وتأمل", "اقرأ آية اليوم من القرآن الكريم وتأمل في معانيها 📖"),
    STORIES(3, "Stories", "قصة اليوم", "قصة جديدة من قصص الأنبياء والصحابة بانتظارك ✨"),
    AZKAR(4, "Azkar", "أذكار اليوم", "لا تنسَ وردك اليومي من الأذكار 🤲"),
    SEBHA(5, "Sebha", "سبحة إلكترونية", "سبحان الله وبحمده، سبحان الله العظيم 📿"),
    CALENDAR(6, "Hijri calendar", "التقويم الهجري", "تعرّف على المناسبات الإسلامية القادمة 🗓️"),
    QIBLA(7, "Qibla"),
    FAMILY_TREE(9, "Family tree"),
    MORE(13, "More"),
    RAMADAN(14, "Ramadan", "رمضان كريم", "تابع جدول الإمساكية وأدعية رمضان 🌙"),
    MOSQUES(15, "Nearby mosques", "أقرب مسجد", "اعثر على أقرب مسجد إليك الآن 🕌"),
    AZAN(16, "Azan"),
    QUOTE_OF_DAY(17, "Hadith of the day", "حديث اليوم", "تم إضافة حديث نبوي جديد.. اطلع عليه الآن"),
    DUAS(18, "Duas", "دعاء اليوم", "لا تنسَ أن ترفع يديك بأدعية مختارة تناسب يومك."),
    SETTINGS(19, "Settings"),
    SURAH_KAHF(20, "Surah Al-Kahf", "سورة الكهف", "لا تنسَ قراءة سورة الكهف في يوم الجمعة 🌿");

    val displayName: String get() = "$label ($id)"
}

/** A ready-made notification; [bodies] rotate each time the template is applied. */
data class NotificationTemplate(
    val key: String,
    val emoji: String,
    val title: String,
    val bodies: List<String>,
    val route: NotificationRoute?,
    val extraRouteId: Int = 0
)

val notificationTemplates: List<NotificationTemplate> = listOf(
    NotificationTemplate(
        key = "morning",
        emoji = "☀️",
        title = "أذكار الصباح",
        bodies = listOf(
            "حان الآن موعد أذكار الصباح.. نوّر يومك بذكر الله.",
            "أصبحنا وأصبح الملك لله، لا تنسَ أذكار الصباح ☀️",
            "ابدأ يومك بالتحصين.. أذكار الصباح بانتظارك.",
            "اللهم بك أصبحنا وبك أمسينا وبك نحيا وبك نموت وإليك النشور.",
            "دقائق من الذكر تبارك يومك كله.. افتح أذكار الصباح الآن.",
            "رضيت بالله ربًا وبالإسلام دينًا وبمحمد ﷺ نبيًا 🌤️"
        ),
        route = NotificationRoute.AZKAR
    ),
    NotificationTemplate(
        key = "evening",
        emoji = "🌙",
        title = "أذكار المساء",
        bodies = listOf(
            "حان الآن موعد أذكار المساء.. حصّن نفسك بذكر الله.",
            "أمسينا وأمسى الملك لله، لا تنسَ أذكار المساء 🌙",
            "اختم يومك بالطمأنينة.. أذكار المساء بانتظارك.",
            "بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم.",
            "أعوذ بكلمات الله التامات من شر ما خلق.. حصّن مساءك الآن."
        ),
        route = NotificationRoute.AZKAR
    ),
    NotificationTemplate(
        key = "sleep",
        emoji = "😴",
        title = "أذكار النوم",
        bodies = listOf(
            "باسمك ربي وضعت جنبي وبك أرفعه.. لا تنسَ أذكار النوم.",
            "قبل أن تنام: اقرأ آية الكرسي والمعوذات 🌌",
            "اللهم قني عذابك يوم تبعث عبادك.. تصبح على خير."
        ),
        route = NotificationRoute.AZKAR
    ),
    NotificationTemplate(
        key = "prayer",
        emoji = "🕌",
        title = "تذكير بالصلاة",
        bodies = listOf(
            "قال رسول الله ﷺ: \"أول ما يحاسب عليه العبد يوم القيامة الصلاة\".",
            "حافظوا على الصلوات والصلاة الوسطى.. حان وقت الصلاة 🕌",
            "أرحنا بها يا بلال.. قم إلى صلاتك الآن.",
            "﴿إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا﴾",
            "الصلاة نور.. لا تؤجلها، فهي موعدك مع الله."
        ),
        route = NotificationRoute.PRAYER_TIME
    ),
    NotificationTemplate(
        key = "fajr",
        emoji = "🌅",
        title = "صلاة الفجر",
        bodies = listOf(
            "الصلاة خير من النوم.. قم لصلاة الفجر 🌅",
            "من صلى الصبح فهو في ذمة الله.. لا تفوّت الفجر.",
            "ركعتا الفجر خير من الدنيا وما فيها."
        ),
        route = NotificationRoute.PRAYER_TIME
    ),
    NotificationTemplate(
        key = "qiyam",
        emoji = "✨",
        title = "قيام الليل",
        bodies = listOf(
            "ينزل ربنا في الثلث الأخير من الليل.. هل من داعٍ فأستجيب له؟",
            "ركعتان في جوف الليل.. سرٌّ بينك وبين الله ✨",
            "أفضل الصلاة بعد الفريضة صلاة الليل."
        ),
        route = NotificationRoute.PRAYER_TIME
    ),
    NotificationTemplate(
        key = "hadith",
        emoji = "📜",
        title = "حديث اليوم",
        bodies = listOf(
            "تم إضافة حديث نبوي جديد.. اطلع عليه الآن.",
            "حديث اليوم بانتظارك، تعلّم سنة نبيك ﷺ.",
            "\"إنما الأعمال بالنيات\".. اقرأ شرح حديث اليوم.",
            "\"الكلمة الطيبة صدقة\".. حديث اليوم بانتظارك 📜",
            "\"خيركم من تعلم القرآن وعلمه\".. تعرّف على حديث اليوم."
        ),
        route = NotificationRoute.QUOTE_OF_DAY
    ),
    NotificationTemplate(
        key = "ayah",
        emoji = "📖",
        title = "آية وتأمل",
        bodies = listOf(
            "اقرأ آية اليوم من القرآن الكريم وتأمل في معانيها بتدبر.",
            "﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾ افتح المصحف الآن 📖",
            "وردك اليومي من القرآن ينتظرك، ولو صفحة واحدة.",
            "﴿فَإِنَّ مَعَ الْعُسْرِ يُسْرًا﴾ تأمّل آية اليوم.",
            "﴿وَقُل رَّبِّ زِدْنِي عِلْمًا﴾ خصص دقائق لكتاب الله.",
            "﴿إِنَّ اللَّهَ مَعَ الصَّابِرِينَ﴾ رسالة لقلبك اليوم 🤍"
        ),
        route = NotificationRoute.QURAN
    ),
    NotificationTemplate(
        key = "kahf",
        emoji = "🌿",
        title = "سورة الكهف",
        bodies = listOf(
            "لا تنسَ قراءة سورة الكهف في يوم الجمعة 🌿",
            "نورٌ ما بين الجمعتين.. اقرأ سورة الكهف الآن.",
            "جمعة مباركة.. هل قرأت سورة الكهف اليوم؟"
        ),
        route = NotificationRoute.SURAH_KAHF
    ),
    NotificationTemplate(
        key = "salawat",
        emoji = "💚",
        title = "الصلاة على النبي ﷺ",
        bodies = listOf(
            "أكثروا من الصلاة عليّ يوم الجمعة.. اللهم صلِّ وسلم على نبينا محمد 💚",
            "من صلى عليّ صلاة صلى الله عليه بها عشرًا.",
            "اللهم صلِّ على محمد وعلى آل محمد كما صليت على إبراهيم."
        ),
        route = NotificationRoute.AZKAR
    ),
    NotificationTemplate(
        key = "dua",
        emoji = "🤲",
        title = "دعاء اليوم",
        bodies = listOf(
            "لا تنسَ أن ترفع يديك بأدعية مختارة تناسب يومك.",
            "الدعاء هو العبادة.. اختر دعاء اليوم 🤲",
            "ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة وقنا عذاب النار.",
            "اللهم إني أسألك العفو والعافية في الدنيا والآخرة.",
            "يا مقلب القلوب ثبت قلبي على دينك."
        ),
        route = NotificationRoute.DUAS
    ),
    NotificationTemplate(
        key = "sebha",
        emoji = "📿",
        title = "سبحة إلكترونية",
        bodies = listOf(
            "سبّح الله بقلبك ولسانك.. سبحان الله وبحمده.",
            "كلمتان خفيفتان على اللسان: سبحان الله وبحمده، سبحان الله العظيم 📿",
            "لا حول ولا قوة إلا بالله.. كنز من كنوز الجنة.",
            "سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر.",
            "من قال سبحان الله وبحمده مئة مرة حُطّت خطاياه."
        ),
        route = NotificationRoute.SEBHA
    ),
    NotificationTemplate(
        key = "istighfar",
        emoji = "🙏",
        title = "أذكار اليوم",
        bodies = listOf(
            "أستغفر الله العظيم الذي لا إله إلا هو الحي القيوم وأتوب إليه 🙏",
            "من لزم الاستغفار جعل الله له من كل همٍّ فرجًا.",
            "﴿فَقُلْتُ اسْتَغْفِرُوا رَبَّكُمْ إِنَّهُ كَانَ غَفَّارًا﴾",
            "لا إله إلا أنت سبحانك إني كنت من الظالمين."
        ),
        route = NotificationRoute.AZKAR
    ),
    NotificationTemplate(
        key = "fasting",
        emoji = "🌙",
        title = "صيام التطوع",
        bodies = listOf(
            "غدًا الإثنين.. هل نويت الصيام؟ تُعرض الأعمال على الله.",
            "غدًا الخميس.. اغتنم أجر صيام التطوع 🌙",
            "الأيام البيض تقترب.. تابع التقويم الهجري."
        ),
        route = NotificationRoute.CALENDAR
    ),
    NotificationTemplate(
        key = "stories",
        emoji = "📚",
        title = "قصة اليوم",
        bodies = listOf(
            "قصة جديدة من قصص الأنبياء والصحابة بانتظارك ✨",
            "﴿لَقَدْ كَانَ فِي قَصَصِهِمْ عِبْرَةٌ﴾ اقرأ قصة اليوم.",
            "خذ استراحة قصيرة مع قصة ملهمة من السيرة 📚"
        ),
        route = NotificationRoute.STORIES
    ),
    NotificationTemplate(
        key = "mosques",
        emoji = "📍",
        title = "أقرب مسجد",
        bodies = listOf(
            "اعثر على أقرب مسجد إليك وصلِّ في جماعة 📍",
            "صلاة الجماعة تفضل صلاة الفذ بسبع وعشرين درجة.. ابحث عن أقرب مسجد."
        ),
        route = NotificationRoute.MOSQUES
    )
)
