package dev.rikoapp.cleanphonelauncher.presentation.applist

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.AppActions
import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.LocalAppOverrideDataSource
import dev.rikoapp.cleanphonelauncher.domain.LocalFavoriteAppDataSource
import dev.rikoapp.cleanphonelauncher.domain.NotificationCountRepository
import dev.rikoapp.cleanphonelauncher.domain.RecentAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.domain.model.FavoriteApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppListViewModel(
    private val context: Application,
    private val installedAppsRepository: InstalledAppsRepository,
    private val recentAppsRepository: RecentAppsRepository,
    private val localFavoriteAppDataSource: LocalFavoriteAppDataSource,
    private val localAppOverrideDataSource: LocalAppOverrideDataSource,
    private val notificationCountRepository: NotificationCountRepository,
    private val appActions: AppActions
) : ViewModel() {

    private val _state = MutableStateFlow(AppListScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                installedAppsRepository.apps,
                localFavoriteAppDataSource.getFavoriteApps(),
                recentAppsRepository.recentApps,
                recentAppsRepository.hasUsageStatsPermission,
                combine(
                    localAppOverrideDataSource.getOverrides(),
                    notificationCountRepository.counts
                ) { overrides, counts -> overrides to counts }
            ) { allApps, favoriteApps, recentApps, hasUsageStatsPermission, overridesAndCounts ->
                val (overrides, notificationCounts) = overridesAndCounts
                val hiddenSet = overrides.filter { it.hidden }.map { it.packageName }.toSet()
                val nameMap = overrides.mapNotNull { o -> o.customName?.let { o.packageName to it } }.toMap()
                fun applyName(app: AppData) =
                    nameMap[app.packageName]?.let { app.copy(name = it) } ?: app

                val renamed = allApps.map(::applyName)
                _state.update {
                    it.copy(
                        allApps = renamed
                            .filter { app -> app.packageName !in hiddenSet }
                            .sortedBy { app -> app.name.uppercase() },
                        hiddenApps = renamed
                            .filter { app -> app.packageName in hiddenSet }
                            .sortedBy { app -> app.name.uppercase() },
                        favoriteAppPackageNames = favoriteApps.map { fav -> fav.packageName }
                            .toSet(),
                        recentApps = recentApps.map(::applyName)
                            .filter { app -> app.packageName !in hiddenSet },
                        hasUsageStatsPermission = hasUsageStatsPermission,
                        notificationCounts = notificationCounts
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            installedAppsRepository.apps.collect { apps ->
                if (apps.isNotEmpty()) {
                    recentAppsRepository.getRecentApps(apps)
                }
            }
        }
    }

    fun onAction(action: AppListScreenAction) {
        when (action) {
            is AppListScreenAction.OnAppClick -> {
                launchApp(action.app)
                recentAppsRepository.getRecentApps(_state.value.allApps)
            }

            is AppListScreenAction.OnAppLongClick -> {
                _state.update { it.copy(showDialogApp = action.app) }
            }

            AppListScreenAction.OnDialogDismiss -> {
                _state.update { it.copy(showDialogApp = null) }
            }

            is AppListScreenAction.OnFavoriteAction -> {
                viewModelScope.launch {
                    if (action.isFavorite) {
                        localFavoriteAppDataSource.deleteFavoriteApp(
                            FavoriteApp(action.app.packageName)
                        )
                    } else {
                        localFavoriteAppDataSource.upsertFavoriteApp(
                            FavoriteApp(action.app.packageName)
                        )
                    }
                    _state.update { it.copy(showDialogApp = null) }
                }
            }

            is AppListScreenAction.OnAppInfoClick -> {
                appActions.openAppInfo(action.app.packageName)
                _state.update { it.copy(showDialogApp = null) }
            }

            is AppListScreenAction.OnUninstallClick -> {
                appActions.requestUninstall(action.app.packageName)
                _state.update { it.copy(showDialogApp = null) }
            }

            is AppListScreenAction.OnHideApp -> {
                viewModelScope.launch {
                    localAppOverrideDataSource.setHidden(action.app.packageName, true)
                    _state.update { it.copy(showDialogApp = null) }
                }
            }

            is AppListScreenAction.OnUnhideApp -> {
                viewModelScope.launch {
                    localAppOverrideDataSource.setHidden(action.app.packageName, false)
                    _state.update { it.copy(showDialogApp = null) }
                }
            }

            is AppListScreenAction.OnRenameClick -> {
                _state.update { it.copy(showDialogApp = null, showRenameApp = action.app) }
            }

            is AppListScreenAction.OnRenameConfirm -> {
                viewModelScope.launch {
                    localAppOverrideDataSource.setCustomName(
                        action.app.packageName,
                        action.newName.trim().ifBlank { null }
                    )
                    _state.update { it.copy(showRenameApp = null) }
                }
            }

            AppListScreenAction.OnRenameDismiss -> {
                _state.update { it.copy(showRenameApp = null) }
            }

            AppListScreenAction.OnGrantPermissionClick -> {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }

            is AppListScreenAction.OnAlphabetScroll -> {
                _state.update { it.copy(searchText = TextFieldState("")) }
            }

            is AppListScreenAction.OnAlphabetClick -> {
                _state.update { it.copy(searchText = TextFieldState("")) }
            }

            is AppListScreenAction.OnSearchDone -> {
                action.appToLaunch?.let { app ->
                    launchApp(app)
                    recentAppsRepository.getRecentApps(_state.value.allApps)
                }
            }

            AppListScreenAction.OnClearSearch -> {
                _state.update { it.copy(searchText = TextFieldState("")) }
            }

            is AppListScreenAction.OnActiveStateChanged -> {
                _state.update {
                    if (action.isActive) {
                        it.copy(isActive = true)
                    } else {
                        it.copy(isActive = false, searchText = TextFieldState(""))
                    }
                }
            }

            AppListScreenAction.OnResume -> {
                recentAppsRepository.checkUsageStatsPermission()
                _state.update { it.copy(searchText = TextFieldState("")) }
            }
        }
    }

    private fun launchApp(app: AppData) {
        if (!appActions.launch(app.packageName)) {
            installedAppsRepository.getInstalledApps()
        }
    }
}
