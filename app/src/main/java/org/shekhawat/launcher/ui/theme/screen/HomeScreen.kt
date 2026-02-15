package org.shekhawat.launcher.ui.theme.screen

import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.content.ComponentName
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.app.NotificationManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.R
import org.shekhawat.launcher.SettingActivity
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.permissions.RequestMicrophonePermission
import org.shekhawat.launcher.service.NotificationListener
import org.shekhawat.launcher.service.RingtonePlayer
import org.shekhawat.launcher.utils.SpeechRecognizerHelper
import org.shekhawat.launcher.utils.UsageStatsHelper
import org.shekhawat.launcher.utils.VoiceCommandHandler
import java.time.LocalDateTime

@Composable
fun HomeScreen(appList: List<AppInfo>) {
    val context = LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }

    // Read settings reactively
    val use24h by sharedPrefManager.observeBoolean("use_24h_clock", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("use_24h_clock", true))
    val showBattery by sharedPrefManager.observeBoolean("show_battery_percentage", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_battery_percentage", true))
    val swipeDownNotif by sharedPrefManager.observeBoolean("swipe_down_notif", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("swipe_down_notif", true))
    val dateFormat by sharedPrefManager.observeString("date_format", "MMM, d EEE")
        .collectAsState(initial = sharedPrefManager.getString("date_format", "MMM, d EEE"))

    // Notification badge
    val showNotifBadge by sharedPrefManager.observeBoolean("show_notif_badge", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_notif_badge", true))
    val notificationItems by NotificationListener.notifications.collectAsState()
    var showNotifDigest by remember { mutableStateOf(false) }
    var showScreenTimeSheet by remember { mutableStateOf(false) }
    var showToolsSheet by remember { mutableStateOf(false) }

    // Icon shape (shared with app drawer)
    val iconShapePref by sharedPrefManager.observeString("icon_shape", "Default")
        .collectAsState(initial = sharedPrefManager.getString("icon_shape", "Default"))

    // Music animation setting
    val musicAnimStyle by sharedPrefManager.observeString("music_anim_style", "Floating Notes")
        .collectAsState(initial = sharedPrefManager.getString("music_anim_style", "Floating Notes"))

    // Screen time badge on home
    var todayScreenTimeMs by remember { mutableLongStateOf(0L) }

    // Break reminder
    val breakReminderStr by sharedPrefManager.observeString("break_reminder_minutes", "120")
        .collectAsState(initial = sharedPrefManager.getString("break_reminder_minutes", "120"))
    var showBreakReminder by remember { mutableStateOf(false) }
    var breakReminderDismissed by remember { mutableStateOf(false) }

    // Favorite apps
    val favoriteAppsStr by sharedPrefManager.observeString("favorite_apps", "")
        .collectAsState(initial = sharedPrefManager.getString("favorite_apps", ""))
    val favoriteApps = remember(favoriteAppsStr, appList) {
        val favPkgs = favoriteAppsStr.split(",").filter { it.isNotBlank() }.toSet()
        appList.filter { it.packageName in favPkgs }
    }

    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    var isCharging by remember { mutableStateOf(bm.isCharging) }
    var batteryPercentage by remember {
        mutableIntStateOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))
    }
    var time by remember { mutableStateOf(LocalDateTime.now()) }
    var hasPermission by remember { mutableStateOf(false) }

    // Now playing state
    var nowPlayingTitle by remember { mutableStateOf<String?>(null) }
    var nowPlayingArtist by remember { mutableStateOf<String?>(null) }
    var nowPlayingArt by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // ComponentName for our NotificationListenerService — required for getActiveSessions
    val listenerComponent = remember {
        ComponentName(context, org.shekhawat.launcher.service.NotificationListener::class.java)
    }

    RequestMicrophonePermission(
        context = context,
        onPermissionResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    // Track last known title to avoid re-fetching album art bitmaps every tick
    val lastMediaTitle = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            batteryPercentage = withContext(Dispatchers.Default) {
                bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            }
            time = withContext(Dispatchers.Default) {
                LocalDateTime.now()
            }
            isCharging = withContext(Dispatchers.Default) {
                bm.isCharging
            }
            // Poll screen time (expensive — do every 5 ticks = 10s)
            if (time.second % 10 < 2) {
                withContext(Dispatchers.IO) {
                    if (UsageStatsHelper.hasPermission(context)) {
                        todayScreenTimeMs = UsageStatsHelper.getTodayScreenTime(context)
                    }
                }
            }
            // Check break reminder
            val breakMinutes = breakReminderStr.toIntOrNull()
            if (breakMinutes != null && !breakReminderDismissed && todayScreenTimeMs > 0) {
                val usedMinutes = todayScreenTimeMs / 60_000
                showBreakReminder = usedMinutes >= breakMinutes
            }
            // Poll active media session for now-playing info
            withContext(Dispatchers.Default) {
                try {
                    val sessionManager =
                        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
                    val sessions: List<MediaController> = try {
                        sessionManager.getActiveSessions(listenerComponent)
                    } catch (_: SecurityException) {
                        try { sessionManager.getActiveSessions(null) } catch (_: SecurityException) { emptyList() }
                    }
                    if (sessions.isNotEmpty()) {
                        val controller = sessions[0]
                        val metadata = controller.metadata
                        val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
                        val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
                        val playing = controller.playbackState?.state == PlaybackState.STATE_PLAYING
                        val newTitle = title ?: if (playing) "Unknown" else null
                        nowPlayingTitle = newTitle
                        nowPlayingArtist = artist
                        isPlaying = playing
                        // Only fetch album art bitmap when the track actually changes
                        if (newTitle != lastMediaTitle.value) {
                            lastMediaTitle.value = newTitle
                            nowPlayingArt = metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
                                ?: metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ART)
                        }
                    } else {
                        if (nowPlayingTitle != null) {
                            nowPlayingTitle = null
                            nowPlayingArtist = null
                            nowPlayingArt = null
                            isPlaying = false
                            lastMediaTitle.value = null
                        }
                    }
                } catch (_: Exception) {
                    // Notification listener not granted or other error
                }
            }
            delay(2000)
        }
    }

    // Background tick for Pomodoro & Timer (keeps state alive when sheet is closed)
    val pomodoroRunningHome by ToolsState.pomodoroRunning.collectAsState()
    val timerRunningHome by ToolsState.timerRunning.collectAsState()
    val playPomodoroSound = remember(context) { SharedPrefManager(context).getBoolean("pomodoro_sound", true) }

    LaunchedEffect(pomodoroRunningHome) {
        while (pomodoroRunningHome) {
            val totalSec = ToolsState.pomodoroTotalSeconds.value
            val st = ToolsState.pomodoroStartTime.value
            val nowSec = java.time.LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC)
            val startSec = st.toEpochSecond(java.time.ZoneOffset.UTC)
            val elapsed = nowSec - startSec
            val rem = (totalSec - elapsed).coerceAtLeast(0L)
            ToolsState.updatePomodoroRemaining(rem)

            if (rem <= 0L) {
                if (playPomodoroSound) {
                    try {
                        val intent = Intent(context, RingtonePlayer::class.java)
                        context.startService(intent)
                    } catch (_: Exception) { }
                }
                delay(2000)
                ToolsState.pomodoroCompleted(totalSec)
            }
            delay(1000)
        }
    }

    LaunchedEffect(timerRunningHome) {
        while (timerRunningHome) {
            val nowSec = java.time.LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC)
            val startSec = ToolsState.timerStartTime.value.toEpochSecond(java.time.ZoneOffset.UTC)
            val elapsed = ToolsState.timerPausedElapsed.value + (nowSec - startSec)
            ToolsState.updateTimerElapsed(elapsed)
            delay(1000)
        }
    }

    // Voice command handler
    val voiceCommandHandler = remember(appList) {
        VoiceCommandHandler(context, appList)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double-tap to lock screen
                        try {
                            val dpm =
                                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                            dpm.lockNow()
                        } catch (_: SecurityException) {
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_SECURITY_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            } catch (_: Exception) { }
                        }
                    }
                )
            }
            .pointerInput(swipeDownNotif) {
                if (swipeDownNotif) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                            if (totalDrag > 100) {
                                totalDrag = 0f // reset to avoid repeated triggers
                                // Swipe down — expand notification shade
                                try {
                                    @Suppress("WrongConstant")
                                    val statusBarService = context.getSystemService("statusbar")
                                    val clazz = Class.forName("android.app.StatusBarManager")
                                    val method = clazz.getMethod("expandNotificationsPanel")
                                    method.invoke(statusBarService)
                                } catch (_: Exception) { }
                            }
                        }
                    )
                }
            }
    ) {
        // Music ambient animation (behind all content)
        if (isPlaying && musicAnimStyle != "Off") {
            MusicAmbientAnimation(
                style = musicAnimStyle,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Battery + Time display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Battery ring + time
            Box(
                modifier = Modifier.padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showBattery) {
                    CircularProgressIndicator(
                        progress = { batteryPercentage / 100.0f },
                        modifier = Modifier
                            .size(200.dp)
                            .animateContentSize(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    )
                }
                CompactTimeDisplay(
                    batteryPercentage = batteryPercentage,
                    isCharging = isCharging,
                    time = time,
                    use24h = use24h,
                    showBattery = showBattery,
                    dateFormat = dateFormat
                )
            }

            // Screen time + notification badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Screen time badge – tap to open screen time sheet
                if (todayScreenTimeMs > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f))
                            .clickable { showScreenTimeSheet = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Screen: ${UsageStatsHelper.formatDuration(todayScreenTimeMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }

                if (todayScreenTimeMs > 0 && showNotifBadge && notificationItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Notification badge
                if (showNotifBadge && notificationItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f))
                            .clickable { showNotifDigest = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${notificationItems.size} notifications",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Now Playing widget — no enter/exit animation to avoid replaying on page revisit
            if (nowPlayingTitle != null) {
                NowPlayingWidget(
                    title = nowPlayingTitle ?: "",
                    artist = nowPlayingArtist,
                    albumArt = nowPlayingArt,
                    isPlaying = isPlaying,
                    onPlayPause = {
                        try {
                            val sessionManager =
                                context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
                            val sessions = try {
                                sessionManager.getActiveSessions(listenerComponent)
                            } catch (_: SecurityException) {
                                try { sessionManager.getActiveSessions(null) } catch (_: SecurityException) { emptyList() }
                            }
                            if (sessions.isNotEmpty()) {
                                val controls = sessions[0].transportControls
                                if (isPlaying) controls.pause() else controls.play()
                            }
                        } catch (_: Exception) { }
                    },
                    onNext = {
                        try {
                            val sessionManager =
                                context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
                            val sessions = try {
                                sessionManager.getActiveSessions(listenerComponent)
                            } catch (_: SecurityException) {
                                try { sessionManager.getActiveSessions(null) } catch (_: SecurityException) { emptyList() }
                            }
                            if (sessions.isNotEmpty()) {
                                sessions[0].transportControls.skipToNext()
                            }
                        } catch (_: Exception) { }
                    },
                    onPrevious = {
                        try {
                            val sessionManager =
                                context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
                            val sessions = try {
                                sessionManager.getActiveSessions(listenerComponent)
                            } catch (_: SecurityException) {
                                try { sessionManager.getActiveSessions(null) } catch (_: SecurityException) { emptyList() }
                            }
                            if (sessions.isNotEmpty()) {
                                sessions[0].transportControls.skipToPrevious()
                            }
                        } catch (_: Exception) { }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick settings toggles
            QuickSettingsRow(context)

            Spacer(modifier = Modifier.height(8.dp))

            // Active Pomodoro / Timer / Tools chips
            ActiveToolsRow(onOpenTools = { showToolsSheet = true })

            // Break reminder banner — static, no animation loop
            if (showBreakReminder && !breakReminderDismissed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                        .clickable { breakReminderDismissed = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Time for a break!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "You've been on your phone for ${UsageStatsHelper.formatDuration(todayScreenTimeMs)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Favorite apps row
            if (favoriteApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FavoriteAppsRow(favoriteApps = favoriteApps, context = context, iconShape = iconShapePref)
            }
        }

        // Speech recognition
        val isListening = remember { mutableStateOf(false) }
        var resultText by remember { mutableStateOf("") }
        var showHelp by remember { mutableStateOf(false) }

        val speechRecognizerHelper = remember {
            SpeechRecognizerHelper(context, isListening) { result ->
                val commandResult = voiceCommandHandler.handle(result)

                val isHelpCommand = result.lowercase().trim().let {
                    it == "help" || it == "what can you do" || it == "commands"
                }
                showHelp = isHelpCommand
                // Don't show result text for help — the full-screen overlay handles it
                resultText = if (isHelpCommand) "" else commandResult.displayText
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                speechRecognizerHelper.destroy()
            }
        }

        // Help overlay — full-screen scrim with a scrollable card
        AnimatedVisibility(
            visible = showHelp,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f))
                    .clickable { showHelp = false }
                    .padding(horizontal = 20.dp, vertical = 48.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    VoiceCommandHelpContent()
                }
            }
        }

        // Mic button + result area
        Column(
            modifier = Modifier
                .padding(bottom = 100.dp, start = 32.dp, end = 32.dp)
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .size(if (isListening.value) 40.dp else 32.dp)
                    .animateContentSize()
                    .clickable {
                        if (hasPermission) {
                            if (isListening.value) {
                                isListening.value = false
                                speechRecognizerHelper.stopListening()
                            } else {
                                isListening.value = true
                                resultText = ""
                                showHelp = false
                                speechRecognizerHelper.startListening()
                            }
                        }
                    },
                painter = painterResource(id = R.drawable.microphone),
                contentDescription = "Mic",
                tint = if (isListening.value)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.onPrimary
            )

            Text(
                text = when {
                    isListening.value -> "Listening..."
                    !hasPermission -> "Microphone permission required"
                    else -> "Try: \"open YouTube\", \"search weather\", \"help\""
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            if (resultText.isNotEmpty() && !showHelp) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom quick actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_DIAL))
                    },
                painter = painterResource(id = R.drawable.telephone),
                contentDescription = "Phone",
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        try {
                            // Try the camera capture intent first
                            context.startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                        } catch (_: SecurityException) {
                            // CAMERA permission revoked — fall back to opening the camera app directly
                            val launchIntent = context.packageManager
                                .getLaunchIntentForPackage("com.android.camera")
                                ?: context.packageManager
                                    .getLaunchIntentForPackage("com.android.camera2")
                            if (launchIntent != null) {
                                try { context.startActivity(launchIntent) } catch (_: Exception) {}
                            }
                        } catch (_: Exception) { }
                    },
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "Camera",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Notification Digest Sheet
        if (showNotifDigest) {
            NotificationDigestSheet(
                notifications = notificationItems,
                onDismiss = { showNotifDigest = false }
            )
        }

        // Screen Time Sheet
        if (showScreenTimeSheet) {
            ScreenTimeSheet(
                onDismiss = { showScreenTimeSheet = false }
            )
        }

        // Tools Sheet (Pomodoro / Timer / Clock)
        if (showToolsSheet) {
            ToolsSheet(
                onDismiss = { showToolsSheet = false }
            )
        }
    }
}

// ---------- Favorite Apps Grid ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoriteAppsRow(favoriteApps: List<AppInfo>, context: Context, iconShape: String = "Default") {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 5
    ) {
        favoriteApps.forEach { appInfo ->
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .clickable {
                        if (appInfo.name == "App Settings") {
                            context.startActivity(Intent(context, SettingActivity::class.java))
                        } else if (!org.shekhawat.launcher.utils.AppBlocker.canLaunch(context, appInfo.packageName)) {
                            // Blocked — AppBlocker already showed the blocked screen
                        } else if (appInfo.isWorkProfile && appInfo.componentName != null && appInfo.userHandle != null) {
                            try {
                                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
                                launcherApps.startMainActivity(appInfo.componentName, appInfo.userHandle, null, null)
                            } catch (_: Exception) { }
                        } else {
                            appInfo.intent?.let { intent ->
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        }
                    }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val favIconShape = iconShapeFor(iconShape)
                Image(
                    bitmap = appInfo.icon,
                    contentDescription = appInfo.name,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(46.dp)
                        .then(
                            if (favIconShape != null) Modifier.clip(favIconShape)
                            else Modifier
                        )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = appInfo.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ---------- Now Playing Widget (Compact / Expandable) ----------

@Composable
private fun NowPlayingWidget(
    title: String,
    artist: String?,
    albumArt: android.graphics.Bitmap?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val accentColor = MaterialTheme.colorScheme.onPrimary
    var expanded by remember { mutableStateOf(false) }

    // Sleep timer state
    var sleepTimerMinutes by remember { mutableIntStateOf(0) } // 0 = off
    var sleepTimerRemaining by remember { mutableLongStateOf(0L) }

    // Sleep timer countdown
    LaunchedEffect(sleepTimerMinutes) {
        if (sleepTimerMinutes > 0) {
            sleepTimerRemaining = sleepTimerMinutes * 60L
            while (sleepTimerRemaining > 0) {
                delay(1000)
                sleepTimerRemaining--
            }
            // Timer expired — pause music
            onPlayPause()
            sleepTimerMinutes = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(contentColor.copy(alpha = 0.08f))
            .animateContentSize()
            .clickable { expanded = !expanded }
    ) {
        // Compact row: album art + title + play/pause
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = if (expanded) 4.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art — small
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(contentColor.copy(alpha = 0.12f))
                    .border(1.dp, contentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (albumArt != null) {
                    Image(
                        bitmap = albumArt.asImageBitmap(),
                        contentDescription = "Album art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = contentColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Track info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isPlaying) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MiniEqualizer(color = accentColor)
                    }
                }
                if (!artist.isNullOrBlank()) {
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp
                    )
                }
            }

            // Compact play/pause
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f))
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Expanded controls
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // Full transport controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Play/Pause — hero
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(contentColor.copy(alpha = 0.18f))
                            .border(1.5.dp, contentColor.copy(alpha = 0.25f), CircleShape)
                            .clickable(onClick = onPlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Next
                    IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Sleep timer row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sleepOptions = listOf(0, 5, 10, 15, 30, 60)
                    sleepOptions.forEach { mins ->
                        val isActive = sleepTimerMinutes == mins
                        val label = if (mins == 0) "Off" else "${mins}m"
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isActive) contentColor.copy(alpha = 0.2f)
                                    else contentColor.copy(alpha = 0.06f)
                                )
                                .clickable { sleepTimerMinutes = mins }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive) contentColor else contentColor.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Sleep timer countdown display
                if (sleepTimerMinutes > 0 && sleepTimerRemaining > 0) {
                    val m = sleepTimerRemaining / 60
                    val s = sleepTimerRemaining % 60
                    Text(
                        text = "Sleep in ${String.format("%02d:%02d", m, s)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ---------- Music Ambient Animation Styles ----------

val MUSIC_ANIM_STYLES = listOf("Floating Notes", "Pulse Glow", "Sound Waves", "Particles", "Off")

@Composable
private fun MusicAmbientAnimation(style: String, color: Color) {
    when (style) {
        "Floating Notes" -> FloatingNotesAnimation(color)
        "Pulse Glow" -> PulseGlowAnimation(color)
        "Sound Waves" -> SoundWavesAnimation(color)
        "Particles" -> ParticlesAnimation(color)
    }
}

@Composable
private fun FloatingNotesAnimation(color: Color) {
    val noteSymbols = listOf("♪", "♫", "♩", "♬")
    // Use 6 floating notes with staggered timing
    data class NoteState(val xFraction: Float, val delayMs: Int, val durationMs: Int, val symbol: String)
    val notes = remember {
        listOf(
            NoteState(0.15f, 0, 4000, noteSymbols[0]),
            NoteState(0.35f, 700, 3500, noteSymbols[1]),
            NoteState(0.55f, 1400, 4200, noteSymbols[2]),
            NoteState(0.75f, 400, 3800, noteSymbols[3]),
            NoteState(0.25f, 1100, 3600, noteSymbols[0]),
            NoteState(0.65f, 1800, 4100, noteSymbols[1]),
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "floatingNotes")

    Box(modifier = Modifier.fillMaxSize()) {
        notes.forEachIndexed { index, note ->
            val yAnim by infiniteTransition.animateFloat(
                initialValue = 1.1f,
                targetValue = -0.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = note.durationMs,
                        delayMillis = note.delayMs,
                        easing = androidx.compose.animation.core.LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "noteY$index"
            )
            val alphaAnim by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = note.durationMs / 2,
                        delayMillis = note.delayMs,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "noteAlpha$index"
            )
            val swayAnim by infiniteTransition.animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000 + index * 200,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "noteSway$index"
            )

            androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val xPx = maxWidth * note.xFraction + swayAnim.dp
                val yPx = maxHeight * yAnim
                Text(
                    text = note.symbol,
                    color = color.copy(alpha = alphaAnim * 0.15f),
                    fontSize = (16 + index * 2).sp,
                    modifier = Modifier
                        .padding(start = xPx.coerceAtLeast(0.dp), top = yPx.coerceAtLeast(0.dp))
                )
            }
        }
    }
}

@Composable
private fun PulseGlowAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse2"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height * 0.35f
        val maxRadius = size.width * 0.45f

        // Outer glow ring
        drawCircle(
            color = color.copy(alpha = 0.04f * scale1),
            radius = maxRadius * scale1,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
        // Inner glow ring
        drawCircle(
            color = color.copy(alpha = 0.06f * scale2),
            radius = maxRadius * 0.6f * scale2,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
        // Core glow
        drawCircle(
            color = color.copy(alpha = 0.03f),
            radius = maxRadius * 0.3f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    }
}

@Composable
private fun SoundWavesAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundWaves")

    // 5 bars with staggered animations
    val barAnimations = (0 until 5).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.7f + (i % 3) * 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 600 + i * 120,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave$i"
        )
    }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = size.width * 0.04f
        val spacing = size.width * 0.06f
        val totalWidth = barWidth * 5 + spacing * 4
        val startX = (size.width - totalWidth) / 2
        val centerY = size.height * 0.85f
        val maxBarHeight = size.height * 0.12f

        barAnimations.forEachIndexed { i, anim ->
            val barHeight = maxBarHeight * anim.value
            drawRoundRect(
                color = color.copy(alpha = 0.08f + anim.value * 0.06f),
                topLeft = androidx.compose.ui.geometry.Offset(
                    startX + i * (barWidth + spacing),
                    centerY - barHeight / 2
                ),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

@Composable
private fun ParticlesAnimation(color: Color) {
    data class Particle(val xFrac: Float, val yStart: Float, val duration: Int, val size: Float)
    val particles = remember {
        (0 until 12).map {
            Particle(
                xFrac = (it * 0.08f + 0.04f) % 1f,
                yStart = 0.3f + (it % 4) * 0.15f,
                duration = 3000 + (it % 5) * 500,
                size = 2f + (it % 3) * 1.5f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val anims = particles.mapIndexed { i, p ->
        val yAnim by infiniteTransition.animateFloat(
            initialValue = p.yStart,
            targetValue = p.yStart - 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(p.duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "particleY$i"
        )
        val alphaAnim by infiniteTransition.animateFloat(
            initialValue = 0.05f,
            targetValue = 0.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(p.duration / 2 + i * 100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "particleA$i"
        )
        Triple(p, yAnim, alphaAnim)
    }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        anims.forEach { (particle, yFrac, alpha) ->
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = particle.size.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(
                    size.width * particle.xFrac,
                    size.height * yFrac
                )
            )
        }
    }
}

// ---------- Mini Equalizer Animation ----------

@Composable
private fun MiniEqualizer(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bar3"
    )

    Row(
        modifier = Modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(bar1, bar2, bar3).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color.copy(alpha = 0.6f))
            )
        }
    }
}

// ---------- Quick Settings Toggles ----------

// All available quick setting IDs
val ALL_QUICK_SETTINGS = listOf("wifi", "bluetooth", "torch", "ringer", "alarm", "mobile_data", "dnd")
val QUICK_SETTING_LABELS = mapOf(
    "wifi" to "Wi-Fi",
    "bluetooth" to "Bluetooth",
    "torch" to "Torch",
    "ringer" to "Ringer",
    "alarm" to "Alarm",
    "mobile_data" to "Mobile Data",
    "dnd" to "DND"
)
private val DEFAULT_QUICK_SETTINGS = "wifi,bluetooth,torch,ringer,alarm,mobile_data"

@Composable
private fun QuickSettingsRow(context: Context) {
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }
    val enabledStr by sharedPrefManager.observeString("quick_settings_items", DEFAULT_QUICK_SETTINGS)
        .collectAsState(initial = sharedPrefManager.getString("quick_settings_items", DEFAULT_QUICK_SETTINGS))
    val enabledItems = remember(enabledStr) {
        enabledStr.split(",").filter { it.isNotBlank() }
    }

    // ── Wi-Fi state ──
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    var wifiOn by remember {
        val initial = try {
            val network = connectivityManager.activeNetwork
            val caps = network?.let { connectivityManager.getNetworkCapabilities(it) }
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } catch (_: SecurityException) {
            false
        }
        mutableStateOf(initial)
    }

    DisposableEffect(connectivityManager) {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { wifiOn = true }
            override fun onLost(network: Network) { wifiOn = false }
        }
        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (_: SecurityException) { }
        onDispose {
            try { connectivityManager.unregisterNetworkCallback(callback) } catch (_: Exception) { }
        }
    }

    // ── Mobile data state ──
    var mobileDataOn by remember {
        val initial = try {
            val network = connectivityManager.activeNetwork
            val caps = network?.let { connectivityManager.getNetworkCapabilities(it) }
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        } catch (_: SecurityException) {
            false
        }
        mutableStateOf(initial)
    }

    DisposableEffect(connectivityManager) {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { mobileDataOn = true }
            override fun onLost(network: Network) { mobileDataOn = false }
        }
        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (_: SecurityException) { }
        onDispose {
            try { connectivityManager.unregisterNetworkCallback(callback) } catch (_: Exception) { }
        }
    }

    // ── Bluetooth state ──
    var btOn by remember {
        val initial = try {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            btManager?.adapter?.isEnabled == true
        } catch (_: SecurityException) {
            false
        }
        mutableStateOf(initial)
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    btOn = state == BluetoothAdapter.STATE_ON
                }
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        try {
            context.registerReceiver(receiver, filter)
        } catch (_: SecurityException) { }
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: Exception) { }
        }
    }

    // ── Torch state ──
    var torchOn by remember { mutableStateOf(false) }
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }

    DisposableEffect(cameraManager) {
        val callback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                torchOn = enabled
            }
        }
        cameraManager.registerTorchCallback(callback, null)
        onDispose { cameraManager.unregisterTorchCallback(callback) }
    }

    // ── Ringer mode state ──
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var isSilent by remember {
        mutableStateOf(audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT)
    }

    // ── DND state ──
    val notificationManager = remember {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    var isDndOn by remember {
        mutableStateOf(
            try {
                notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE ||
                notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY
            } catch (_: Exception) { false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        enabledItems.forEach { item ->
            when (item) {
                "wifi" -> QuickSettingChip(
                    label = "", active = wifiOn,
                    icon = if (wifiOn) Icons.Default.Wifi else Icons.Default.WifiOff
                ) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            context.startActivity(Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } else {
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    } catch (_: Exception) { }
                }
                "bluetooth" -> QuickSettingChip(
                    label = "", active = btOn,
                    icon = if (btOn) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled
                ) {
                    try {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (_: Exception) { }
                }
                "torch" -> QuickSettingChip(
                    label = "", active = torchOn,
                    icon = if (torchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff
                ) {
                    try {
                        val cameraId = cameraManager.cameraIdList[0]
                        val newState = !torchOn
                        cameraManager.setTorchMode(cameraId, newState)
                        torchOn = newState
                    } catch (_: Exception) { }
                }
                "ringer" -> QuickSettingChip(
                    label = "", active = isSilent,
                    icon = if (isSilent) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp
                ) {
                    try {
                        if (isSilent) {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                            isSilent = false
                        } else {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                            isSilent = true
                        }
                    } catch (_: Exception) {
                        try {
                            context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (_: Exception) { }
                    }
                }
                "alarm" -> QuickSettingChip(
                    label = "", active = false, icon = Icons.Default.Alarm
                ) {
                    try {
                        context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
                    } catch (_: Exception) {
                        try {
                            context.startActivity(Intent(AlarmClock.ACTION_SET_ALARM).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (_: Exception) { }
                    }
                }
                "mobile_data" -> QuickSettingChip(
                    label = "", active = mobileDataOn,
                    icon = if (mobileDataOn) Icons.Default.SignalCellularAlt else Icons.Default.SignalCellularOff
                ) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            context.startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } else {
                            context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    } catch (_: Exception) {
                        try {
                            context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (_: Exception) { }
                    }
                }
                "dnd" -> QuickSettingChip(
                    label = "", active = isDndOn,
                    icon = if (isDndOn) Icons.Default.DoNotDisturbOn else Icons.Default.DoNotDisturb
                ) {
                    try {
                        if (notificationManager.isNotificationPolicyAccessGranted) {
                            if (isDndOn) {
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                                isDndOn = false
                            } else {
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                                isDndOn = true
                            }
                        } else {
                            // Request DND access
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    } catch (_: Exception) {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } catch (_: Exception) { }
                    }
                }
            }
        }
    }
}

// ---------- Active Tools Row (Pomodoro / Timer live status + Tools chip) ----------

@Composable
private fun ActiveToolsRow(onOpenTools: () -> Unit) {
    val pomodoroRunning by ToolsState.pomodoroRunning.collectAsState()
    val pomodoroRemaining by ToolsState.pomodoroRemaining.collectAsState()
    val timerRunning by ToolsState.timerRunning.collectAsState()
    val timerElapsed by ToolsState.timerElapsed.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active Pomodoro chip
        if (pomodoroRunning) {
            val min = pomodoroRemaining / 60
            val sec = pomodoroRemaining % 60
            QuickSettingChip(
                label = String.format("%02d:%02d", min, sec),
                active = true,
                icon = Icons.Default.SelfImprovement
            ) {
                onOpenTools()
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Active Timer chip
        if (timerRunning) {
            val h = timerElapsed / 3600
            val m = (timerElapsed % 3600) / 60
            val s = timerElapsed % 60
            QuickSettingChip(
                label = String.format("%02d:%02d:%02d", h, m, s),
                active = true,
                icon = Icons.Default.Timer
            ) {
                onOpenTools()
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Tools chip (always visible)
        QuickSettingChip(
            label = "Tools",
            active = pomodoroRunning || timerRunning,
            icon = Icons.Default.Handyman
        ) {
            onOpenTools()
        }
    }
}

@Composable
internal fun QuickSettingChip(
    label: String,
    active: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val contentColor = if (active) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (active) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = if (icon != null && label.isEmpty()) 10.dp else 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label.ifEmpty { null },
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                if (label.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontSize = 11.sp
            )
        }
    }
}

// ---------- Compact Time Display (inside battery ring) ----------

@Composable
fun CompactTimeDisplay(
    batteryPercentage: Int,
    isCharging: Boolean,
    time: LocalDateTime,
    use24h: Boolean,
    showBattery: Boolean,
    dateFormat: String = "MMM, d EEE"
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showBattery) {
            Text(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY))
                    },
                text = "${batteryPercentage}%",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        if (isCharging) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Charging ",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(id = R.drawable.battery),
                    contentDescription = "Charging",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        val hourStr = if (use24h) {
            String.format("%02d", time.hour)
        } else {
            val h = time.hour % 12
            String.format("%02d", if (h == 0) 12 else h)
        }
        val amPm = if (!use24h) {
            if (time.hour < 12) " AM" else " PM"
        } else ""

        Text(
            text = "$hourStr:${String.format("%02d", time.minute)}$amPm",
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable {
                    try {
                        context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
                    } catch (_: Exception) { }
                },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )

        val formattedDate = try {
            java.time.format.DateTimeFormatter.ofPattern(dateFormat).format(time)
        } catch (_: Exception) {
            "${time.month}, ${time.dayOfMonth} ${time.dayOfWeek.toString().substring(0, 3)}"
        }

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = formattedDate,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
    }
}

// ---------- Voice Command Help ----------

@Composable
private fun VoiceCommandHelpContent() {
    val categories = listOf(
        "Apps" to listOf(
            "\"open YouTube\"" to "Launch any app",
            "\"launch Spotify\"" to "Alias for open",
        ),
        "Communication" to listOf(
            "\"call Mom\"" to "Call a contact",
            "\"call 9876543210\"" to "Call a number",
            "\"text John\"" to "Open SMS to contact",
            "\"send email\"" to "Compose an email",
        ),
        "Search & Navigation" to listOf(
            "\"search weather today\"" to "Web search",
            "\"navigate to airport\"" to "Maps directions",
        ),
        "Alarms & Timers" to listOf(
            "\"set alarm 7 30 am\"" to "Create an alarm",
            "\"set timer 5 minutes\"" to "Countdown timer",
        ),
        "Music" to listOf(
            "\"play\" / \"pause\"" to "Play or pause music",
            "\"next\" / \"previous\"" to "Skip tracks",
        ),
        "Device Controls" to listOf(
            "\"volume up/down/mute/max\"" to "Adjust volume",
            "\"wifi on/off\"" to "Wi-Fi settings",
            "\"bluetooth on/off\"" to "Bluetooth settings",
            "\"brightness up/down\"" to "Display settings",
        ),
        "Info" to listOf(
            "\"battery\"" to "Battery level",
            "\"what time is it\"" to "Current time",
            "\"what date is it\"" to "Current date",
        ),
        "Utilities" to listOf(
            "\"camera\" / \"selfie\"" to "Open camera",
            "\"calculator\"" to "Open calculator",
            "\"settings\"" to "Device settings",
        ),
    )

    Text(
        text = "Voice Commands",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    categories.forEach { (category, commands) ->
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
        )
        commands.forEach { (command, description) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = command,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    textAlign = TextAlign.End
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Tap anywhere to dismiss",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}
