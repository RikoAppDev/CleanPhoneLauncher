package dev.rikoapp.cleanphonelauncher.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.LocalAppOverrideDataSource
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
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
            val colorFlow = combine(
                settingsRepository.colorStyle,
                settingsRepository.accentColor
            ) { style, accent -> style to accent }

            combine(
                settingsRepository.themeMode,
                colorFlow,
                settingsRepository.crashReportingEnabled,
                installedAppsRepository.apps,
                localAppOverrideDataSource.getOverrides()
            ) { themeMode, (colorStyle, accentColor), crashReporting, apps, overrides ->
                val nameMap = overrides
                    .mapNotNull { o -> o.customName?.let { o.packageName to it } }
                    .toMap()
                val hiddenPackages = overrides.filter { it.hidden }.map { it.packageName }.toSet()
                val hiddenApps = apps
                    .filter { it.packageName in hiddenPackages }
                    .map { app -> nameMap[app.packageName]?.let { app.copy(name = it) } ?: app }
                    .sortedBy { it.name.uppercase() }

                SettingsScreenState(
                    themeMode = themeMode,
                    colorStyle = colorStyle,
                    crashReportingEnabled = crashReporting,
                    accentColor = accentColor,
                    hiddenApps = hiddenApps
                )
            }.collect { _state.value = it }
        }
    }

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
        }
    }
}
