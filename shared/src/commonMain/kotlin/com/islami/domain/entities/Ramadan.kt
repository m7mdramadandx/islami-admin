package com.islami.domain.entities

import com.islami.domain.ramadan.ramadanDayName

/**
 * One document in `root/ar/ramadan` (ids `01`..`30`). The app loads the collection in id order and
 * shows the entry at index (Hijri day - 1) on its Ramadan duas screen, so documents are only ever
 * edited, never added, removed or renamed.
 */
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

data class RamadanBackground(
    /** Storage path, e.g. `islamic_pictures/0c73e1c5....jpg`. */
    val path: String,
    val url: String
)

/** Another admin saved one of the edited days after it was loaded. */
class RamadanConflictException(val days: List<Int>) :
    Exception("Day ${days.joinToString()} changed since you opened it")
