package dev.rikoapp.cleanphonelauncher.presentation.settings

import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode

sealed class SettingsScreenAction {
    data class OnThemeModeSelected(val mode: ThemeMode) : SettingsScreenAction()
    data class OnColorStyleSelected(val style: AppColorStyle) : SettingsScreenAction()
    data class OnCrashReportingToggled(val enabled: Boolean) : SettingsScreenAction()
    data class OnUnhideApp(val packageName: String) : SettingsScreenAction()
}
