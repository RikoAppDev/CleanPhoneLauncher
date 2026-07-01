package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.presentation.version.VersionState

interface VersionCheckRepository {
    suspend fun check(): VersionState

    /** Force an immediate Remote Config fetch (ignoring the throttle) for a manual check. */
    suspend fun forceCheck(): UpdateCheckResult
    fun dismissWarn()
}

sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult
    data object Error : UpdateCheckResult
    data class UpdateAvailable(val storeUrl: String) : UpdateCheckResult
}
