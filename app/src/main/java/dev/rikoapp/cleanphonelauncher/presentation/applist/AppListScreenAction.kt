package dev.rikoapp.cleanphonelauncher.presentation.applist

import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.domain.model.AppShortcut
import dev.rikoapp.cleanphonelauncher.domain.model.Contact

sealed class AppListScreenAction {
    data class OnAppClick(val app: AppData) : AppListScreenAction()
    data class OnAppLongClick(val app: AppData) : AppListScreenAction()
    object OnDialogDismiss : AppListScreenAction()
    data class OnShortcutClick(val shortcut: AppShortcut) : AppListScreenAction()
    data class OnSearchQueryChanged(val query: String) : AppListScreenAction()
    data class OnContactClick(val contact: Contact) : AppListScreenAction()
    data class OnFavoriteAction(val app: AppData, val isFavorite: Boolean) : AppListScreenAction()
    data class OnAppInfoClick(val app: AppData) : AppListScreenAction()
    data class OnUninstallClick(val app: AppData) : AppListScreenAction()
    data class OnHideApp(val app: AppData) : AppListScreenAction()
    data class OnUnhideApp(val app: AppData) : AppListScreenAction()
    data class OnRenameClick(val app: AppData) : AppListScreenAction()
    data class OnRenameConfirm(val app: AppData, val newName: String) : AppListScreenAction()
    object OnRenameDismiss : AppListScreenAction()
    object OnGrantPermissionClick : AppListScreenAction()
    data class OnAlphabetScroll(val letter: Char) : AppListScreenAction()
    data class OnAlphabetClick(val letter: Char) : AppListScreenAction()
    data class OnSearchDone(val appToLaunch: AppData?, val query: String = "") : AppListScreenAction()
    object OnClearSearch : AppListScreenAction()
    data class OnActiveStateChanged(val isActive: Boolean) : AppListScreenAction()
    object OnResume : AppListScreenAction()
}
