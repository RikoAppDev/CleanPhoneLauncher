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
            entities.map {
                HomeWidget(
                    appWidgetId = it.appWidgetId,
                    page = it.page,
                    col = it.col,
                    row = it.row,
                    colSpan = it.colSpan,
                    rowSpan = it.rowSpan,
                    position = it.position,
                )
            }
        }

    override suspend fun addWidget(appWidgetId: Int, page: Int, col: Int, row: Int, colSpan: Int, rowSpan: Int) {
        widgetDao.upsert(
            WidgetEntity(
                appWidgetId = appWidgetId,
                position = widgetDao.nextPosition(),
                page = page,
                col = col,
                row = row,
                colSpan = colSpan,
                rowSpan = rowSpan,
            )
        )
    }

    override suspend fun updatePlacement(appWidgetId: Int, page: Int, col: Int, row: Int, colSpan: Int, rowSpan: Int) {
        widgetDao.updatePlacement(appWidgetId, page, col, row, colSpan, rowSpan)
    }

    override suspend fun remove(appWidgetId: Int) {
        widgetDao.deleteById(appWidgetId)
    }

    override suspend fun removePage(page: Int) {
        widgetDao.deleteWidgetsOnPage(page)
        widgetDao.shiftPagesAfter(page)
    }

    override suspend fun addPageAtStart() {
        widgetDao.shiftAllPagesForward()
    }
}
