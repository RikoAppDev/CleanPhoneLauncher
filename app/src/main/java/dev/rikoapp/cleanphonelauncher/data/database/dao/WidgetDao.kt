package dev.rikoapp.cleanphonelauncher.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.rikoapp.cleanphonelauncher.data.database.entities.WidgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {
    @Query("SELECT * FROM widgets ORDER BY position ASC")
    fun getWidgets(): Flow<List<WidgetEntity>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM widgets")
    suspend fun nextPosition(): Int

    @Upsert
    suspend fun upsert(widget: WidgetEntity)

    @Query("UPDATE widgets SET position = :position WHERE appWidgetId = :appWidgetId")
    suspend fun updatePosition(appWidgetId: Int, position: Int)

    @Query("UPDATE widgets SET heightDp = :heightDp, widthPercent = :widthPercent WHERE appWidgetId = :appWidgetId")
    suspend fun updateSize(appWidgetId: Int, widthPercent: Int, heightDp: Int)

    @Query("DELETE FROM widgets WHERE appWidgetId = :appWidgetId")
    suspend fun deleteById(appWidgetId: Int)
}
