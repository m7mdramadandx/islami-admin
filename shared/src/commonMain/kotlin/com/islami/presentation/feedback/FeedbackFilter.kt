package com.islami.presentation.feedback

import com.islami.domain.entities.FeedbackMessage

enum class FeedbackSort(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    APP_VERSION("App version")
}

/** Case-insensitive search across every visible field, then [sort]. */
fun List<FeedbackMessage>.filterAndSort(query: String, sort: FeedbackSort): List<FeedbackMessage> {
    val term = query.trim()
    val matches = if (term.isEmpty()) this else filter { message ->
        with(message) { listOf(msg, email, phone, deviceName, appVersion, date) }
            .any { it.contains(term, ignoreCase = true) }
    }
    return when (sort) {
        FeedbackSort.NEWEST -> matches.sortedWith(dateOrder.reversed())
        FeedbackSort.OLDEST -> matches.sortedWith(dateOrder)
        FeedbackSort.APP_VERSION -> matches.sortedWith(versionOrder.reversed().then(dateOrder.reversed()))
    }
}

/**
 * `date` is stored as a string. Epoch-millis strings compare numerically; anything else
 * compares as text, which matches the Flutter admin's Firestore `orderBy("date")`.
 */
private val dateOrder = Comparator<FeedbackMessage> { a, b ->
    val aMillis = a.date.toLongOrNull()
    val bMillis = b.date.toLongOrNull()
    if (aMillis != null && bMillis != null) aMillis.compareTo(bMillis) else a.date.compareTo(b.date)
}

/** Compares "1.10.2" > "1.9.0" by numeric parts; blank versions sort lowest. */
private val versionOrder = Comparator<FeedbackMessage> { a, b ->
    val aParts = a.appVersion.split('.', '-', '+', ' ').map { it.toIntOrNull() ?: 0 }
    val bParts = b.appVersion.split('.', '-', '+', ' ').map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(aParts.size, bParts.size)) {
        val diff = aParts.getOrElse(i) { 0 }.compareTo(bParts.getOrElse(i) { 0 })
        if (diff != 0) return@Comparator diff
    }
    0
}
