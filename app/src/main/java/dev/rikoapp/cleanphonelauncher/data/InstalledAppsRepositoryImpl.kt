package dev.rikoapp.cleanphonelauncher.data

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.domain.InstalledAppsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InstalledAppsRepositoryImpl(
    private val context: Application,
    private val applicationScope: CoroutineScope
) : InstalledAppsRepository {

    private val _apps = MutableStateFlow<List<AppData>>(emptyList())
    override val apps = _apps.asStateFlow()

    private val _phoneApp = MutableStateFlow<AppData?>(null)
    override val phoneApp = _phoneApp.asStateFlow()

    private val _cameraApp = MutableStateFlow<AppData?>(null)
    override val cameraApp = _cameraApp.asStateFlow()

    init {
        getInstalledApps()
        registerPackageReceiver()
    }

    private fun registerPackageReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                getInstalledApps()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(receiver, filter)
    }

    override fun getInstalledApps() {
        applicationScope.launch {
            _apps.value = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val allApps =
                    pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))

                allApps.mapNotNull {
                    if (pm.getLaunchIntentForPackage(it.packageName) != null) {
                        val appName = it.loadLabel(pm).toString()
                        val packageName = it.packageName
                        AppData(appName, packageName)
                    } else {
                        null
                    }
                }.sortedBy { it.name.toUpperCase(Locale.current) }
            }
            findCoreApps()
        }
    }

    override fun findCoreApps() {
        applicationScope.launch(Dispatchers.IO) {
            val pm = context.packageManager

            // Find phone app
            val dialIntent = Intent(Intent.ACTION_DIAL)
            pm.resolveActivity(dialIntent, PackageManager.MATCH_DEFAULT_ONLY)?.let {
                val appInfo = it.activityInfo.applicationInfo
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    _phoneApp.value = AppData(
                        name = appInfo.loadLabel(pm).toString(),
                        packageName = appInfo.packageName
                    )
                }
            }

            // Find camera app
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            pm.resolveActivity(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)?.let {
                val appInfo = it.activityInfo.applicationInfo
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    _cameraApp.value = AppData(
                        name = appInfo.loadLabel(pm).toString(),
                        packageName = appInfo.packageName
                    )
                }
            }
        }
    }
}