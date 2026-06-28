package dev.rikoapp.cleanphonelauncher.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.rikoapp.cleanphonelauncher.data.database.dao.AppOverrideDao
import dev.rikoapp.cleanphonelauncher.data.database.dao.FavoriteAppDao
import dev.rikoapp.cleanphonelauncher.data.database.entities.AppOverrideEntity
import dev.rikoapp.cleanphonelauncher.data.database.entities.FavoriteAppEntity

@Database(
    entities = [FavoriteAppEntity::class, AppOverrideEntity::class],
    version = 3,
    exportSchema = false
)
abstract class CleanPhoneLauncherDatabase : RoomDatabase() {
    abstract val favoriteAppDao: FavoriteAppDao
    abstract val appOverrideDao: AppOverrideDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE favorite_apps ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS app_overrides (" +
                "packageName TEXT NOT NULL PRIMARY KEY, " +
                "hidden INTEGER NOT NULL DEFAULT 0, " +
                "customName TEXT)"
        )
    }
}