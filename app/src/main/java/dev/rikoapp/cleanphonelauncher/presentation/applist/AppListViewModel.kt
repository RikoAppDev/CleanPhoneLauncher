package dev.rikoapp.cleanphonelauncher.presentation.applist

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.LocalFavoriteAppDataSource
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
    private val localFavoriteAppDataSource: LocalFavoriteAppDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(AppListScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                installedAppsRepository.apps,
                localFavoriteAppDataSource.getFavoriteApps(),
                recentAppsRepository.recentApps,
                recentAppsRepository.hasUsageStatsPermission
            ) { allApps, favoriteApps, recentApps, hasUsageStatsPermission ->
                _state.update {
                    it.copy(
                        allApps = allApps,
                        favoriteAppPackageNames = favoriteApps.map { fav -> fav.packageName }
                            .toSet(),
                        recentApps = recentApps,
                        hasUsageStatsPermission = hasUsageStatsPermission
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
                _state.update { it.copy(isActive = action.isActive) }
            }

            AppListScreenAction.OnResume -> {
                recentAppsRepository.checkUsageStatsPermission()
            }
        }
    }

    private fun launchApp(app: AppData) {
        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}
