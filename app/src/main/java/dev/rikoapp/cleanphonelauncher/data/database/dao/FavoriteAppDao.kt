package dev.rikoapp.cleanphonelauncher.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import dev.rikoapp.cleanphonelauncher.data.database.entities.FavoriteAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteAppDao {
    @Query("SELECT * FROM favorite_apps")
    fun getFavoriteApps(): Flow<List<FavoriteAppEntity>>

    @Upsert
    suspend fun upsert(favoriteApp: FavoriteAppEntity)

    @Delete
    suspend fun delete(favoriteApp: FavoriteAppEntity)
}