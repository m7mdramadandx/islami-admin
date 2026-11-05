package com.islami.domain.ramadan

import com.islami.domain.entities.RamadanDay

data class RamadanIssue(val dayId: String, val isError: Boolean, val message: String)

/** The app shows whatever is stored for the day, so an empty dua is an empty screen. */
fun List<RamadanDay>.validate(): List<RamadanIssue> = flatMap { day ->
    buildList {
        if (day.dua.isBlank()) add(RamadanIssue(day.id, true, "Day ${day.dayNumber} has no dua; the app would show an empty screen"))
        if (day.backgroundUrl.isBlank()) add(RamadanIssue(day.id, false, "Day ${day.dayNumber} has no background image"))
    }
}

/** Ids of days whose content differs from [published]. */
fun List<RamadanDay>.changedFrom(published: List<RamadanDay>): Set<String> {
    val before = published.associateBy { it.id }
    return filter { it != before[it.id] }.map { it.id }.toSet()
}
