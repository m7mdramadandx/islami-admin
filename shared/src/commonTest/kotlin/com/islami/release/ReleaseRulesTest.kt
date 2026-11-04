package com.islami.release

import com.islami.domain.entities.AppReleaseConfig
import com.islami.domain.release.UpdatePrompt
import com.islami.domain.release.compareVersions
import com.islami.domain.release.isValidVersion
import com.islami.domain.release.normalized
import com.islami.domain.release.promptFor
import com.islami.domain.release.raisesMinVersion
import com.islami.domain.release.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReleaseRulesTest {

    private val live = AppReleaseConfig(
        storeVersion = "4.4.1",
        minVersion = "3.20.0",
        releaseNotesAr = listOf("أ"),
        releaseNotesEn = listOf("a")
    )

    @Test
    fun comparesLikeTheMobileApp() {
        assertTrue(compareVersions("3.9.0", "3.20.0") < 0) // numeric, not string, comparison
        assertEquals(0, compareVersions("4.4", "4.4.0"))
        assertTrue(compareVersions("4.10", "4.9.9") > 0)
    }

    @Test
    fun promptsMatchDashboardLogic() {
        assertEquals(UpdatePrompt.FORCE, live.promptFor("3.19.9"))
        assertEquals(UpdatePrompt.SOFT, live.promptFor("3.20.0"))
        assertEquals(UpdatePrompt.SOFT, live.promptFor("4.4.0"))
        assertEquals(UpdatePrompt.NONE, live.promptFor("4.4.1"))
    }

    @Test
    fun liveConfigIsValid() {
        assertTrue(live.validate(live).none { it.isError })
    }

    @Test
    fun minimumAboveStoreIsBlocked() {
        val lockout = live.copy(minVersion = "4.5.0")
        assertTrue(lockout.validate(live).any { it.isError && "locked out" in it.message })
    }

    @Test
    fun malformedVersionsAreBlocked() {
        assertFalse(isValidVersion("4.4."))
        assertFalse(isValidVersion("v4.4.1"))
        assertTrue(live.copy(storeVersion = "").validate(live).any { it.isError })
    }

    @Test
    fun loweringStoreVersionAndStaleNotesOnlyWarn() {
        val lowered = live.copy(storeVersion = "4.4.0").validate(live)
        assertTrue(lowered.none { it.isError })
        assertTrue(lowered.any { "Lower than" in it.message })

        val newRelease = live.copy(storeVersion = "4.5.0").validate(live)
        assertTrue(newRelease.any { "still describe 4.4.1" in it.message })
    }

    @Test
    fun onlyRaisingTheMinimumNeedsExtraConfirmation() {
        assertTrue(live.copy(minVersion = "4.0.0").raisesMinVersion(live))
        assertFalse(live.copy(minVersion = "3.20").raisesMinVersion(live))
        assertFalse(live.copy(minVersion = "3.0.0").raisesMinVersion(live))
    }

    @Test
    fun normalizingTrimsAndDropsBlankNotes() {
        val messy = AppReleaseConfig(" 4.5.0 ", "4.0.0", listOf(" أ ", "", "  "), listOf("a"))
        assertEquals(AppReleaseConfig("4.5.0", "4.0.0", listOf("أ"), listOf("a")), messy.normalized())
    }
}
