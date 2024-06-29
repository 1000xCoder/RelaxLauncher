package org.shekhawat.launcher.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.ViewModelProvider

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Process the notification data as needed
        val newNotification = sbn.notification
        Log.d("NotificationListener", "Notification: ${newNotification}")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Handle removed notifications here
    }

    override fun getActiveNotifications(): Array<StatusBarNotification> {
        Log.d("RootScreen", "Active Notifications: ${super.getActiveNotifications().contentToString()}")
        return super.getActiveNotifications()
    }
}
