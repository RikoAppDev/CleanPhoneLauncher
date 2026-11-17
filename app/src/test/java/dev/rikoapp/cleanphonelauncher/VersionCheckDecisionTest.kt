package dev.rikoapp.cleanphonelauncher

import dev.rikoapp.cleanphonelauncher.data.updateCheckResult
import dev.rikoapp.cleanphonelauncher.data.versionState
import dev.rikoapp.cleanphonelauncher.domain.UpdateCheckResult
import dev.rikoapp.cleanphonelauncher.presentation.version.VersionState
import org.junit.Assert.assertEquals
import org.junit.Test

class VersionCheckDecisionTest {

    private val url = "store"

    private fun state(min: Long = 0, latest: Long = 0, warned: Long = 0, play: Boolean?) =
        versionState(current = 100, min = min, latest = latest, warned = warned, playHasUpdate = play, storeUrl = url)

    @Test
    fun newerBuild_onlyOnInternalTrack_isNotOffered() {
        assertEquals(VersionState.Ok, state(latest = 101, play = false))
        assertEquals(VersionState.Ok, state(min = 101, latest = 101, play = false))
        assertEquals(
            UpdateCheckResult.UpToDate,
            updateCheckResult(current = 100, latest = 101, playHasUpdate = false, storeUrl = url)
        )
    }

    @Test
    fun newerBuild_thatPlayOffers_isOffered() {
        assertEquals(VersionState.WarnUpgrade(url), state(latest = 101, play = true))
        assertEquals(VersionState.ForceUpgrade(url), state(min = 101, latest = 101, play = true))
        assertEquals(
            UpdateCheckResult.UpdateAvailable(url),
            updateCheckResult(current = 100, latest = 101, playHasUpdate = true, storeUrl = url)
        )
    }

    @Test
    fun whenPlayCannotTell_remoteConfigDecides() {
        assertEquals(VersionState.WarnUpgrade(url), state(latest = 101, play = null))
        assertEquals(VersionState.ForceUpgrade(url), state(min = 101, play = null))
        assertEquals(
            UpdateCheckResult.UpdateAvailable(url),
            updateCheckResult(current = 100, latest = 101, playHasUpdate = null, storeUrl = url)
        )
    }

    @Test
    fun dismissedOrCurrentVersion_staysQuiet() {
        assertEquals(VersionState.Ok, state(latest = 101, warned = 101, play = true))
        assertEquals(VersionState.Ok, state(latest = 100, play = null))
        assertEquals(
            UpdateCheckResult.UpToDate,
            updateCheckResult(current = 100, latest = 100, playHasUpdate = null, storeUrl = url)
        )
    }
}
