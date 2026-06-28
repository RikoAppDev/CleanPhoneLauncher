package dev.rikoapp.cleanphonelauncher.data.di

import dev.rikoapp.cleanphonelauncher.data.AndroidAppActions
import dev.rikoapp.cleanphonelauncher.data.ClockRepositoryImpl
import dev.rikoapp.cleanphonelauncher.data.InstalledAppsRepositoryImpl
import dev.rikoapp.cleanphonelauncher.data.RecentAppsRepositoryImpl
import dev.rikoapp.cleanphonelauncher.data.SettingsRepositoryImpl
import dev.rikoapp.cleanphonelauncher.data.database.di.databaseModule
import dev.rikoapp.cleanphonelauncher.domain.AppActions
import dev.rikoapp.cleanphonelauncher.domain.ClockRepository
import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.RecentAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    includes(databaseModule)

    singleOf(::InstalledAppsRepositoryImpl) bind InstalledAppsRepository::class
    singleOf(::ClockRepositoryImpl) bind ClockRepository::class
    singleOf(::RecentAppsRepositoryImpl) bind RecentAppsRepository::class
    singleOf(::AndroidAppActions) bind AppActions::class
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
}
