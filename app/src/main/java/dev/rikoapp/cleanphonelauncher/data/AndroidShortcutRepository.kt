package dev.rikoapp.cleanphonelauncher.data

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import dev.rikoapp.cleanphonelauncher.domain.ShortcutRepository
import dev.rikoapp.cleanphonelauncher.domain.model.AppShortcut

class AndroidShortcutRepository(
    private val context: Application
) : ShortcutRepository {

    private val launcherApps: LauncherApps
        get() = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    override fun getShortcuts(packageName: String): List<AppShortcut> {
        val apps = launcherApps
        if (!apps.hasShortcutHostPermission()) return emptyList()

        val query = LauncherApps.ShortcutQuery()
            .setPackage(packageName)
            .setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
            )

        val shortcuts = runCatching {
            apps.getShortcuts(query, Process.myUserHandle())
        }.getOrNull().orEmpty()

        return shortcuts
            .filter { it.isEnabled }
            .sortedBy { it.rank }
            .mapNotNull { info ->
                val label = (info.shortLabel ?: info.longLabel)
                    ?.toString()
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                AppShortcut(id = info.id, packageName = packageName, label = label)
            }
            .take(5)
    }

    override fun launchShortcut(shortcut: AppShortcut) {
        runCatching {
            launcherApps.startShortcut(
                shortcut.packageName,
                shortcut.id,
                null,
                null,
                Process.myUserHandle()
            )
        }
    }
}
