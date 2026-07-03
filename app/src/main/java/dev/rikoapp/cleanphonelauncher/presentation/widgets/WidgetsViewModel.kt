package dev.rikoapp.cleanphonelauncher.presentation.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import dev.rikoapp.cleanphonelauncher.domain.LocalWidgetDataSource
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WidgetsViewModel(
    private val widgetDataSource: LocalWidgetDataSource,
    private val widgetHost: WidgetHostManager,
    private val settingsRepository: SettingsRepository,
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
                // Repair any overlapping/out-of-bounds layout (notably right after the
                // v6->v7 migration, where every widget starts at cell 0,0).
                val laidOut = if (WidgetGrid.hasOverlaps(widgets)) WidgetGrid.repack(widgets) else widgets
                _state.update { it.copy(widgets = laidOut) }
                if (laidOut != widgets) {
                    laidOut.forEach {
                        widgetDataSource.updatePlacement(it.appWidgetId, it.page, it.col, it.row, it.colSpan, it.rowSpan)
                    }
                    val neededPages = (laidOut.maxOfOrNull { it.page } ?: -1) + 1
                    if (settingsRepository.widgetPageCount.value < neededPages) {
                        settingsRepository.setWidgetPageCount(neededPages)
                    }
                }
            }
        }
    }

    fun onWidgetBound(appWidgetId: Int, page: Int) {
        viewModelScope.launch {
            val all = _state.value.widgets
            val span = WidgetGrid.DEFAULT_SPAN
            var targetPage = page
            var free = WidgetGrid.findFree(WidgetGrid.occupancy(all.filter { it.page == page }), span, span)
            if (free == null) {
                // Current page is full — spill to another page with room, else a new page.
                val count = settingsRepository.widgetPageCount.value
                for (p in 0 until count) {
                    if (p == page) continue
                    val f = WidgetGrid.findFree(WidgetGrid.occupancy(all.filter { it.page == p }), span, span)
                    if (f != null) {
                        free = f
                        targetPage = p
                        break
                    }
                }
                if (free == null) {
                    targetPage = count
                    settingsRepository.setWidgetPageCount(count + 1)
                    free = 0 to 0
                }
            }
            widgetDataSource.addWidget(appWidgetId, targetPage, free.first, free.second, span, span)
        }
    }

    /** Move a widget to a cell on [targetPage]; ignored if it doesn't fit there. */
    fun onMoveWidget(appWidgetId: Int, targetPage: Int, col: Int, row: Int) {
        viewModelScope.launch {
            val widget = _state.value.widgets.find { it.appWidgetId == appWidgetId } ?: return@launch
            val others = _state.value.widgets.filter { it.page == targetPage && it.appWidgetId != appWidgetId }
            val grid = WidgetGrid.occupancy(others)
            if (WidgetGrid.fits(grid, col, row, widget.colSpan, widget.rowSpan)) {
                widgetDataSource.updatePlacement(appWidgetId, targetPage, col, row, widget.colSpan, widget.rowSpan)
            }
        }
    }

    /** Resize a widget, clamped to the free space around it. */
    fun onResizeWidget(appWidgetId: Int, colSpan: Int, rowSpan: Int) {
        viewModelScope.launch {
            val widget = _state.value.widgets.find { it.appWidgetId == appWidgetId } ?: return@launch
            val others = _state.value.widgets.filter { it.page == widget.page && it.appWidgetId != appWidgetId }
            val grid = WidgetGrid.occupancy(others)
            val (cs, rs) = WidgetGrid.clampSpan(grid, widget.col, widget.row, colSpan, rowSpan)
            widgetDataSource.updatePlacement(appWidgetId, widget.page, widget.col, widget.row, cs, rs)
        }
    }

    fun onRemovePage(page: Int) {
        viewModelScope.launch {
            _state.value.widgets
                .filter { it.page == page }
                .forEach { widgetHost.deleteId(it.appWidgetId) }
            widgetDataSource.removePage(page)
        }
    }

    fun onAddPageAtStart() {
        viewModelScope.launch {
            widgetDataSource.addPageAtStart()
        }
    }

    // Edit mode is shared across all widget pages so it survives page navigation.
    fun setEditMode(enabled: Boolean) {
        _state.update { it.copy(editMode = enabled) }
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
}
