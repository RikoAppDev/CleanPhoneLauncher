package dev.rikoapp.cleanphonelauncher.presentation.settings

import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.GestureAction
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode

data class SettingsScreenState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorStyle: AppColorStyle = AppColorStyle.DYNAMIC,
    val crashReportingEnabled: Boolean = false,
    val accentColor: Int = 0,
    val swipeUpAction: GestureAction = GestureAction.APP_DRAWER,
    val swipeDownAction: GestureAction = GestureAction.NOTIFICATIONS,
    val doubleTapAction: GestureAction = GestureAction.LOCK_SCREEN,
    val hiddenApps: List<AppData> = emptyList()
)
