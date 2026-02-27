package dev.rikoapp.cleanphonelauncher.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.rikoapp.cleanphonelauncher.data.database.dao.FavoriteAppDao
import dev.rikoapp.cleanphonelauncher.data.database.entities.FavoriteAppEntity

@Database(
    entities = [FavoriteAppEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CleanPhoneLauncherDatabase : RoomDatabase() {
    abstract val favoriteAppDao: FavoriteAppDao
}