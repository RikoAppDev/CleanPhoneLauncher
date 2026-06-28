package dev.rikoapp.cleanphonelauncher.domain

interface AppActions {
    fun launch(packageName: String): Boolean
    fun openAppInfo(packageName: String)
    fun requestUninstall(packageName: String)
}
