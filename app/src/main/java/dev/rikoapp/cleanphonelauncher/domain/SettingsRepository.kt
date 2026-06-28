package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val themeMode: StateFlow<ThemeMode>
    val colorStyle: StateFlow<AppColorStyle>
    val crashReportingEnabled: StateFlow<Boolean>
    val widgetId: StateFlow<Int>
    fun setThemeMode(mode: ThemeMode)
    fun setColorStyle(style: AppColorStyle)
    fun setCrashReportingEnabled(enabled: Boolean)
    fun setWidgetId(id: Int)
}
