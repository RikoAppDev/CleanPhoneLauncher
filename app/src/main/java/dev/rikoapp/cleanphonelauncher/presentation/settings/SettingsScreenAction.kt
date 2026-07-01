package dev.rikoapp.cleanphonelauncher.presentation.settings

import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.GestureAction
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode

sealed class SettingsScreenAction {
    data class OnThemeModeSelected(val mode: ThemeMode) : SettingsScreenAction()
    data class OnColorStyleSelected(val style: AppColorStyle) : SettingsScreenAction()
    data class OnAccentColorSelected(val color: Int) : SettingsScreenAction()
    data class OnCrashReportingToggled(val enabled: Boolean) : SettingsScreenAction()
    data class OnUnhideApp(val packageName: String) : SettingsScreenAction()
    data object OnRerunSetup : SettingsScreenAction()
    data class OnSwipeUpActionSelected(val action: GestureAction) : SettingsScreenAction()
    data class OnSwipeDownActionSelected(val action: GestureAction) : SettingsScreenAction()
    data class OnDoubleTapActionSelected(val action: GestureAction) : SettingsScreenAction()
    data class OnContactsSearchToggled(val enabled: Boolean) : SettingsScreenAction()
    data class OnPageIndicatorToggled(val enabled: Boolean) : SettingsScreenAction()
}
