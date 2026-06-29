package dev.rikoapp.cleanphonelauncher.presentation.home

import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.presentation.model.ClockType

data class HomeScreenState(
    val allApps: List<AppData> = emptyList(),
    val phoneApp: AppData? = null,
    val cameraApp: AppData? = null,
    val clockType: ClockType = ClockType.ANALOG_WITH_SECONDS, // Default value
    val batteryLevel: Int = 0,
    val showClockTypeDialog: Boolean = false,
    val favoriteAppsData: List<AppData> = emptyList(), // Resolved favorite apps
    val showDialogApp: AppData? = null,
    val notificationCounts: Map<String, Int> = emptyMap(),
    val requestDeviceAdmin: Boolean = false
)
