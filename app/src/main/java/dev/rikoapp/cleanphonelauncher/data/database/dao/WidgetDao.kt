package dev.rikoapp.cleanphonelauncher.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.rikoapp.cleanphonelauncher.data.database.entities.WidgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {
    @Query("SELECT * FROM widgets ORDER BY page ASC, position ASC")
    fun getWidgets(): Flow<List<WidgetEntity>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM widgets")
    suspend fun nextPosition(): Int

    @Upsert
    suspend fun upsert(widget: WidgetEntity)

    @Query(
        "UPDATE widgets SET page = :page, col = :col, row = :row, " +
            "colSpan = :colSpan, rowSpan = :rowSpan WHERE appWidgetId = :appWidgetId"
    )
    suspend fun updatePlacement(
        appWidgetId: Int,
        page: Int,
        col: Int,
        row: Int,
        colSpan: Int,
        rowSpan: Int,
    )

    @Query("DELETE FROM widgets WHERE page = :page")
    suspend fun deleteWidgetsOnPage(page: Int)

    @Query("UPDATE widgets SET page = page - 1 WHERE page > :page")
    suspend fun shiftPagesAfter(page: Int)

    @Query("UPDATE widgets SET page = page + 1")
    suspend fun shiftAllPagesForward()

    @Query("DELETE FROM widgets WHERE appWidgetId = :appWidgetId")
    suspend fun deleteById(appWidgetId: Int)
}
