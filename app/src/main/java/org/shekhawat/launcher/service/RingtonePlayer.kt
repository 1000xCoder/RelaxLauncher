package org.shekhawat.launcher.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import org.shekhawat.launcher.R

class RingtonePlayer : Service() {
    private lateinit var player: MediaPlayer
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        player = MediaPlayer.create(this, R.raw.short_alarm_clock)
        player.start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        player.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}