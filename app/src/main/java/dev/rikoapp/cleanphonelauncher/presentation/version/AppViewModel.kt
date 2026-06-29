package dev.rikoapp.cleanphonelauncher.presentation.version

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.VersionCheckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val versionCheckRepository: VersionCheckRepository
) : ViewModel() {

    private val _versionState = MutableStateFlow<VersionState>(VersionState.Ok)
    val versionState = _versionState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { versionCheckRepository.check() }
                .onSuccess { _versionState.value = it }
        }
    }

    fun onWarnDismissed() {
        versionCheckRepository.dismissWarn()
        _versionState.value = VersionState.Ok
    }
}
