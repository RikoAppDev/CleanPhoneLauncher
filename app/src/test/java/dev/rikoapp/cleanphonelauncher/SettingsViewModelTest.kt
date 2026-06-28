package dev.rikoapp.cleanphonelauncher

import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode
import dev.rikoapp.cleanphonelauncher.presentation.settings.SettingsScreenAction
import dev.rikoapp.cleanphonelauncher.presentation.settings.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
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
        override fun setThemeMode(mode: ThemeMode) { themeMode.value = mode }
        override fun setColorStyle(style: AppColorStyle) { colorStyle.value = style }
        override fun setCrashReportingEnabled(enabled: Boolean) { crashReportingEnabled.value = enabled }
    }

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
        val vm = SettingsViewModel(FakeSettingsRepository())
        advanceUntilIdle()
        assertEquals(ThemeMode.SYSTEM, vm.state.value.themeMode)
        assertEquals(AppColorStyle.DYNAMIC, vm.state.value.colorStyle)
        assertEquals(false, vm.state.value.crashReportingEnabled)
    }

    @Test
    fun selectingThemeAndColor_updatesState() = runTest(dispatcher) {
        val vm = SettingsViewModel(FakeSettingsRepository())
        vm.onAction(SettingsScreenAction.OnThemeModeSelected(ThemeMode.DARK))
        vm.onAction(SettingsScreenAction.OnColorStyleSelected(AppColorStyle.BLUE))
        advanceUntilIdle()
        assertEquals(ThemeMode.DARK, vm.state.value.themeMode)
        assertEquals(AppColorStyle.BLUE, vm.state.value.colorStyle)
    }

    @Test
    fun togglingCrashReporting_updatesState() = runTest(dispatcher) {
        val vm = SettingsViewModel(FakeSettingsRepository())
        vm.onAction(SettingsScreenAction.OnCrashReportingToggled(true))
        advanceUntilIdle()
        assertEquals(true, vm.state.value.crashReportingEnabled)
    }
}
