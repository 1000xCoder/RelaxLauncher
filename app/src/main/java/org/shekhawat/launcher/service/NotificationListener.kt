package org.shekhawat.launcher.service

import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data class representing a single notification item.
 */
data class NotificationItem(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)

class NotificationListener : NotificationListenerService() {

    companion object {
        private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
        val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

        private var instance: NotificationListener? = null

        /**
         * Dismiss a notification by its key.
         */
        fun dismissNotification(key: String) {
            instance?.cancelNotification(key)
        }

        /**
         * Dismiss all notifications.
         */
        fun dismissAll() {
            instance?.cancelAllNotifications()
        }
    }

    // Debounce handler — coalesces rapid notification bursts (e.g. WhatsApp group chats)
    // into a single list update to avoid janking the UI.
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = Runnable { doUpdateNotificationList() }
    private val debounceMs = 500L

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        instance = null
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scheduleUpdate()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        scheduleUpdate()
    }

    /**
     * Debounced update: if many notifications arrive in quick succession (WhatsApp burst),
     * we only rebuild the list once after the burst settles.
     */
    private fun scheduleUpdate() {
        handler.removeCallbacks(updateRunnable)
        handler.postDelayed(updateRunnable, debounceMs)
    }

    private fun doUpdateNotificationList() {
        try {
            val active = activeNotifications ?: return
            val items = active.mapNotNull { sbn ->
                val extras = sbn.notification.extras
                val title = extras.getCharSequence("android.title")?.toString() ?: return@mapNotNull null
                val text = extras.getCharSequence("android.text")?.toString() ?: ""
                // Skip our own notifications
                if (sbn.packageName == packageName) return@mapNotNull null
                NotificationItem(
                    key = sbn.key,
                    packageName = sbn.packageName,
                    title = title,
                    text = text,
                    timestamp = sbn.postTime
                )
            }
            _notifications.value = items
        } catch (_: Exception) {
            // Service may not be connected yet
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        doUpdateNotificationList()
    }
}
