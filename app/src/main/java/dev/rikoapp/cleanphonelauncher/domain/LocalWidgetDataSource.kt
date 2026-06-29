package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.domain.model.HomeWidget
import kotlinx.coroutines.flow.Flow

interface LocalWidgetDataSource {
    fun getWidgets(): Flow<List<HomeWidget>>
    suspend fun addWidget(appWidgetId: Int, heightDp: Int)
    suspend fun updateHeight(appWidgetId: Int, heightDp: Int)
    suspend fun reorder(orderedIds: List<Int>)
    suspend fun remove(appWidgetId: Int)
}
