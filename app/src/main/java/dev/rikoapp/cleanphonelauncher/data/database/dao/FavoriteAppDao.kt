package dev.rikoapp.cleanphonelauncher.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import dev.rikoapp.cleanphonelauncher.data.database.entities.FavoriteAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteAppDao {
    @Query("SELECT * FROM favorite_apps ORDER BY position ASC")
    fun getFavoriteApps(): Flow<List<FavoriteAppEntity>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM favorite_apps")
    suspend fun nextPosition(): Int

    @Upsert
    suspend fun upsert(favoriteApp: FavoriteAppEntity)

    @Query("UPDATE favorite_apps SET position = :position WHERE packageName = :packageName")
    suspend fun updatePosition(packageName: String, position: Int)

    @Delete
    suspend fun delete(favoriteApp: FavoriteAppEntity)
}