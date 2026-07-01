package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.domain.model.AppShortcut

interface ShortcutRepository {
    fun getShortcuts(packageName: String): List<AppShortcut>
    fun launchShortcut(shortcut: AppShortcut)
}
