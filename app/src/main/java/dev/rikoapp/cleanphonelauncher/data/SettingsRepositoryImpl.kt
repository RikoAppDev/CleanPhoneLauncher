package dev.rikoapp.cleanphonelauncher.data

import android.app.Application
import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsRepositoryImpl(
    private val context: Application,
    private val applicationScope: CoroutineScope
) : SettingsRepository {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        prefs.getString(KEY_THEME_MODE, null).toThemeMode()
    )
    override val themeMode = _themeMode.asStateFlow()

    private val _colorStyle = MutableStateFlow(
        prefs.getString(KEY_COLOR_STYLE, null).toColorStyle()
    )
    override val colorStyle = _colorStyle.asStateFlow()

    private val _crashReportingEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_CRASH_REPORTING, false)
    )
    override val crashReportingEnabled = _crashReportingEnabled.asStateFlow()

    private val _accentColor = MutableStateFlow(prefs.getInt(KEY_ACCENT_COLOR, DEFAULT_ACCENT))
    override val accentColor = _accentColor.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    override val onboardingCompleted = _onboardingCompleted.asStateFlow()

    init {
        applyCrashReporting(_crashReportingEnabled.value)
    }

    override fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        applicationScope.launch {
            prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        }
    }

    override fun setColorStyle(style: AppColorStyle) {
        _colorStyle.value = style
        applicationScope.launch {
            prefs.edit().putString(KEY_COLOR_STYLE, style.name).apply()
        }
    }

    override fun setCrashReportingEnabled(enabled: Boolean) {
        _crashReportingEnabled.value = enabled
        applyCrashReporting(enabled)
        applicationScope.launch {
            prefs.edit().putBoolean(KEY_CRASH_REPORTING, enabled).apply()
        }
    }

    private fun applyCrashReporting(enabled: Boolean) {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled
    }

    override fun setAccentColor(color: Int) {
        _accentColor.value = color
        applicationScope.launch {
            prefs.edit().putInt(KEY_ACCENT_COLOR, color).apply()
        }
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
        applicationScope.launch {
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        }
    }

    private fun String?.toThemeMode() =
        ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.SYSTEM

    private fun String?.toColorStyle() =
        AppColorStyle.entries.firstOrNull { it.name == this } ?: AppColorStyle.DYNAMIC

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_COLOR_STYLE = "color_style"
        private const val KEY_CRASH_REPORTING = "crash_reporting"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val DEFAULT_ACCENT = 0xFF5B8DEF.toInt()
    }
}
