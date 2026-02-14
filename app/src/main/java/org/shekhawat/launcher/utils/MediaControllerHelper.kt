package org.shekhawat.launcher.utils

import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager

class MediaControllerHelper(private val context: Context) {

    private var mediaController: MediaController? = null

    fun connectToSession() {
        val sessionManager =
            context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val sessions = sessionManager.getActiveSessions(null)
        if (sessions.isNotEmpty()) {
            mediaController = MediaController(context, sessions[0].sessionToken)
        }
    }

    fun play() {
        mediaController?.transportControls?.play()
    }

    fun pause() {
        mediaController?.transportControls?.pause()
    }

    fun next() {
        mediaController?.transportControls?.skipToNext()
    }

    fun previous() {
        mediaController?.transportControls?.skipToPrevious()
    }
}
