package com.islami.domain.release

import com.islami.domain.entities.AppReleaseConfig

private val VERSION_PATTERN = Regex("\\d+(\\.\\d+)*")

fun isValidVersion(version: String): Boolean = VERSION_PATTERN.matches(version)

/**
 * Same comparison as the mobile app's `StringUtils.compareVersions`: dot-separated parts compared
 * as numbers, missing or non-numeric parts count as 0 (so "4.4" == "4.4.0").
 */
fun compareVersions(a: String, b: String): Int {
    val left = a.split(".").map { it.toIntOrNull() ?: 0 }
    val right = b.split(".").map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(left.size, right.size)) {
        val diff = left.getOrElse(i) { 0 }.compareTo(right.getOrElse(i) { 0 })
        if (diff != 0) return diff
    }
    return 0
}

enum class UpdatePrompt { FORCE, SOFT, NONE }

/** What an app on [appVersion] shows on launch, mirroring `DashboardViewModel.loadAppVersion`. */
fun AppReleaseConfig.promptFor(appVersion: String): UpdatePrompt = when {
    compareVersions(appVersion, minVersion) < 0 -> UpdatePrompt.FORCE
    compareVersions(appVersion, storeVersion) < 0 -> UpdatePrompt.SOFT
    else -> UpdatePrompt.NONE
}

enum class ReleaseField { STORE_VERSION, MIN_VERSION, NOTES_AR, NOTES_EN }

data class ReleaseIssue(val field: ReleaseField, val isError: Boolean, val message: String)

/** Trims versions and drops blank notes, the form that is actually written. */
fun AppReleaseConfig.normalized() = AppReleaseConfig(
    storeVersion = storeVersion.trim(),
    minVersion = minVersion.trim(),
    releaseNotesAr = releaseNotesAr.map { it.trim() }.filter { it.isNotEmpty() },
    releaseNotesEn = releaseNotesEn.map { it.trim() }.filter { it.isNotEmpty() }
)

fun AppReleaseConfig.validate(published: AppReleaseConfig?): List<ReleaseIssue> = buildList {
    val config = normalized()
    val storeValid = isValidVersion(config.storeVersion)
    val minValid = isValidVersion(config.minVersion)

    if (!storeValid) add(ReleaseIssue(ReleaseField.STORE_VERSION, true, "Use numbers and dots, like 4.4.1"))
    if (!minValid) add(ReleaseIssue(ReleaseField.MIN_VERSION, true, "Use numbers and dots, like 3.20.0"))

    if (storeValid && minValid && compareVersions(config.minVersion, config.storeVersion) > 0) {
        // Users on the newest release would be told to update to a version that doesn't exist.
        add(
            ReleaseIssue(
                ReleaseField.MIN_VERSION, true,
                "Can't be above the store version (${config.storeVersion}), or everyone gets locked out"
            )
        )
    }

    if (published != null && storeValid && isValidVersion(published.storeVersion) &&
        compareVersions(config.storeVersion, published.storeVersion) < 0
    ) {
        add(
            ReleaseIssue(
                ReleaseField.STORE_VERSION, false,
                "Lower than the live ${published.storeVersion}; apps on newer versions won't be prompted"
            )
        )
    }

    val notesUnchanged = published != null &&
        config.releaseNotesAr == published.releaseNotesAr && config.releaseNotesEn == published.releaseNotesEn
    if (published != null && notesUnchanged && storeValid &&
        compareVersions(config.storeVersion, published.storeVersion) > 0
    ) {
        add(
            ReleaseIssue(
                ReleaseField.NOTES_EN, false,
                "Release notes still describe ${published.storeVersion}"
            )
        )
    }
    if (config.releaseNotesAr.isEmpty() != config.releaseNotesEn.isEmpty()) {
        val missing = if (config.releaseNotesAr.isEmpty()) ReleaseField.NOTES_AR else ReleaseField.NOTES_EN
        add(ReleaseIssue(missing, false, "Readers in this language will see no release notes"))
    } else if (config.releaseNotesAr.size != config.releaseNotesEn.size) {
        add(
            ReleaseIssue(
                ReleaseField.NOTES_EN, false,
                "Arabic has ${config.releaseNotesAr.size} notes and English has ${config.releaseNotesEn.size}"
            )
        )
    }
}

/** Raising the minimum blocks real users, so the UI asks for it to be typed out. */
fun AppReleaseConfig.raisesMinVersion(published: AppReleaseConfig?): Boolean {
    val min = minVersion.trim()
    val old = published?.minVersion?.trim().orEmpty()
    return isValidVersion(min) && (!isValidVersion(old) || compareVersions(min, old) > 0)
}
