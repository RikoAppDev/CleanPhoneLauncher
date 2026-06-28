package dev.rikoapp.cleanphonelauncher.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.rikoapp.cleanphonelauncher.data.database.dao.FavoriteAppDao
import dev.rikoapp.cleanphonelauncher.data.database.entities.FavoriteAppEntity

@Database(
    entities = [FavoriteAppEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CleanPhoneLauncherDatabase : RoomDatabase() {
    abstract val favoriteAppDao: FavoriteAppDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE favorite_apps ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
    }
}