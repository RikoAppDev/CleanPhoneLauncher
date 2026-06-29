package dev.rikoapp.cleanphonelauncher.presentation.widgets

import androidx.compose.runtime.Stable
import dev.rikoapp.cleanphonelauncher.domain.model.HomeWidget

@Stable
data class WidgetsScreenState(
    val widgets: List<HomeWidget> = emptyList()
)
