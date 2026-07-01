package dev.rikoapp.cleanphonelauncher.presentation.applist

import androidx.compose.foundation.text.input.TextFieldState
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.domain.model.AppShortcut
import dev.rikoapp.cleanphonelauncher.domain.model.Contact
import kotlin.text.indexOf

data class AppListScreenState(
    val allApps: List<AppData> = emptyList(),
    val hiddenApps: List<AppData> = emptyList(),
    val searchText: TextFieldState = TextFieldState(),
    val searchContacts: List<Contact> = emptyList(),
    val showDialogApp: AppData? = null,
    val dialogShortcuts: List<AppShortcut> = emptyList(),
    val showRenameApp: AppData? = null,
    val favoriteAppPackageNames: Set<String> = emptySet(),
    val recentApps: List<AppData> = emptyList(),
    val hasUsageStatsPermission: Boolean = false,
    val isActive: Boolean = false,
    val notificationCounts: Map<String, Int> = emptyMap(),
    val notificationSectionEnabled: Boolean = false
) {
    fun isHidden(app: AppData): Boolean = hiddenApps.any { it.packageName == app.packageName }

    fun badgeCount(app: AppData): Int = notificationCounts[app.packageName] ?: 0

    val notifiedApps: List<AppData>
        get() = if (notificationSectionEnabled) {
            allApps.filter { badgeCount(it) > 0 }.sortedByDescending { badgeCount(it) }
        } else {
            emptyList()
        }

    val filteredApps: List<AppData>
        get() = if (searchText.text.isBlank()) {
            allApps
        } else {
            allApps
                .filter { it.name.contains(searchText.text, ignoreCase = true) }
                .sortedWith(
                    compareBy<AppData> {
                        it.name.indexOf(
                            searchText.text.toString(),
                            ignoreCase = true
                        )
                    }
                        .thenBy { it.name.length }
                )
        }

    fun isFavorite(app: AppData): Boolean = favoriteAppPackageNames.contains(app.packageName)
}
