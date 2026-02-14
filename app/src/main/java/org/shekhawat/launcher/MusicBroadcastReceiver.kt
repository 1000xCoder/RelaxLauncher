package org.shekhawat.launcher

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MusicBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val title = intent.getStringExtra("title")
        val text = intent.getStringExtra("text")
        val packageName = intent.getStringExtra("packageName")
        val actions = intent.getParcelableArrayExtra("actions", Notification.Action::class.java)
        val notification = intent.getParcelableExtra("notification", Notification::class.java)  
    }
}