package dev.rikoapp.cleanphonelauncher.presentation.applist

import dev.rikoapp.cleanphonelauncher.domain.model.AppData

sealed class AppListScreenAction {
    data class OnAppClick(val app: AppData) : AppListScreenAction()
    data class OnAppLongClick(val app: AppData) : AppListScreenAction()
    object OnDialogDismiss : AppListScreenAction()
    data class OnFavoriteAction(val app: AppData, val isFavorite: Boolean) : AppListScreenAction()
    object OnGrantPermissionClick : AppListScreenAction()
    data class OnAlphabetScroll(val letter: Char) : AppListScreenAction()
    data class OnAlphabetClick(val letter: Char) : AppListScreenAction()
    data class OnSearchDone(val appToLaunch: AppData?) : AppListScreenAction()
    object OnClearSearch : AppListScreenAction()
    data class OnActiveStateChanged(val isActive: Boolean) : AppListScreenAction()
    object OnResume : AppListScreenAction()
}
