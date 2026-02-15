package org.shekhawat.launcher.utils

import android.app.SearchManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.data.fetchContacts

/**
 * Result of processing a voice command.
 * [displayText] is the feedback shown to the user.
 * [handled] indicates whether a command was recognized.
 */
data class VoiceCommandResult(
    val displayText: String,
    val handled: Boolean
)

/**
 * Handles voice commands spoken by the user on the home screen.
 *
 * Supported commands:
 *  - "open <app>"           → Launch an installed app
 *  - "call <contact/number>"→ Call a contact by name or number
 *  - "text/message <contact>" → Open SMS to a contact
 *  - "search <query>"       → Web search via browser
 *  - "play music" / "pause music" / "next song" / "previous song" → Media controls
 *  - "set alarm <time>"     → Create an alarm (e.g. "set alarm 7 30 am")
 *  - "set timer <minutes>"  → Start a countdown timer
 *  - "take a selfie/photo"  → Open camera
 *  - "flashlight on/off"    → Toggle torch
 *  - "brightness up/down"   → Adjust screen brightness
 *  - "volume up/down/mute"  → Adjust media volume
 *  - "wifi on/off"          → Toggle Wi-Fi
 *  - "bluetooth on/off"     → Toggle Bluetooth
 *  - "battery"              → Show battery info
 *  - "navigate to <place>"  → Open maps navigation
 *  - "send email"           → Open email compose
 *  - "open settings"        → Open device settings
 *  - "what time is it"      → Speak the current time
 *  - "help"                 → Show all available commands
 */
