package dev.rikoapp.cleanphonelauncher.presentation.di

import dev.rikoapp.cleanphonelauncher.presentation.applist.AppListViewModel
import dev.rikoapp.cleanphonelauncher.presentation.home.HomeViewModel
import dev.rikoapp.cleanphonelauncher.presentation.onboarding.OnboardingViewModel
import dev.rikoapp.cleanphonelauncher.presentation.settings.SettingsViewModel
import dev.rikoapp.cleanphonelauncher.presentation.version.AppViewModel
import dev.rikoapp.cleanphonelauncher.presentation.widgets.WidgetsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::AppViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AppListViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::WidgetsViewModel)
    viewModelOf(::OnboardingViewModel)
}
