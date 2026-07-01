package dev.rikoapp.cleanphonelauncher.presentation.onboarding

import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode

sealed interface OnboardingScreenAction {
    data object OnNext : OnboardingScreenAction
    data object OnBack : OnboardingScreenAction
    data object OnSkipAll : OnboardingScreenAction
    data object OnFinish : OnboardingScreenAction
    data object OnRefreshStatuses : OnboardingScreenAction
    data class OnThemeModeSelected(val mode: ThemeMode) : OnboardingScreenAction
    data class OnColorStyleSelected(val style: AppColorStyle) : OnboardingScreenAction
    data class OnAccentColorSelected(val color: Int) : OnboardingScreenAction
}
