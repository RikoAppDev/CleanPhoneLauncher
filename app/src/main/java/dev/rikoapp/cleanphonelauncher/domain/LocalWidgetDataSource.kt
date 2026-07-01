package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.domain.model.HomeWidget
import kotlinx.coroutines.flow.Flow

interface LocalWidgetDataSource {
    fun getWidgets(): Flow<List<HomeWidget>>
    suspend fun addWidget(appWidgetId: Int, heightDp: Int, page: Int)
    suspend fun updateSize(appWidgetId: Int, widthPercent: Int, heightDp: Int)
    suspend fun reorder(orderedIds: List<Int>)
    suspend fun remove(appWidgetId: Int)
    suspend fun removePage(page: Int)
}
