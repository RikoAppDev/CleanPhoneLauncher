package dev.rikoapp.cleanphonelauncher.presentation.home

import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.presentation.model.ClockType

sealed class HomeScreenAction {
    object OnClockClick : HomeScreenAction()
    object OnClockLongClick : HomeScreenAction()
    object OnClockTypeDialogDismiss : HomeScreenAction()
    data class OnClockTypeConfirm(val selectedType: ClockType) : HomeScreenAction()
    data class OnFavoriteAppClick(val app: AppData) : HomeScreenAction()
    data class OnFavoriteAppLongClick(val app: AppData) : HomeScreenAction()
    object OnFavoriteDialogDismiss : HomeScreenAction()
    data class OnRemoveFavorite(val packageName: String) : HomeScreenAction()
    data class OnReorderFavorites(val orderedPackageNames: List<String>) : HomeScreenAction()
    data class OnAppInfoClick(val app: AppData) : HomeScreenAction()
    data class OnUninstallClick(val app: AppData) : HomeScreenAction()
    data class OnPhoneAppClick(val app: AppData) : HomeScreenAction()
    data class OnCameraAppClick(val app: AppData) : HomeScreenAction()
    object OnDoubleTapHome : HomeScreenAction()
    object OnSwipeDownHome : HomeScreenAction()
    object OnAccessibilityDisclosureDismiss : HomeScreenAction()
}
