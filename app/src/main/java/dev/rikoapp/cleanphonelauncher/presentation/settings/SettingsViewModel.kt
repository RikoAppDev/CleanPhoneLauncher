package dev.rikoapp.cleanphonelauncher.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.themeMode,
                settingsRepository.colorStyle,
                settingsRepository.crashReportingEnabled
            ) { themeMode, colorStyle, crashReporting ->
                SettingsScreenState(themeMode, colorStyle, crashReporting)
            }.collect { _state.value = it }
        }
    }

    fun onAction(action: SettingsScreenAction) {
        when (action) {
            is SettingsScreenAction.OnThemeModeSelected ->
                settingsRepository.setThemeMode(action.mode)

            is SettingsScreenAction.OnColorStyleSelected ->
                settingsRepository.setColorStyle(action.style)

            is SettingsScreenAction.OnCrashReportingToggled ->
                settingsRepository.setCrashReportingEnabled(action.enabled)
        }
    }
}
