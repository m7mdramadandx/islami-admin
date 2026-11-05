package com.islami.domain.entities

import com.islami.domain.ramadan.ramadanDayName

/** A day in `root/ar/ramadan` (`01`..`30`); the app picks it by position, so days are only edited. */
data class RamadanDay(
    val id: String,
    val dua: String,
    /** Download URL of an image in Storage `islamic_pictures/`, shown behind the dua. */
    val backgroundUrl: String
) {
    /** 1-based day of Ramadan, from the document id. */
    val dayNumber: Int get() = id.toIntOrNull() ?: 0

    /** The title the app shows for this day; it comes from code, not the database. */
    val title: String get() = ramadanDayName(dayNumber) ?: "Day $dayNumber"
}

/** Another admin saved one of the edited days after it was loaded. */
class RamadanConflictException(val days: List<Int>) :
    Exception("Day ${days.joinToString()} changed since you opened it")
