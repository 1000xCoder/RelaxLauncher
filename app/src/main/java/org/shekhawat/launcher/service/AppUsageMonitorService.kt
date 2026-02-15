package org.shekhawat.launcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import org.json.JSONObject
import org.shekhawat.launcher.AppBlockedActivity
import org.shekhawat.launcher.R
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.utils.UsageStatsHelper

/**
 * Foreground service that periodically checks app usage against configured limits.
 * When an app exceeds its daily limit, it launches AppBlockedActivity.
 *
 * Runs every 3 seconds so that even apps opened from the Recent Apps screen
 * are caught almost immediately.
 */
class AppUsageMonitorService : Service() {

    companion object {
        private const val CHANNEL_ID = "app_usage_monitor"
        private const val NOTIFICATION_ID = 1001
        /**
         * 3-second interval — fast enough to catch apps opened from Recent Apps
         * before the user gets meaningful usage time.
         */
        private const val CHECK_INTERVAL_MS = 3_000L

        fun start(context: Context) {
            val intent = Intent(context, AppUsageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AppUsageMonitorService::class.java))
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sharedPrefManager: SharedPrefManager

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkAppLimits()
            handler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sharedPrefManager = SharedPrefManager(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        handler.post(checkRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        super.onDestroy()
    }

    private fun checkAppLimits() {
        if (!UsageStatsHelper.hasPermission(this)) return

        val limitsStr = sharedPrefManager.getString("app_limits", "{}")
        val limits = try {
            val json = JSONObject(limitsStr)
            json.keys().asSequence().map { key ->
                key to json.optInt(key, 0)
            }.toList()
        } catch (_: Exception) {
            emptyList()
        }

        if (limits.isEmpty()) return

        // Determine which app is currently in the foreground.
        // This is critical: we only block if the user is actively inside the app,
        // not when they're on the home screen or in a different app.
        val currentForeground = UsageStatsHelper.getCurrentForegroundApp(this)

        // Skip if we can't determine the foreground app or if it's the launcher itself
        if (currentForeground == null || currentForeground == packageName) return

        val now = System.currentTimeMillis()

        for ((pkg, limitMinutes) in limits) {
            if (limitMinutes <= 0) continue

            // Only block if this specific app is in the foreground right now
            if (currentForeground != pkg) continue

            val usageMs = UsageStatsHelper.getAppUsageToday(this, pkg)
            val usageMinutes = usageMs / 60_000

            if (usageMinutes >= limitMinutes) {
                // Throttle: don't re-block within 3 seconds to avoid rapid-fire
                val lastBlocked = sharedPrefManager.getLong("app_limit_last_blocked_$pkg", 0L)
                if (now - lastBlocked < 3_000) continue

                sharedPrefManager.saveLong("app_limit_last_blocked_$pkg", now)

                // Launch the blocked activity — no exceptions, no matter how the app was opened
                val blockedIntent = Intent(this, AppBlockedActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("package_name", pkg)
                    putExtra("limit_minutes", limitMinutes)
                    putExtra("used_minutes", usageMinutes.toInt())
                }
                startActivity(blockedIntent)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Usage Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors app usage against configured time limits"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("Relax Launcher")
            .setContentText("Monitoring app usage limits")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }
}