class VoiceCommandHandler(
    private val context: Context,
    private val appList: List<AppInfo>
) {

    fun handle(spokenText: String): VoiceCommandResult {
        val input = spokenText.lowercase().trim()

        return when {
            // --- Help ---
            input == "help" || input == "what can you do" || input == "commands" ->
                helpCommand()

            // --- Open app ---
            input.startsWith("open ") ->
                openAppCommand(input)

            // --- Launch / start (alias for open) ---
            input.startsWith("launch ") || input.startsWith("start ") ->
                openAppCommand(input)

            // --- Call ---
            input.startsWith("call ") ->
                callCommand(input)

            // --- Text / Message ---
            input.startsWith("text ") || input.startsWith("message ") || input.startsWith("sms ") ->
                sendTextCommand(input)

            // --- Web search ---
            input.startsWith("search ") || input.startsWith("google ") || input.startsWith("look up ") ->
                searchCommand(input)

            // --- Navigate / Directions ---
            input.startsWith("navigate to ") || input.startsWith("directions to ") || input.startsWith("take me to ") ->
                navigateCommand(input)

            // --- Set alarm ---
            input.startsWith("set alarm") || input.startsWith("set an alarm") || input.startsWith("wake me up") ->
                setAlarmCommand(input)

            // --- Set timer ---
            input.startsWith("set timer") || input.startsWith("set a timer") || input.startsWith("timer for") || input.startsWith("countdown") ->
                setTimerCommand(input)

            // --- Camera / Selfie ---
            input.contains("selfie") || input.contains("take a photo") || input.contains("take a picture") || input == "camera" || input == "open camera" ->
                cameraCommand()

            // --- Media controls ---
            input == "play music" || input == "play" || input == "resume music" || input == "resume" ->
                mediaCommand("play")
            input == "pause music" || input == "pause" || input == "stop music" || input == "stop" ->
                mediaCommand("pause")
            input == "next song" || input == "next track" || input == "skip" || input == "next" ->
                mediaCommand("next")
            input == "previous song" || input == "previous track" || input == "previous" || input == "go back" ->
                mediaCommand("previous")

            // --- Volume ---
            input.contains("volume up") || input == "louder" ->
                volumeCommand(AudioManager.ADJUST_RAISE)
            input.contains("volume down") || input == "quieter" || input == "softer" ->
                volumeCommand(AudioManager.ADJUST_LOWER)
            input.contains("mute") || input.contains("silent") || input.contains("silence") ->
                volumeCommand(AudioManager.ADJUST_MUTE)
            input.contains("unmute") ->
                volumeCommand(AudioManager.ADJUST_UNMUTE)
            input.contains("volume max") || input.contains("max volume") || input.contains("full volume") ->
                volumeMaxCommand()

            // --- Brightness ---
            input.contains("brightness up") || input.contains("brighter") ->
                brightnessCommand(increase = true)
            input.contains("brightness down") || input.contains("dimmer") || input.contains("dim") ->
                brightnessCommand(increase = false)

            // --- Wi-Fi ---
            input.contains("wifi on") || input.contains("wi-fi on") || input.contains("turn on wifi") ->
                wifiCommand(enable = true)
            input.contains("wifi off") || input.contains("wi-fi off") || input.contains("turn off wifi") ->
                wifiCommand(enable = false)
            input == "wifi" || input == "wifi settings" || input == "wi-fi settings" ->
                openWifiSettings()

            // --- Bluetooth ---
            input.contains("bluetooth on") || input.contains("turn on bluetooth") ->
                bluetoothCommand(enable = true)
            input.contains("bluetooth off") || input.contains("turn off bluetooth") ->
                bluetoothCommand(enable = false)
            input == "bluetooth" || input == "bluetooth settings" ->
                openBluetoothSettings()

            // --- Battery ---
            input.contains("battery") ->
                batteryCommand()

            // --- Settings ---
            input == "settings" || input == "open settings" || input == "device settings" ->
                openDeviceSettings()

            // --- Email ---
            input.startsWith("send email") || input.startsWith("email") || input.startsWith("compose email") ->
                emailCommand()

            // --- What time ---
            input.contains("what time") || input.contains("current time") || input == "time" ->
                timeCommand()

            // --- What date ---
            input.contains("what date") || input.contains("today's date") || input.contains("what day") || input == "date" ->
                dateCommand()

            // --- Calculator ---
            input == "calculator" || input == "open calculator" ->
                openCalculator()

            // --- Unrecognized ---
            else ->
                VoiceCommandResult(
                    displayText = "\"$spokenText\"\nCommand not recognized. Say \"help\" for available commands.",
                    handled = false
                )
        }
    }

    // ─── Command implementations ────────────────────────────────────────────

    private fun helpCommand(): VoiceCommandResult {
        val commands = """
            |Available voice commands:
            |
            |• "open <app>" — Launch any app
            |• "call <name/number>" — Make a phone call
            |• "text <name>" — Send a text message
            |• "search <query>" — Web search
            |• "navigate to <place>" — Maps directions
            |• "set alarm 7 30 am" — Set an alarm
            |• "set timer 5 minutes" — Countdown timer
            |• "take a photo" / "selfie" — Camera
            |• "play" / "pause" / "next" / "previous" — Music
            |• "volume up/down/mute/max"
            |• "wifi on/off" / "bluetooth on/off"
            |• "brightness up/down"
            |• "battery" — Battery info
            |• "settings" — Device settings
            |• "calculator" — Open calculator
            |• "send email" — Compose email
            |• "what time is it" / "what date"
        """.trimMargin()
        return VoiceCommandResult(commands, handled = true)
    }

    private fun openAppCommand(input: String): VoiceCommandResult {
        val keyword = when {
            input.startsWith("open ") -> "open "
            input.startsWith("launch ") -> "launch "
            input.startsWith("start ") -> "start "
            else -> "open "
        }
        val appName = input.removePrefix(keyword).trim().replace(" ", "")
        if (appName.isEmpty()) {
            return VoiceCommandResult("Please specify an app name", handled = false)
        }

        // Try exact match first, then fuzzy contains
        val app = appList.find {
            it.name.trim().replace(" ", "").equals(appName, ignoreCase = true)
        } ?: appList.find {
            it.name.trim().replace(" ", "").contains(appName, ignoreCase = true)
        }

        return if (app != null) {
            app.intent?.let {
                context.startActivity(it)
            }
            VoiceCommandResult("Opening ${app.name}", handled = true)
        } else {
            // Fallback: try searching the Play Store
            VoiceCommandResult("App \"$appName\" not found. Try installing it.", handled = false)
        }
    }

    private fun callCommand(input: String): VoiceCommandResult {
        val searchTerm = input.removePrefix("call ").trim().replace(" ", "")
        if (searchTerm.isEmpty()) {
            return VoiceCommandResult("Please specify a contact name or number", handled = false)
        }

        val contacts = fetchContacts(context)
        val number = contacts.find {
            it.name.replace(" ", "").contains(searchTerm, ignoreCase = true)
        }?.phoneNumber ?: searchTerm

        return if (isValidPhoneNumber(number)) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
            }
            context.startActivity(intent)
            VoiceCommandResult("Calling $number", handled = true)
        } else {
            VoiceCommandResult("Contact \"$searchTerm\" not found", handled = false)
        }
    }

    private fun sendTextCommand(input: String): VoiceCommandResult {
        val keyword = when {
            input.startsWith("text ") -> "text "
            input.startsWith("message ") -> "message "
            input.startsWith("sms ") -> "sms "
            else -> "text "
        }
        val searchTerm = input.removePrefix(keyword).trim()
        if (searchTerm.isEmpty()) {
            return VoiceCommandResult("Please specify a contact name", handled = false)
        }

        val contacts = fetchContacts(context)
        val contact = contacts.find {
            it.name.replace(" ", "").contains(searchTerm.replace(" ", ""), ignoreCase = true)
        }

        return if (contact != null) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${contact.phoneNumber}")
            }
            context.startActivity(intent)
            VoiceCommandResult("Messaging ${contact.name}", handled = true)
        } else {
            // If it looks like a number, text it directly
            val cleaned = searchTerm.replace(" ", "")
            if (isValidPhoneNumber(cleaned)) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$cleaned")
                }
                context.startActivity(intent)
                VoiceCommandResult("Messaging $cleaned", handled = true)
            } else {
                VoiceCommandResult("Contact \"$searchTerm\" not found", handled = false)
            }
        }
    }

    private fun searchCommand(input: String): VoiceCommandResult {
        val keyword = when {
            input.startsWith("search ") -> "search "
            input.startsWith("google ") -> "google "
            input.startsWith("look up ") -> "look up "
            else -> "search "
        }
        val query = input.removePrefix(keyword).trim()
        if (query.isEmpty()) {
            return VoiceCommandResult("Please specify what to search for", handled = false)
        }

        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}"))
            context.startActivity(browserIntent)
        }
        return VoiceCommandResult("Searching for \"$query\"", handled = true)
    }

    private fun navigateCommand(input: String): VoiceCommandResult {
        val keyword = when {
            input.startsWith("navigate to ") -> "navigate to "
            input.startsWith("directions to ") -> "directions to "
            input.startsWith("take me to ") -> "take me to "
            else -> "navigate to "
        }
        val destination = input.removePrefix(keyword).trim()
        if (destination.isEmpty()) {
            return VoiceCommandResult("Please specify a destination", handled = false)
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${Uri.encode(destination)}"))
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to maps web
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?daddr=${Uri.encode(destination)}"))
            context.startActivity(webIntent)
        }
        return VoiceCommandResult("Navigating to \"$destination\"", handled = true)
    }

    private fun setAlarmCommand(input: String): VoiceCommandResult {
        // Extract numbers from the input
        val numbers = Regex("\\d+").findAll(input).map { it.value.toInt() }.toList()

        val isPm = input.contains("pm") || input.contains("p.m")
        val isAm = input.contains("am") || input.contains("a.m")

        return if (numbers.isNotEmpty()) {
            var hour = numbers[0]
            val minute = if (numbers.size > 1) numbers[1] else 0

            // Convert to 24h if AM/PM specified
            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0

            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
            try {
                context.startActivity(intent)
                val timeStr = String.format("%d:%02d %s", if (hour > 12) hour - 12 else if (hour == 0) 12 else hour, minute, if (hour >= 12) "PM" else "AM")
                VoiceCommandResult("Setting alarm for $timeStr", handled = true)
            } catch (_: Exception) {
                VoiceCommandResult("Could not set alarm. No clock app found.", handled = false)
            }
        } else {
            // No time specified, just open alarms
            try {
                context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
                VoiceCommandResult("Opening alarms", handled = true)
            } catch (_: Exception) {
                VoiceCommandResult("Could not open alarms", handled = false)
            }
        }
    }

    private fun setTimerCommand(input: String): VoiceCommandResult {
        val numbers = Regex("\\d+").findAll(input).map { it.value.toInt() }.toList()

        return if (numbers.isNotEmpty()) {
            val minutes = numbers[0]
            val seconds = minutes * 60

            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
            try {
                context.startActivity(intent)
                VoiceCommandResult("Setting timer for $minutes minutes", handled = true)
            } catch (_: Exception) {
                VoiceCommandResult("Could not set timer. No clock app found.", handled = false)
            }
        } else {
            VoiceCommandResult("Please specify how many minutes (e.g. \"set timer 5 minutes\")", handled = false)
        }
    }

    private fun cameraCommand(): VoiceCommandResult {
        return try {
            context.startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            VoiceCommandResult("Opening camera", handled = true)
        } catch (_: Exception) {
            VoiceCommandResult("Could not open camera", handled = false)
        }
    }

    private fun mediaCommand(action: String): VoiceCommandResult {
        return try {
            val mediaHelper = MediaControllerHelper(context)
            mediaHelper.connectToSession()
            when (action) {
                "play" -> {
                    mediaHelper.play()
                    VoiceCommandResult("Playing music", handled = true)
                }
                "pause" -> {
                    mediaHelper.pause()
                    VoiceCommandResult("Music paused", handled = true)
                }
                "next" -> {
                    mediaHelper.next()
                    VoiceCommandResult("Skipping to next track", handled = true)
                }
                "previous" -> {
                    mediaHelper.previous()
                    VoiceCommandResult("Going to previous track", handled = true)
                }
                else -> VoiceCommandResult("Unknown media action", handled = false)
            }
        } catch (_: Exception) {
            VoiceCommandResult("No active media session found", handled = false)
        }
    }

    private fun volumeCommand(direction: Int): VoiceCommandResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
        val label = when (direction) {
            AudioManager.ADJUST_RAISE -> "Volume up"
            AudioManager.ADJUST_LOWER -> "Volume down"
            AudioManager.ADJUST_MUTE -> "Muted"
            AudioManager.ADJUST_UNMUTE -> "Unmuted"
            else -> "Volume adjusted"
        }
        return VoiceCommandResult(label, handled = true)
    }

    private fun volumeMaxCommand(): VoiceCommandResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, AudioManager.FLAG_SHOW_UI)
        return VoiceCommandResult("Volume set to max", handled = true)
    }

    private fun brightnessCommand(increase: Boolean): VoiceCommandResult {
        // Brightness requires WRITE_SETTINGS permission; open the settings panel instead
        return try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
            context.startActivity(intent)
            VoiceCommandResult(if (increase) "Opening display settings to increase brightness" else "Opening display settings to decrease brightness", handled = true)
        } catch (_: Exception) {
            VoiceCommandResult("Could not open display settings", handled = false)
        }
    }

    @Suppress("DEPRECATION")
    private fun wifiCommand(enable: Boolean): VoiceCommandResult {
        // On Android 10+, apps can't toggle Wi-Fi directly; open settings panel
        return try {
            val intent = Intent(Settings.Panel.ACTION_WIFI)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            VoiceCommandResult("Opening Wi-Fi settings", handled = true)
        } catch (_: Exception) {
            openWifiSettings()
        }
    }

    private fun openWifiSettings(): VoiceCommandResult {
        return try {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            VoiceCommandResult("Opening Wi-Fi settings", handled = true)
        } catch (_: Exception) {
            VoiceCommandResult("Could not open Wi-Fi settings", handled = false)
        }
    }

    @Suppress("DEPRECATION")
    private fun bluetoothCommand(enable: Boolean): VoiceCommandResult {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            context.startActivity(intent)
            VoiceCommandResult("Opening Bluetooth settings", handled = true)
        } catch (_: Exception) {
            VoiceCommandResult("Could not open Bluetooth settings", handled = false)
        }
    }

    private fun openBluetoothSettings(): VoiceCommandResult {
        return try {
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            VoiceCommandResult("Opening Bluetooth settings", handled = true)
        } catch (_: Exception) {
            VoiceCommandResult("Could not open Bluetooth settings", handled = false)
        }
    }

    private fun batteryCommand(): VoiceCommandResult {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        val level = bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val charging = if (bm.isCharging) " (charging)" else ""
        return VoiceCommandResult("Battery: $level%$charging", handled = true)
    }

    private fun openDeviceSettings(): VoiceCommandResult {
        return try {
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
            VoiceCommandResult("Opening settings", handled = true)
        } catch (_: Exception) {
            VoiceCommandResult("Could not open settings", handled = false)
        }
    }

    private fun emailCommand(): VoiceCommandResult {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
            }
            context.startActivity(intent)
            VoiceCommandResult("Opening email", handled = true)
        } catch (_: Exception) {
            VoiceCommandResult("No email app found", handled = false)
        }
    }

    private fun timeCommand(): VoiceCommandResult {
        val now = java.time.LocalDateTime.now()
        val hour = now.hour
        val minute = now.minute
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return VoiceCommandResult("It's ${displayHour}:${String.format("%02d", minute)} $amPm", handled = true)
    }

    private fun dateCommand(): VoiceCommandResult {
        val now = java.time.LocalDate.now()
        val dayOfWeek = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val month = now.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return VoiceCommandResult("$dayOfWeek, $month ${now.dayOfMonth}, ${now.year}", handled = true)
    }

    private fun openCalculator(): VoiceCommandResult {
        // Try to find a calculator app
        val app = appList.find {
            it.name.replace(" ", "").contains("calculator", ignoreCase = true) ||
            it.packageName.contains("calculator", ignoreCase = true)
        }
        return if (app?.intent != null) {
            context.startActivity(app.intent)
            VoiceCommandResult("Opening calculator", handled = true)
        } else {
            VoiceCommandResult("Calculator app not found", handled = false)
        }
    }

    companion object {
        fun isValidPhoneNumber(number: String): Boolean {
            return number.length >= 10 && !number.contains("[a-zA-Z]".toRegex())
        }
    }
}
