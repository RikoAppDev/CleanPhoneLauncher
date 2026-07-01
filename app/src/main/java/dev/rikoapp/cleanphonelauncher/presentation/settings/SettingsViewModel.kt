package dev.rikoapp.cleanphonelauncher.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.LocalAppOverrideDataSource
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.GestureAction
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val localAppOverrideDataSource: LocalAppOverrideDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val appearanceFlow = combine(
                settingsRepository.themeMode,
                settingsRepository.colorStyle,
                settingsRepository.accentColor,
                settingsRepository.crashReportingEnabled
            ) { themeMode, colorStyle, accentColor, crashReporting ->
                Appearance(themeMode, colorStyle, accentColor, crashReporting)
            }

            val gestureFlow = combine(
                settingsRepository.swipeUpAction,
                settingsRepository.swipeDownAction,
                settingsRepository.doubleTapAction
            ) { swipeUp, swipeDown, doubleTap ->
                Gestures(swipeUp, swipeDown, doubleTap)
            }

            combine(
                appearanceFlow,
                gestureFlow,
                installedAppsRepository.apps,
                localAppOverrideDataSource.getOverrides()
            ) { appearance, gestures, apps, overrides ->
                val nameMap = overrides
                    .mapNotNull { o -> o.customName?.let { o.packageName to it } }
                    .toMap()
                val hiddenPackages = overrides.filter { it.hidden }.map { it.packageName }.toSet()
                val hiddenApps = apps
                    .filter { it.packageName in hiddenPackages }
                    .map { app -> nameMap[app.packageName]?.let { app.copy(name = it) } ?: app }
                    .sortedBy { it.name.uppercase() }

                SettingsScreenState(
                    themeMode = appearance.themeMode,
                    colorStyle = appearance.colorStyle,
                    crashReportingEnabled = appearance.crashReporting,
                    accentColor = appearance.accentColor,
                    swipeUpAction = gestures.swipeUp,
                    swipeDownAction = gestures.swipeDown,
                    doubleTapAction = gestures.doubleTap,
                    hiddenApps = hiddenApps
                )
            }.collect { _state.value = it }
        }
    }

    private data class Appearance(
        val themeMode: ThemeMode,
        val colorStyle: AppColorStyle,
        val accentColor: Int,
        val crashReporting: Boolean
    )

    private data class Gestures(
        val swipeUp: GestureAction,
        val swipeDown: GestureAction,
        val doubleTap: GestureAction
    )

    fun onAction(action: SettingsScreenAction) {
        when (action) {
            is SettingsScreenAction.OnThemeModeSelected ->
                settingsRepository.setThemeMode(action.mode)

            is SettingsScreenAction.OnColorStyleSelected ->
                settingsRepository.setColorStyle(action.style)

            is SettingsScreenAction.OnAccentColorSelected ->
                settingsRepository.setAccentColor(action.color)

            is SettingsScreenAction.OnCrashReportingToggled ->
                settingsRepository.setCrashReportingEnabled(action.enabled)

            is SettingsScreenAction.OnUnhideApp -> {
                viewModelScope.launch {
                    localAppOverrideDataSource.setHidden(action.packageName, false)
                }
            }

            is SettingsScreenAction.OnRerunSetup ->
                settingsRepository.setOnboardingCompleted(false)

            is SettingsScreenAction.OnSwipeUpActionSelected ->
                settingsRepository.setSwipeUpAction(action.action)

            is SettingsScreenAction.OnSwipeDownActionSelected ->
                settingsRepository.setSwipeDownAction(action.action)

            is SettingsScreenAction.OnDoubleTapActionSelected ->
                settingsRepository.setDoubleTapAction(action.action)
        }
    }
}
