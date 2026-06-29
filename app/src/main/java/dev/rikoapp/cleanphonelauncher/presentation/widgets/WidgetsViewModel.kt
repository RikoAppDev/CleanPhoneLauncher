package dev.rikoapp.cleanphonelauncher.presentation.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import dev.rikoapp.cleanphonelauncher.domain.LocalWidgetDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WidgetsViewModel(
    private val widgetDataSource: LocalWidgetDataSource,
    private val widgetHost: WidgetHostManager
) : ViewModel() {

    private val _state = MutableStateFlow(WidgetsScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val keptIds = widgetDataSource.getWidgets().first().map { it.appWidgetId }.toSet()
            widgetHost.allocatedIds().forEach { id ->
                if (id !in keptIds) widgetHost.deleteId(id)
            }
        }
        viewModelScope.launch {
            widgetDataSource.getWidgets().collect { widgets ->
                _state.update { it.copy(widgets = widgets) }
            }
        }
    }

    fun onWidgetBound(appWidgetId: Int) {
        viewModelScope.launch {
            widgetDataSource.addWidget(appWidgetId, DEFAULT_HEIGHT_DP)
        }
    }

    fun discardAllocation(appWidgetId: Int) {
        if (appWidgetId != -1) widgetHost.deleteId(appWidgetId)
    }

    fun onRemove(appWidgetId: Int) {
        viewModelScope.launch {
            widgetDataSource.remove(appWidgetId)
            widgetHost.deleteId(appWidgetId)
        }
    }

    fun onResize(appWidgetId: Int, heightDp: Int) {
        viewModelScope.launch {
            widgetDataSource.updateHeight(appWidgetId, heightDp.coerceIn(MIN_HEIGHT_DP, MAX_HEIGHT_DP))
        }
    }

    fun onReorder(orderedIds: List<Int>) {
        viewModelScope.launch { widgetDataSource.reorder(orderedIds) }
    }

    companion object {
        const val MIN_HEIGHT_DP = 80
        const val MAX_HEIGHT_DP = 560
        const val DEFAULT_HEIGHT_DP = 180
    }
}
