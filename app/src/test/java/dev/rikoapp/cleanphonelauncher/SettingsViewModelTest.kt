package dev.rikoapp.cleanphonelauncher

import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.LocalAppOverrideDataSource
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.domain.model.AppOverride
import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.GestureAction
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode
import dev.rikoapp.cleanphonelauncher.presentation.settings.SettingsScreenAction
import dev.rikoapp.cleanphonelauncher.presentation.settings.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private class FakeSettingsRepository : SettingsRepository {
        override val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        override val colorStyle = MutableStateFlow(AppColorStyle.DYNAMIC)
        override val crashReportingEnabled = MutableStateFlow(false)
        override val accentColor = MutableStateFlow(0)
        override val onboardingCompleted = MutableStateFlow(true)
        override val swipeUpAction = MutableStateFlow(GestureAction.APP_DRAWER)
        override val swipeDownAction = MutableStateFlow(GestureAction.NOTIFICATIONS)
        override val doubleTapAction = MutableStateFlow(GestureAction.LOCK_SCREEN)
        override fun setThemeMode(mode: ThemeMode) { themeMode.value = mode }
        override fun setColorStyle(style: AppColorStyle) { colorStyle.value = style }
        override fun setCrashReportingEnabled(enabled: Boolean) { crashReportingEnabled.value = enabled }
        override fun setAccentColor(color: Int) { accentColor.value = color }
        override fun setOnboardingCompleted(completed: Boolean) { onboardingCompleted.value = completed }
        override fun setSwipeUpAction(action: GestureAction) { swipeUpAction.value = action }
        override fun setSwipeDownAction(action: GestureAction) { swipeDownAction.value = action }
        override fun setDoubleTapAction(action: GestureAction) { doubleTapAction.value = action }
    }

    private class FakeInstalledAppsRepository : InstalledAppsRepository {
        override val apps = MutableStateFlow<List<AppData>>(emptyList())
        override val phoneApp = MutableStateFlow<AppData?>(null)
        override val cameraApp = MutableStateFlow<AppData?>(null)
        override fun getInstalledApps() {}
        override fun findCoreApps() {}
    }

    private class FakeAppOverrideDataSource : LocalAppOverrideDataSource {
        val overrides = MutableStateFlow<List<AppOverride>>(emptyList())
        override fun getOverrides(): Flow<List<AppOverride>> = overrides
        override suspend fun setHidden(packageName: String, hidden: Boolean) {}
        override suspend fun setCustomName(packageName: String, customName: String?) {}
    }

    private fun viewModel() = SettingsViewModel(
        FakeSettingsRepository(),
        FakeInstalledAppsRepository(),
        FakeAppOverrideDataSource()
    )

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_reflectsRepository() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(ThemeMode.SYSTEM, vm.state.value.themeMode)
        assertEquals(AppColorStyle.DYNAMIC, vm.state.value.colorStyle)
        assertEquals(false, vm.state.value.crashReportingEnabled)
    }

    @Test
    fun selectingThemeAndColor_updatesState() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(SettingsScreenAction.OnThemeModeSelected(ThemeMode.DARK))
        vm.onAction(SettingsScreenAction.OnColorStyleSelected(AppColorStyle.BLUE))
        advanceUntilIdle()
        assertEquals(ThemeMode.DARK, vm.state.value.themeMode)
        assertEquals(AppColorStyle.BLUE, vm.state.value.colorStyle)
    }

    @Test
    fun togglingCrashReporting_updatesState() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(SettingsScreenAction.OnCrashReportingToggled(true))
        advanceUntilIdle()
        assertEquals(true, vm.state.value.crashReportingEnabled)
    }
}
