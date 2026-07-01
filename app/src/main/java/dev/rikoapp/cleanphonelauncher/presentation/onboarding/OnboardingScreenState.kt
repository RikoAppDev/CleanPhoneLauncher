package dev.rikoapp.cleanphonelauncher.presentation.onboarding

import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode

data class OnboardingScreenState(
    val stepIndex: Int = 0,
    val isDefaultLauncher: Boolean = false,
    val hasUsageAccess: Boolean = false,
    val notificationListenerEnabled: Boolean = false,
    val accessibilityLockEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorStyle: AppColorStyle = AppColorStyle.DYNAMIC,
    val accentColor: Int = 0
) {
    val steps: List<OnboardingStep> = OnboardingStep.entries
    val step: OnboardingStep get() = steps[stepIndex.coerceIn(0, steps.lastIndex)]
    val isFirstStep: Boolean get() = stepIndex <= 0
    val isLastStep: Boolean get() = stepIndex >= steps.lastIndex
}
