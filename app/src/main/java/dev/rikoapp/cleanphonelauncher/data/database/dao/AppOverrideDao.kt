package dev.rikoapp.cleanphonelauncher.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.rikoapp.cleanphonelauncher.data.database.entities.AppOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppOverrideDao {
    @Query("SELECT * FROM app_overrides")
    fun getOverrides(): Flow<List<AppOverrideEntity>>

    @Query("SELECT * FROM app_overrides WHERE packageName = :packageName")
    suspend fun getOverride(packageName: String): AppOverrideEntity?

    @Upsert
    suspend fun upsert(override: AppOverrideEntity)

    @Query("DELETE FROM app_overrides WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
