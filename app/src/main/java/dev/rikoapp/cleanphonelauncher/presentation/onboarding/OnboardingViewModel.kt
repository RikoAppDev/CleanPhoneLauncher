package dev.rikoapp.cleanphonelauncher.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.data.SetupStatusChecker
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val setupStatusChecker: SetupStatusChecker
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingScreenState())
    val state = _state.asStateFlow()

    init {
        refreshStatuses()
        viewModelScope.launch {
            combine(
                settingsRepository.themeMode,
                settingsRepository.colorStyle,
                settingsRepository.accentColor
            ) { mode, style, accent -> Triple(mode, style, accent) }
                .collect { (mode, style, accent) ->
                    _state.update {
                        it.copy(themeMode = mode, colorStyle = style, accentColor = accent)
                    }
                }
        }
    }

    fun onAction(action: OnboardingScreenAction) {
        when (action) {
            OnboardingScreenAction.OnNext ->
                _state.update { it.copy(stepIndex = (it.stepIndex + 1).coerceAtMost(it.steps.lastIndex)) }

            OnboardingScreenAction.OnBack ->
                _state.update { it.copy(stepIndex = (it.stepIndex - 1).coerceAtLeast(0)) }

            OnboardingScreenAction.OnRefreshStatuses ->
                refreshStatuses()

            OnboardingScreenAction.OnSkipAll,
            OnboardingScreenAction.OnFinish ->
                settingsRepository.setOnboardingCompleted(true)

            is OnboardingScreenAction.OnThemeModeSelected ->
                settingsRepository.setThemeMode(action.mode)

            is OnboardingScreenAction.OnColorStyleSelected ->
                settingsRepository.setColorStyle(action.style)

            is OnboardingScreenAction.OnAccentColorSelected ->
                settingsRepository.setAccentColor(action.color)
        }
    }

    private fun refreshStatuses() {
        _state.update {
            it.copy(
                isDefaultLauncher = setupStatusChecker.isDefaultLauncher(),
                hasUsageAccess = setupStatusChecker.hasUsageAccess(),
                notificationListenerEnabled = setupStatusChecker.isNotificationListenerEnabled(),
                accessibilityLockEnabled = setupStatusChecker.isAccessibilityLockEnabled()
            )
        }
    }
}
