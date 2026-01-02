package dev.rikoapp.cleanphonelauncher.presentation.di

import dev.rikoapp.cleanphonelauncher.presentation.applist.AppListViewModel
import dev.rikoapp.cleanphonelauncher.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::AppListViewModel)
}
