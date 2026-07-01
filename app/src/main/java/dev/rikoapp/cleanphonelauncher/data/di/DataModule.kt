package dev.rikoapp.cleanphonelauncher.data.di

import dev.rikoapp.cleanphonelauncher.data.AndroidAppActions
import dev.rikoapp.cleanphonelauncher.data.ClockRepositoryImpl
import dev.rikoapp.cleanphonelauncher.data.InstalledAppsRepositoryImpl
import dev.rikoapp.cleanphonelauncher.data.NotificationCountRepositoryImpl
import dev.rikoapp.cleanphonelauncher.data.RecentAppsRepositoryImpl
import dev.rikoapp.cleanphonelauncher.data.SettingsRepositoryImpl
import dev.rikoapp.cleanphonelauncher.data.RecommendedAppsProvider
import dev.rikoapp.cleanphonelauncher.data.SetupStatusChecker
import dev.rikoapp.cleanphonelauncher.data.AndroidShortcutRepository
import dev.rikoapp.cleanphonelauncher.data.AndroidContactRepository
import dev.rikoapp.cleanphonelauncher.data.FirebaseVersionCheckRepository
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import dev.rikoapp.cleanphonelauncher.data.database.di.databaseModule
import dev.rikoapp.cleanphonelauncher.domain.AppActions
import dev.rikoapp.cleanphonelauncher.domain.ClockRepository
import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.NotificationCountRepository
import dev.rikoapp.cleanphonelauncher.domain.RecentAppsRepository
import dev.rikoapp.cleanphonelauncher.domain.ContactRepository
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.domain.ShortcutRepository
import dev.rikoapp.cleanphonelauncher.domain.VersionCheckRepository
import org.koin.android.ext.koin.androidApplication
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
    singleOf(::NotificationCountRepositoryImpl) bind NotificationCountRepository::class
    singleOf(::FirebaseVersionCheckRepository) bind VersionCheckRepository::class
    singleOf(::SetupStatusChecker)
    singleOf(::RecommendedAppsProvider)
    singleOf(::AndroidShortcutRepository) bind ShortcutRepository::class
    singleOf(::AndroidContactRepository) bind ContactRepository::class
    single { WidgetHostManager(androidApplication()) }
}
