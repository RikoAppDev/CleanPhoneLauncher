package dev.rikoapp.cleanphonelauncher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.rikoapp.cleanphonelauncher.domain.NotificationCountRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppNotificationListenerService : NotificationListenerService(), KoinComponent {

    private val repository: NotificationCountRepository by inject()

    override fun onListenerConnected() = refresh()

    override fun onNotificationPosted(sbn: StatusBarNotification?) = refresh()

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refresh()

    override fun onListenerDisconnected() {
        repository.update(emptyMap())
    }

    private fun refresh() {
        val counts = try {
            activeNotifications
                ?.filter { it.isClearable }
                ?.groupingBy { it.packageName }
                ?.eachCount()
                ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
        repository.update(counts)
    }
}
