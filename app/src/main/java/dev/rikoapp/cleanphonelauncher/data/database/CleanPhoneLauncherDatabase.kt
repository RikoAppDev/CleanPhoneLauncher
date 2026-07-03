package dev.rikoapp.cleanphonelauncher.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.rikoapp.cleanphonelauncher.data.database.dao.AppOverrideDao
import dev.rikoapp.cleanphonelauncher.data.database.dao.FavoriteAppDao
import dev.rikoapp.cleanphonelauncher.data.database.dao.WidgetDao
import dev.rikoapp.cleanphonelauncher.data.database.entities.AppOverrideEntity
import dev.rikoapp.cleanphonelauncher.data.database.entities.FavoriteAppEntity
import dev.rikoapp.cleanphonelauncher.data.database.entities.WidgetEntity

@Database(
    entities = [FavoriteAppEntity::class, AppOverrideEntity::class, WidgetEntity::class],
    version = 7,
    exportSchema = false
)
abstract class CleanPhoneLauncherDatabase : RoomDatabase() {
    abstract val favoriteAppDao: FavoriteAppDao
    abstract val appOverrideDao: AppOverrideDao
    abstract val widgetDao: WidgetDao
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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS widgets (" +
                "appWidgetId INTEGER NOT NULL PRIMARY KEY, " +
                "position INTEGER NOT NULL DEFAULT 0, " +
                "heightDp INTEGER NOT NULL DEFAULT 180)"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE widgets ADD COLUMN widthPercent INTEGER NOT NULL DEFAULT 100")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE widgets ADD COLUMN page INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Grid coordinates. Seed span from the legacy free-form size; col/row stay 0
        // so the layout engine re-packs them into a valid non-overlapping grid on load.
        db.execSQL("ALTER TABLE widgets ADD COLUMN col INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE widgets ADD COLUMN row INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE widgets ADD COLUMN colSpan INTEGER NOT NULL DEFAULT 2")
        db.execSQL("ALTER TABLE widgets ADD COLUMN rowSpan INTEGER NOT NULL DEFAULT 2")
        db.execSQL("UPDATE widgets SET colSpan = MAX(1, MIN(4, (widthPercent + 12) / 25))")
        db.execSQL("UPDATE widgets SET rowSpan = MAX(1, MIN(8, (heightDp + 44) / 88))")
    }
}