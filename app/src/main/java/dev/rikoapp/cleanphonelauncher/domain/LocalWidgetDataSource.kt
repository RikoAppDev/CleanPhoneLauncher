package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.domain.model.HomeWidget
import kotlinx.coroutines.flow.Flow

interface LocalWidgetDataSource {
    fun getWidgets(): Flow<List<HomeWidget>>
    suspend fun addWidget(appWidgetId: Int, page: Int, col: Int, row: Int, colSpan: Int, rowSpan: Int)
    suspend fun updatePlacement(appWidgetId: Int, page: Int, col: Int, row: Int, colSpan: Int, rowSpan: Int)
    suspend fun remove(appWidgetId: Int)
    suspend fun removePage(page: Int)
    suspend fun addPageAtStart()
}
