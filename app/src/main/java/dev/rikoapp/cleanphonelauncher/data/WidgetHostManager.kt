package dev.rikoapp.cleanphonelauncher.data

import android.app.Application
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Bundle

class WidgetHostManager(context: Application) {

    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val host: AppWidgetHost = AppWidgetHost(context, HOST_ID)

    fun allocateId(): Int = host.allocateAppWidgetId()

    fun allocatedIds(): IntArray = runCatching { host.appWidgetIds }.getOrDefault(IntArray(0))

    fun deleteId(id: Int) {
        runCatching { host.deleteAppWidgetId(id) }
    }

    fun startListening() {
        runCatching { host.startListening() }
    }

    fun stopListening() {
        runCatching { host.stopListening() }
    }

    fun getInfo(id: Int): AppWidgetProviderInfo? = appWidgetManager.getAppWidgetInfo(id)

    fun updateSize(id: Int, widthDp: Int, heightDp: Int) {
        val options = Bundle().apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp)
        }
        runCatching { appWidgetManager.updateAppWidgetOptions(id, options) }
    }

    fun bindIfAllowed(id: Int, info: AppWidgetProviderInfo): Boolean =
        runCatching { appWidgetManager.bindAppWidgetIdIfAllowed(id, info.provider) }.getOrDefault(false)

    fun createView(context: Context, id: Int): AppWidgetHostView? = runCatching {
        val info = appWidgetManager.getAppWidgetInfo(id) ?: return null
        host.createView(context, id, info)
    }.getOrNull()

    companion object {
        const val HOST_ID = 1024
    }
}
