package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.GestureAction
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val themeMode: StateFlow<ThemeMode>
    val colorStyle: StateFlow<AppColorStyle>
    val crashReportingEnabled: StateFlow<Boolean>
    val accentColor: StateFlow<Int>
    val onboardingCompleted: StateFlow<Boolean>
    val swipeUpAction: StateFlow<GestureAction>
    val swipeDownAction: StateFlow<GestureAction>
    val doubleTapAction: StateFlow<GestureAction>
    val contactsSearchEnabled: StateFlow<Boolean>
    val quickActions: StateFlow<List<String>>
    val quickActionsConfigured: StateFlow<Boolean>
    val pageIndicatorEnabled: StateFlow<Boolean>
    val notificationDrawerSectionEnabled: StateFlow<Boolean>
    val widgetPageCount: StateFlow<Int>
    fun setThemeMode(mode: ThemeMode)
    fun setColorStyle(style: AppColorStyle)
    fun setCrashReportingEnabled(enabled: Boolean)
    fun setAccentColor(color: Int)
    fun setOnboardingCompleted(completed: Boolean)
    fun setSwipeUpAction(action: GestureAction)
    fun setSwipeDownAction(action: GestureAction)
    fun setDoubleTapAction(action: GestureAction)
    fun setContactsSearchEnabled(enabled: Boolean)
    fun setQuickActions(packageNames: List<String>)
    fun setPageIndicatorEnabled(enabled: Boolean)
    fun setNotificationDrawerSectionEnabled(enabled: Boolean)
    fun setWidgetPageCount(count: Int)
}
