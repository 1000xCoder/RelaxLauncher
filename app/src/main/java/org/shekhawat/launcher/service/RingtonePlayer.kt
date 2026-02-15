package org.shekhawat.launcher.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import org.shekhawat.launcher.R

class RingtonePlayer : Service() {
    private var player: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Release any existing player before creating a new one
        releasePlayer()
        try {
            player = MediaPlayer.create(this, R.raw.short_alarm_clock)?.apply {
                setOnCompletionListener {
                    stopSelf()
                }
                start()
            }
        } catch (e: Exception) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun releasePlayer() {
        player?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (_: Exception) {
                // Ignore exceptions during cleanup
            }
        }
        player = null
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
