package dev.rikoapp.cleanphonelauncher.data.database.di

import androidx.room.Room
import dev.rikoapp.cleanphonelauncher.data.RoomLocalFavoriteAppDataSource
import dev.rikoapp.cleanphonelauncher.data.database.CleanPhoneLauncherDatabase
import dev.rikoapp.cleanphonelauncher.domain.LocalFavoriteAppDataSource
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
        ).build()
    }

    single { get<CleanPhoneLauncherDatabase>().favoriteAppDao }
    singleOf(::RoomLocalFavoriteAppDataSource) bind LocalFavoriteAppDataSource::class
}