package dev.rikoapp.cleanphonelauncher.data.database.di

import androidx.room.Room
import dev.rikoapp.cleanphonelauncher.data.RoomLocalAppOverrideDataSource
import dev.rikoapp.cleanphonelauncher.data.RoomLocalFavoriteAppDataSource
import dev.rikoapp.cleanphonelauncher.data.RoomLocalWidgetDataSource
import dev.rikoapp.cleanphonelauncher.data.database.CleanPhoneLauncherDatabase
import dev.rikoapp.cleanphonelauncher.data.database.MIGRATION_1_2
import dev.rikoapp.cleanphonelauncher.data.database.MIGRATION_2_3
import dev.rikoapp.cleanphonelauncher.data.database.MIGRATION_3_4
import dev.rikoapp.cleanphonelauncher.domain.LocalAppOverrideDataSource
import dev.rikoapp.cleanphonelauncher.domain.LocalFavoriteAppDataSource
import dev.rikoapp.cleanphonelauncher.domain.LocalWidgetDataSource
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            CleanPhoneLauncherDatabase::class.java,
            "CleanPhoneLauncher.db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
    }

    single { get<CleanPhoneLauncherDatabase>().favoriteAppDao }
    single { get<CleanPhoneLauncherDatabase>().appOverrideDao }
    single { get<CleanPhoneLauncherDatabase>().widgetDao }
    singleOf(::RoomLocalFavoriteAppDataSource) bind LocalFavoriteAppDataSource::class
    singleOf(::RoomLocalAppOverrideDataSource) bind LocalAppOverrideDataSource::class
    singleOf(::RoomLocalWidgetDataSource) bind LocalWidgetDataSource::class
}