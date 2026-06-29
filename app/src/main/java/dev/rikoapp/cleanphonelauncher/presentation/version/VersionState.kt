package dev.rikoapp.cleanphonelauncher.presentation.version

sealed interface VersionState {
    data object Ok : VersionState
    data class ForceUpgrade(val storeUrl: String) : VersionState
    data class WarnUpgrade(val storeUrl: String) : VersionState
}
