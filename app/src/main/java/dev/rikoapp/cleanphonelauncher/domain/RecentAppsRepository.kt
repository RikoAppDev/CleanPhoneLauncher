package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import kotlinx.coroutines.flow.StateFlow

interface RecentAppsRepository {
    val recentApps: StateFlow<List<AppData>>
    val hasUsageStatsPermission: StateFlow<Boolean>

    fun checkUsageStatsPermission()
    fun getRecentApps(apps: List<AppData>)
}