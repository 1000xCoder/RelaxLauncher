package org.shekhawat.launcher

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class MusicBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val title = intent.getStringExtra("title")
        val text = intent.getStringExtra("text")
        val packageName = intent.getStringExtra("packageName")

        val actions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra("actions", Notification.Action::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra("actions")
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("notification", Notification::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("notification")
        }

        // TODO: Use the received notification data (title, text, actions, notification)
    }
}
