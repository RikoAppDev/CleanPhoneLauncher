package dev.rikoapp.cleanphonelauncher

import android.app.Application
import dev.rikoapp.cleanphonelauncher.data.di.dataModule
import dev.rikoapp.cleanphonelauncher.di.appModule
import dev.rikoapp.cleanphonelauncher.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                appModule,
                dataModule,
                presentationModule
            )
        }
    }
}