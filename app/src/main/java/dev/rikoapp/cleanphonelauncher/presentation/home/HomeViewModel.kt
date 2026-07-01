package dev.rikoapp.cleanphonelauncher.presentation.home

import android.app.Application
import android.content.Intent
import android.provider.AlarmClock
import androidx.lifecycle.ViewModel
import dev.rikoapp.cleanphonelauncher.LockAccessibilityService
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.AppActions
import dev.rikoapp.cleanphonelauncher.domain.ClockRepository
import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.LocalFavoriteAppDataSource
import dev.rikoapp.cleanphonelauncher.domain.NotificationCountRepository
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.domain.model.FavoriteApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val context: Application,
    private val installedAppsRepository: InstalledAppsRepository,
    private val clockRepository: ClockRepository,
    private val localFavoriteAppDataSource: LocalFavoriteAppDataSource,
    private val notificationCountRepository: NotificationCountRepository,
    private val settingsRepository: SettingsRepository,
    private val appActions: AppActions
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Combine the first 3 flows related to apps
            val appFlows = combine(
                installedAppsRepository.apps,
                installedAppsRepository.phoneApp,
                installedAppsRepository.cameraApp
            ) { allApps, phoneApp, cameraApp ->
                Triple(allApps, phoneApp, cameraApp)
            }

            // Combine the other 3 flows
            val otherFlows = combine(
                clockRepository.clockType,
                clockRepository.batteryLevel,
                localFavoriteAppDataSource.getFavoriteApps()
            ) { clockType, batteryLevel, favoriteApps ->
                Triple(clockType, batteryLevel, favoriteApps)
            }

            val gestureFlows = combine(
                settingsRepository.swipeUpAction,
                settingsRepository.swipeDownAction,
                settingsRepository.doubleTapAction
            ) { swipeUp, swipeDown, doubleTap ->
                Triple(swipeUp, swipeDown, doubleTap)
            }

            val quickFlows = combine(
                settingsRepository.quickActions,
                settingsRepository.quickActionsConfigured
            ) { packages, configured -> packages to configured }

            // Now, combine the results of the combined flows with notification counts and gestures
            combine(
                appFlows,
                otherFlows,
                notificationCountRepository.counts,
                gestureFlows,
                quickFlows
            ) { appData, otherData, notificationCounts, gestures, quickData ->
                val (allApps, phoneApp, cameraApp) = appData
                val (clockType, batteryLevel, favoriteApps) = otherData
                val (swipeUp, swipeDown, doubleTap) = gestures
                val (quickActionPackages, quickConfigured) = quickData

                val favoriteAppsData = favoriteApps.mapNotNull { favoriteApp ->
                    allApps.find { it.packageName == favoriteApp.packageName }
                }

                // Once the user has set quick actions (even to empty) we honour that;
                // only the unconfigured first-run default falls back to phone + camera.
                val quickPackages = if (quickConfigured) {
                    quickActionPackages
                } else {
                    listOfNotNull(phoneApp?.packageName, cameraApp?.packageName)
                }
                val quickActions = quickPackages.mapNotNull { pkg ->
                    allApps.find { it.packageName == pkg }
                }

                _state.update {
                    it.copy(
                        allApps = allApps,
                        phoneApp = phoneApp,
                        cameraApp = cameraApp,
                        clockType = clockType,
                        batteryLevel = batteryLevel,
                        favoriteAppsData = favoriteAppsData,
                        notificationCounts = notificationCounts,
                        swipeUpAction = swipeUp,
                        swipeDownAction = swipeDown,
                        doubleTapAction = doubleTap,
                        quickActions = quickActions
                    )
                }
            }.collect()
        }
    }

    fun onAction(action: HomeScreenAction) {
        when (action) {
            HomeScreenAction.OnClockClick -> {
                val pm = context.packageManager
                val clockIntent = listOf(
                    "com.google.android.deskclock",
                    "com.sec.android.app.clockpackage",
                    "com.sonymobile.digitalclock",
                    "com.htc.android.worldclock",
                    "com.motorola.blur.alarmclock",
                    "com.lge.clock",
                    "com.android.deskclock"
                ).firstNotNullOfOrNull { pkgName ->
                    pm.getLaunchIntentForPackage(pkgName)
                } ?: Intent(AlarmClock.ACTION_SHOW_ALARMS)

                try {
                    clockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(clockIntent)
                } catch (_: Exception) {
                    val fallbackIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                }
            }

            HomeScreenAction.OnClockLongClick -> {
                _state.update { it.copy(showClockTypeDialog = true) }
            }

            HomeScreenAction.OnClockTypeDialogDismiss -> {
                _state.update { it.copy(showClockTypeDialog = false) }
            }

            is HomeScreenAction.OnClockTypeConfirm -> {
                clockRepository.setClockType(action.selectedType)
                _state.update { it.copy(showClockTypeDialog = false) }
            }

            is HomeScreenAction.OnFavoriteAppClick -> {
                launchApp(action.app)
            }

            is HomeScreenAction.OnFavoriteAppLongClick -> {
                _state.update { it.copy(showDialogApp = action.app) }
            }

            HomeScreenAction.OnFavoriteDialogDismiss -> {
                _state.update { it.copy(showDialogApp = null) }
            }

            is HomeScreenAction.OnRemoveFavorite -> {
                viewModelScope.launch {
                    localFavoriteAppDataSource.deleteFavoriteApp(FavoriteApp(action.packageName))
                    _state.update { it.copy(showDialogApp = null) }
                }
            }

            is HomeScreenAction.OnReorderFavorites -> {
                viewModelScope.launch {
                    localFavoriteAppDataSource.reorderFavoriteApps(action.orderedPackageNames)
                }
            }

            is HomeScreenAction.OnAppInfoClick -> {
                appActions.openAppInfo(action.app.packageName)
                _state.update { it.copy(showDialogApp = null) }
            }

            is HomeScreenAction.OnUninstallClick -> {
                appActions.requestUninstall(action.app.packageName)
                _state.update { it.copy(showDialogApp = null) }
            }

            is HomeScreenAction.OnPhoneAppClick -> {
                launchApp(action.app)
            }

            is HomeScreenAction.OnCameraAppClick -> {
                launchApp(action.app)
            }

            is HomeScreenAction.OnQuickActionClick -> {
                launchApp(action.app)
            }

            is HomeScreenAction.OnQuickActionSet -> {
                val current = currentQuickPackages().toMutableList()
                if (action.index in current.indices) {
                    current[action.index] = action.packageName
                    settingsRepository.setQuickActions(current)
                }
            }

            is HomeScreenAction.OnQuickActionAdd -> {
                val current = currentQuickPackages()
                if (current.size < MAX_QUICK_ACTIONS && action.packageName !in current) {
                    settingsRepository.setQuickActions(current + action.packageName)
                }
            }

            is HomeScreenAction.OnQuickActionRemove -> {
                val current = currentQuickPackages().toMutableList()
                if (current.size > MIN_QUICK_ACTIONS && action.index in current.indices) {
                    current.removeAt(action.index)
                    settingsRepository.setQuickActions(current)
                }
            }

            HomeScreenAction.OnLockScreen -> {
                lockScreen()
            }

            HomeScreenAction.OnExpandNotifications -> {
                expandNotificationsPanel()
            }

            HomeScreenAction.OnAccessibilityDisclosureDismiss -> {
                _state.update { it.copy(showAccessibilityDisclosure = false) }
            }
        }
    }

    private fun expandNotificationsPanel() {
        runCatching {
            val service = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            statusBarManager.getMethod("expandNotificationsPanel").invoke(service)
        }
    }

    private fun lockScreen() {
        if (!LockAccessibilityService.lockScreen()) {
            _state.update { it.copy(showAccessibilityDisclosure = true) }
        }
    }

    private fun currentQuickPackages(): List<String> =
        _state.value.quickActions.map { it.packageName }

    private fun launchApp(app: AppData) {
        if (!appActions.launch(app.packageName)) {
            installedAppsRepository.getInstalledApps()
        }
    }

    companion object {
        const val MIN_QUICK_ACTIONS = 0
        const val MAX_QUICK_ACTIONS = 5
    }
}
