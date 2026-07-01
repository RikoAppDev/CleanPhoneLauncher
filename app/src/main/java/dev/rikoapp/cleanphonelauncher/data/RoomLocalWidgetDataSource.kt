package dev.rikoapp.cleanphonelauncher.data

import dev.rikoapp.cleanphonelauncher.data.database.dao.WidgetDao
import dev.rikoapp.cleanphonelauncher.data.database.entities.WidgetEntity
import dev.rikoapp.cleanphonelauncher.domain.LocalWidgetDataSource
import dev.rikoapp.cleanphonelauncher.domain.model.HomeWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLocalWidgetDataSource(
    private val widgetDao: WidgetDao
) : LocalWidgetDataSource {

    override fun getWidgets(): Flow<List<HomeWidget>> =
        widgetDao.getWidgets().map { entities ->
            entities.map { HomeWidget(it.appWidgetId, it.position, it.heightDp, it.widthPercent) }
        }

    override suspend fun addWidget(appWidgetId: Int, heightDp: Int) {
        widgetDao.upsert(
            WidgetEntity(
                appWidgetId = appWidgetId,
                position = widgetDao.nextPosition(),
                heightDp = heightDp
            )
        )
    }

    override suspend fun updateSize(appWidgetId: Int, widthPercent: Int, heightDp: Int) {
        widgetDao.updateSize(appWidgetId, widthPercent, heightDp)
    }

    override suspend fun reorder(orderedIds: List<Int>) {
        orderedIds.forEachIndexed { index, id ->
            widgetDao.updatePosition(id, index)
        }
    }

    override suspend fun remove(appWidgetId: Int) {
        widgetDao.deleteById(appWidgetId)
    }
}
