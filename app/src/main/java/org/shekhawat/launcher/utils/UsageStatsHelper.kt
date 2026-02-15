package org.shekhawat.launcher.utils

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import java.util.Calendar

/**
 * Utility for querying Android UsageStatsManager to get screen time data.
 */
object UsageStatsHelper {

    data class AppUsageInfo(
        val packageName: String,
        val appName: String,
        val totalTimeMs: Long
    )

    /**
     * Check if the app has been granted usage stats permission.
     */
    fun hasPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Get total screen time for today in milliseconds.
     */
    fun getTodayScreenTime(context: Context): Long {
        return getPerAppUsage(context, getDayStart()).sumOf { it.totalTimeMs }
    }

    /**
     * Get per-app usage for today, sorted by usage time descending.
     */
    fun getTodayPerAppUsage(context: Context): List<AppUsageInfo> {
        return getPerAppUsage(context, getDayStart())
    }

    /**
     * Get per-app usage for the last 7 days, sorted by usage time descending.
     */
    fun getWeeklyPerAppUsage(context: Context): List<AppUsageInfo> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return getPerAppUsage(context, cal.timeInMillis)
    }

    /**
     * Get the number of device unlocks today.
     */
    fun getTodayUnlockCount(context: Context): Int {
        if (!hasPermission(context)) return 0
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val start = getDayStart()
        val end = System.currentTimeMillis()

        var count = 0
        val events = usm.queryEvents(start, end)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                count++
            }
        }
        return count
    }

    /**
     * Get the package name of the app currently in the foreground.
     * Returns null if it cannot be determined.
     *
     * Uses a 30-second event window to reliably detect the foreground app,
     * even when opened from the Recent Apps screen. Checks both the legacy
     * MOVE_TO_FOREGROUND and the modern ACTIVITY_RESUMED event types.
     */
    fun getCurrentForegroundApp(context: Context): String? {
        if (!hasPermission(context)) return null
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        // 30-second window — wide enough to catch apps opened from Recent Apps
        val start = end - 30_000
        val events = usm.queryEvents(start, end)
        val event = UsageEvents.Event()
        var lastForegroundPkg: String? = null
        var lastForegroundTime = 0L
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            @Suppress("DEPRECATION")
            val isForeground = event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            if (isForeground && event.timeStamp >= lastForegroundTime) {
                lastForegroundPkg = event.packageName
                lastForegroundTime = event.timeStamp
            }
        }
        return lastForegroundPkg
    }

    /**
     * Get usage for a specific app today (in milliseconds).
     */
    fun getAppUsageToday(context: Context, packageName: String): Long {
        return getPerAppUsage(context, getDayStart())
            .find { it.packageName == packageName }
            ?.totalTimeMs ?: 0L
    }

    /**
     * Compute per-app foreground time by walking FOREGROUND_SERVICE-style events.
     *
     * Why not queryUsageStats()?
     *   queryUsageStats(INTERVAL_DAILY) returns totalTimeInForeground as a cumulative
     *   value for the entire bucket. Buckets can span midnight, so "today" queries
     *   still include yesterday's usage. There is no way to get "only since midnight"
     *   from queryUsageStats.
     *
     * Why not raw ACTIVITY_RESUMED / ACTIVITY_PAUSED?
     *   Those fire per-Activity, not per-app. A single app with 3 activities would
     *   generate interleaved resume/pause events, inflating the count. System services
     *   (DNS resolver, content providers) also emit these events.
     *
     * Solution: Use ACTIVITY_RESUMED / ACTIVITY_PAUSED but track state per-PACKAGE.
     *   - When we see a RESUMED for package X and X is not already in the foreground,
     *     record the timestamp.
     *   - When we see a PAUSED for package X, only count it as "left foreground" if
     *     the NEXT event is a RESUMED for a DIFFERENT package (meaning the user actually
     *     switched away). We approximate this by keeping a "currently foreground" package
     *     and only closing out the previous package when a new one takes over.
     *
     * This gives us one continuous foreground session per app, matching what the user
     * actually sees on screen.
     */
    private fun getPerAppUsage(context: Context, startTime: Long): List<AppUsageInfo> {
        if (!hasPermission(context)) return emptyList()

        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val pm = context.packageManager

        val events = usm.queryEvents(startTime, end)
        val event = UsageEvents.Event()

        // Accumulated foreground time per package
        val accumulated = mutableMapOf<String, Long>()
        // The package currently in the foreground and when it started
        var currentFgPkg: String? = null
        var currentFgStart = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue

            @Suppress("DEPRECATION")
            val isResume = event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND

            if (isResume) {
                // A new package came to the foreground
                if (pkg != currentFgPkg) {
                    // Close out the previous foreground package
                    if (currentFgPkg != null && event.timeStamp > currentFgStart) {
                        accumulated[currentFgPkg!!] =
                            (accumulated[currentFgPkg!!] ?: 0L) + (event.timeStamp - currentFgStart)
                    }
                    // Start tracking the new foreground package
                    currentFgPkg = pkg
                    currentFgStart = event.timeStamp
                }
                // If same package resumed again (activity switch within app), ignore — already tracking
            }
        }

        // The app still in the foreground right now — count up to "end"
        if (currentFgPkg != null && end > currentFgStart) {
            accumulated[currentFgPkg!!] =
                (accumulated[currentFgPkg!!] ?: 0L) + (end - currentFgStart)
        }

        // Filter out system framework packages
        val systemPrefixes = listOf(
            "com.android.providers.", "com.android.internal.",
            "com.android.systemui", "com.android.shell",
            "com.android.settings", // settings is system
            "android"
        )

        return accumulated
            .filter { (pkg, timeMs) ->
                timeMs > 60_000 && systemPrefixes.none { prefix ->
                    pkg == prefix || pkg.startsWith("$prefix.")
                }
            }
            .mapNotNull { (pkg, timeMs) ->
                val appName = try {
                    val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0L))
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getApplicationInfo(pkg, 0)
                    }
                    val label = pm.getApplicationLabel(appInfo).toString()
                    if (label == pkg) return@mapNotNull null
                    label
                } catch (_: PackageManager.NameNotFoundException) {
                    return@mapNotNull null
                }
                AppUsageInfo(pkg, appName, timeMs)
            }
            .sortedByDescending { it.totalTimeMs }
    }

    private fun getDayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Format milliseconds to a human-readable string like "2h 15m".
     */
    fun formatDuration(ms: Long): String {
        val totalMinutes = ms / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
