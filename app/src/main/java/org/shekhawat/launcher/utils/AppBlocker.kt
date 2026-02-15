package org.shekhawat.launcher.utils

import android.content.Context
import android.content.Intent
import org.json.JSONObject
import org.shekhawat.launcher.AppBlockedActivity
import org.shekhawat.launcher.SharedPrefManager

/**
 * Centralized app-blocking gate. Every app launch MUST go through [canLaunch].
 * If the app has exceeded its daily limit, the blocked screen is shown immediately.
 * No exceptions, no snooze.
 */
object AppBlocker {

    /**
     * Returns true if the app is allowed to launch.
     * Returns false and shows the blocked screen if the limit is exceeded.
     */
    fun canLaunch(context: Context, packageName: String): Boolean {
        // Strip work profile suffix for limit lookup
        val realPkg = packageName.removeSuffix("#work")

        if (!UsageStatsHelper.hasPermission(context)) return true // Can't enforce without permission

        val prefs = SharedPrefManager(context)
        val limitsStr = prefs.getString("app_limits", "{}")
        val limitMinutes = try {
            val json = JSONObject(limitsStr)
            if (json.has(realPkg)) json.getInt(realPkg) else -1
        } catch (_: Exception) {
            -1
        }

        if (limitMinutes <= 0) return true // No limit set

        val usageMs = UsageStatsHelper.getAppUsageToday(context, realPkg)
        val usageMinutes = usageMs / 60_000

        if (usageMinutes >= limitMinutes) {
            // Block — show the blocked screen immediately
            val blockedIntent = Intent(context, AppBlockedActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("package_name", realPkg)
                putExtra("limit_minutes", limitMinutes)
                putExtra("used_minutes", usageMinutes.toInt())
            }
            context.startActivity(blockedIntent)
            return false
        }

        return true
    }
}
