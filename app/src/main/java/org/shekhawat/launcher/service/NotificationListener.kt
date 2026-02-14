package org.shekhawat.launcher.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Process the notification data as needed
        val notification = sbn.notification
        val packageName = sbn.packageName
        val title = notification.extras.getCharSequence("android.title") // track
        val text = notification.extras.getCharSequence("android.text") // artist
        val actions = notification.actions
        val category = notification.category // transport

        Log.d("RootScreen", "Notification: $notification")
        Log.d("RootScreen", "Package Name: $packageName")
        Log.d("RootScreen", "Title: $title, Text: $text")
        Log.d("RootScreen", "Category: $category")
        Log.d("RootScreen", "Actions: $actions")

        // call broadcast receiver
        val intent = Intent("org.shekhawat.launcher.NotificationListener")
        intent.putExtra("notification", notification)
        intent.putExtra("packageName", packageName)
        intent.putExtra("title", title)
        intent.putExtra("text", text)
        intent.putExtra("actions", actions)
        sendBroadcast(intent)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Handle removed notifications here
    }

    override fun getActiveNotifications(): Array<StatusBarNotification> {
        return super.getActiveNotifications()
    }
}
