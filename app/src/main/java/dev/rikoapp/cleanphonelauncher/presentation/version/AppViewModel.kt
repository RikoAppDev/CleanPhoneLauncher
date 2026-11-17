package dev.rikoapp.cleanphonelauncher.presentation.version

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.domain.VersionCheckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppViewModel(
    private val versionCheckRepository: VersionCheckRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _versionState = MutableStateFlow<VersionState>(VersionState.Ok)
    val versionState = _versionState.asStateFlow()

    init {
        viewModelScope.launch {
            // Waiting for the opt-in instead of reading it once means turning the setting on
            // checks right away, without restarting the long-lived launcher process.
            settingsRepository.updateCheckEnabled.first { it }
            runCatching { versionCheckRepository.check() }
                .onSuccess { _versionState.value = it }
        }
    }

    fun onWarnDismissed() {
        versionCheckRepository.dismissWarn()
        _versionState.value = VersionState.Ok
    }
}
