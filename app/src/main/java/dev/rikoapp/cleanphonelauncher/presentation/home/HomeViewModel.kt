package dev.rikoapp.cleanphonelauncher.presentation.home

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.lifecycle.ViewModel
import dev.rikoapp.cleanphonelauncher.LockDeviceAdminReceiver
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.AppActions
import dev.rikoapp.cleanphonelauncher.domain.ClockRepository
import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.LocalFavoriteAppDataSource
import dev.rikoapp.cleanphonelauncher.domain.NotificationCountRepository
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

            // Now, combine the results of the two combined flows with notification counts
            combine(
                appFlows,
                otherFlows,
                notificationCountRepository.counts
            ) { appData, otherData, notificationCounts ->
                val (allApps, phoneApp, cameraApp) = appData
                val (clockType, batteryLevel, favoriteApps) = otherData

                val favoriteAppsData = favoriteApps.mapNotNull { favoriteApp ->
                    allApps.find { it.packageName == favoriteApp.packageName }
                }

                _state.update {
                    it.copy(
                        allApps = allApps,
                        phoneApp = phoneApp,
                        cameraApp = cameraApp,
                        clockType = clockType,
                        batteryLevel = batteryLevel,
                        favoriteAppsData = favoriteAppsData,
                        notificationCounts = notificationCounts
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

            HomeScreenAction.OnDoubleTapHome -> {
                lockScreen()
            }
        }
    }

    private fun lockScreen() {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, LockDeviceAdminReceiver::class.java)
        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                .putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    context.getString(dev.rikoapp.cleanphonelauncher.R.string.device_admin_explanation)
                )
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    private fun launchApp(app: AppData) {
        if (!appActions.launch(app.packageName)) {
            installedAppsRepository.getInstalledApps()
        }
    }
}
