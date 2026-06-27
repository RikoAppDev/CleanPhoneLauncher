package dev.rikoapp.cleanphonelauncher.data

import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.domain.RecentAppsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecentAppsRepositoryImpl(
    private val context: Application,
    private val applicationScope: CoroutineScope
): RecentAppsRepository {

    private val _recentApps = MutableStateFlow<List<AppData>>(emptyList())
    override val recentApps = _recentApps.asStateFlow()

    private val _hasUsageStatsPermission = MutableStateFlow(false)
    override val hasUsageStatsPermission = _hasUsageStatsPermission.asStateFlow()

    init {
        checkUsageStatsPermission()
    }

    @Suppress("DEPRECATION")
    override fun checkUsageStatsPermission() {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        _hasUsageStatsPermission.value = mode == AppOpsManager.MODE_ALLOWED
    }

    override fun getRecentApps(apps: List<AppData>) {
        checkUsageStatsPermission()
        if (!_hasUsageStatsPermission.value) {
            _recentApps.value = emptyList()
            return
        }

        applicationScope.launch {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 1000 * 60 * 60 * 24 // 24 hours
            val usageStats =
                usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    beginTime,
                    endTime
                )

            // queryUsageStats can return null (e.g. permission revoked mid-flight)
            _recentApps.value = usageStats
                .orEmpty()
                .filter { it.lastTimeUsed > 0 }
                .sortedByDescending { it.lastTimeUsed }
                .mapNotNull { stats ->
                    apps.find { it.packageName == stats.packageName }
                }
                .distinctBy { it.packageName }
                .filter { it.packageName != context.packageName }
                .take(5)
        }
    }
}