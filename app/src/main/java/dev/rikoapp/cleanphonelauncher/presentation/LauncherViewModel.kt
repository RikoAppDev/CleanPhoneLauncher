package dev.rikoapp.cleanphonelauncher.presentation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rikoapp.cleanphonelauncher.domain.AppData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherViewModel : ViewModel() {

    private val _apps = MutableStateFlow<List<AppData>>(emptyList())
    val apps = _apps.asStateFlow()

    private val _phoneApp = MutableStateFlow<AppData?>(null)
    val phoneApp = _phoneApp.asStateFlow()

    private val _cameraApp = MutableStateFlow<AppData?>(null)
    val cameraApp = _cameraApp.asStateFlow()

    fun getInstalledApps(context: Context) {
        viewModelScope.launch {
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
            findCoreApps(context)
        }
    }

    private fun findCoreApps(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
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
