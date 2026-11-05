package com.islami.domain.ramadan

// Same map as the mobile app's RamadanDayNames.kt, which titles each day's dua screen with it.

private val RAMADAN_DAY_NAMES = mapOf(
    1 to "اليوم الأول",
    2 to "اليوم الثاني",
    3 to "اليوم الثالث",
    4 to "اليوم الرابع",
    5 to "اليوم الخامس",
    6 to "اليوم السادس",
    7 to "اليوم السابع",
    8 to "اليوم الثامن",
    9 to "اليوم التاسع",
    10 to "اليوم العاشر",
    11 to "اليوم الحادي عشر",
    12 to "اليوم الثاني عشر",
    13 to "اليوم الثالث عشر",
    14 to "اليوم الرابع عشر",
    15 to "اليوم الخامس عشر",
    16 to "اليوم السادس عشر",
    17 to "اليوم السابع عشر",
    18 to "اليوم الثامن عشر",
    19 to "اليوم التاسع عشر",
    20 to "اليوم العشرون",
    21 to "اليوم الحادي والعشرون",
    22 to "اليوم الثاني والعشرون",
    23 to "اليوم الثالث والعشرون",
    24 to "اليوم الرابع والعشرون",
    25 to "اليوم الخامس والعشرون",
    26 to "اليوم السادس والعشرون",
    27 to "اليوم السابع والعشرون",
    28 to "اليوم الثامن والعشرون",
    29 to "اليوم التاسع والعشرون",
    30 to "اليوم الثلاثون",
)

/** Title for a day of Ramadan, e.g. 1 -> "اليوم الأول"; null outside 1..30. */
fun ramadanDayName(day: Int): String? = RAMADAN_DAY_NAMES[day]
