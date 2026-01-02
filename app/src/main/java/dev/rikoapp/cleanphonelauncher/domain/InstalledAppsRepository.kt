package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import kotlinx.coroutines.flow.StateFlow

interface InstalledAppsRepository {
    val apps: StateFlow<List<AppData>>
    val phoneApp: StateFlow<AppData?>
    val cameraApp: StateFlow<AppData?>

    fun getInstalledApps()
    fun findCoreApps()
}