package dev.rikoapp.cleanphonelauncher.data

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import dev.rikoapp.cleanphonelauncher.domain.AppActions

class AndroidAppActions(
    private val context: Application
) : AppActions {

    override fun launch(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun openAppInfo(packageName: String) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "package:$packageName".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    @Suppress("DEPRECATION")
    override fun requestUninstall(packageName: String) {
        val intent = Intent(
            Intent.ACTION_UNINSTALL_PACKAGE,
            "package:$packageName".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
