package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.presentation.version.VersionState

interface VersionCheckRepository {
    suspend fun check(): VersionState
    fun dismissWarn()
}
