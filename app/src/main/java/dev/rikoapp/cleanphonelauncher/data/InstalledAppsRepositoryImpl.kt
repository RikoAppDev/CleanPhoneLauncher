package dev.rikoapp.cleanphonelauncher.data

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
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

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            private var registered = false

            override fun onStart(owner: LifecycleOwner) {
                if (!registered) {
                    ContextCompat.registerReceiver(
                        context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
                    )
                    registered = true
                }
                getInstalledApps()
            }

            override fun onStop(owner: LifecycleOwner) {
                if (registered) {
                    context.unregisterReceiver(receiver)
                    registered = false
                }
            }
        })
    }

    override fun getInstalledApps() {
        applicationScope.launch {
            _apps.value = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val launcherIntent = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentActivities(
                        launcherIntent,
                        PackageManager.ResolveInfoFlags.of(0L)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.queryIntentActivities(launcherIntent, 0)
                }

                resolveInfos
                    .map { AppData(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
                    .distinctBy { it.packageName }
                    .sortedBy { it.name.toUpperCase(Locale.current) }
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