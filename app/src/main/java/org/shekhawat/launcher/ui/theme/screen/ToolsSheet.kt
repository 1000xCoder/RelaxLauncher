package org.shekhawat.launcher.ui.theme.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.shekhawat.launcher.R
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.components.PomodoroSettings
import org.shekhawat.launcher.service.RingtonePlayer
import org.shekhawat.launcher.viewmodel.PomodoroViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset

private val TAB_TITLES = listOf("Pomodoro", "Timer", "Clock")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // ── Tab Row ──
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                divider = {}
            ) {
                TAB_TITLES.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Animated Tab Content ──
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 10 })
                        .togetherWith(fadeOut(tween(150)))
                },
                label = "tabContent"
            ) { tab ->
                when (tab) {
                    0 -> PomodoroTabContent()
                    1 -> TimerTabContent()
                    2 -> ClockTabContent()
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// Pomodoro Tab
// ──────────────────────────────────────────────

@Composable
private fun PomodoroTabContent() {
    val context = LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }
    val viewModel = remember(sharedPrefManager) { PomodoroViewModel(sharedPrefManager) }
    val pomodoroTimeInSeconds by viewModel.pomodoroTimeStateFlow.collectAsState(viewModel.getPomodoroTime())

    val isRunning by ToolsState.pomodoroRunning.collectAsState()
    val remaining by ToolsState.pomodoroRemaining.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var showFullscreen by remember { mutableStateOf(false) }

    val playSound = sharedPrefManager.getBoolean("pomodoro_sound", true)

    // Sync total seconds from settings when not running
    LaunchedEffect(pomodoroTimeInSeconds, isRunning) {
        if (!isRunning) {
            ToolsState.updatePomodoroRemaining(pomodoroTimeInSeconds)
            ToolsState.restartPomodoro(pomodoroTimeInSeconds)
            ToolsState.stopPomodoro(pomodoroTimeInSeconds)
        }
    }

    // Timer tick
    LaunchedEffect(isRunning) {
        while (isRunning) {
            val totalSec = ToolsState.pomodoroTotalSeconds.value
            val st = ToolsState.pomodoroStartTime.value
            val nowSec = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            val startSec = st.toEpochSecond(ZoneOffset.UTC)
            val rem = (totalSec - (nowSec - startSec)).coerceAtLeast(0L)
            ToolsState.updatePomodoroRemaining(rem)
            if (rem <= 0L) {
                if (playSound) {
                    try {
                        context.startService(Intent(context, RingtonePlayer::class.java))
                    } catch (_: Exception) { }
                }
                delay(2000)
                ToolsState.pomodoroCompleted(pomodoroTimeInSeconds)
            }
            delay(1000)
        }
    }

    if (showSettings) {
        PomodoroSettings(viewModel = viewModel) { showSettings = false }
    }

    val totalSeconds = pomodoroTimeInSeconds.coerceAtLeast(1)
    val progress = remaining.toFloat() / totalSeconds
    val minutes = remaining / 60
    val seconds = remaining % 60

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "pomProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Circular progress ring ──
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            // Track ring
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(200.dp),
                strokeWidth = 10.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                trackColor = Color.Transparent
            )
            // Progress ring
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(200.dp),
                strokeWidth = 10.dp,
                color = if (isRunning) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                trackColor = Color.Transparent
            )
            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = String.format("%02d:%02d", minutes, seconds),
                    transitionSpec = {
                        (fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.96f))
                            .togetherWith(fadeOut(tween(120)))
                    },
                    label = "pomTime"
                ) { timeText ->
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 52.sp,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                AnimatedVisibility(visible = isRunning) {
                    Text(
                        text = "FOCUS",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Action buttons ──
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isRunning) {
                SheetActionButton(iconRes = R.drawable.play, label = "Start", isPrimary = true) {
                    ToolsState.startPomodoro(pomodoroTimeInSeconds)
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
                }
                Spacer(modifier = Modifier.width(20.dp))
                SheetActionButton(iconRes = R.drawable.setting_new, label = "Settings") {
                    showSettings = true
                }
            } else {
                SheetActionButton(iconRes = R.drawable.stop, label = "Stop", isPrimary = true) {
                    ToolsState.stopPomodoro(pomodoroTimeInSeconds)
                }
                Spacer(modifier = Modifier.width(20.dp))
                SheetActionButton(iconRes = R.drawable.restart, label = "Restart") {
                    ToolsState.restartPomodoro(pomodoroTimeInSeconds)
                    ToolsState.startPomodoro(pomodoroTimeInSeconds)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Fullscreen chip ──
        FullscreenChip { showFullscreen = true }
    }

    // ── Fullscreen overlay — primary bg so existing PomodoroScreen colors work ──
    if (showFullscreen) {
        FullscreenToolDialog(onDismiss = { showFullscreen = false }) {
            PomodoroScreen()
        }
    }
}

// ──────────────────────────────────────────────
// Timer Tab
// ──────────────────────────────────────────────

@Composable
private fun TimerTabContent() {
    val context = LocalContext.current

    val isRunning by ToolsState.timerRunning.collectAsState()
    val elapsedSeconds by ToolsState.timerElapsed.collectAsState()

    var showFullscreen by remember { mutableStateOf(false) }

    // Timer tick
    LaunchedEffect(isRunning) {
        while (isRunning) {
            val nowSec = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            val startSec = ToolsState.timerStartTime.value.toEpochSecond(ZoneOffset.UTC)
            val elapsed = ToolsState.timerPausedElapsed.value + (nowSec - startSec)
            ToolsState.updateTimerElapsed(elapsed)
            delay(1000)
        }
    }

    val hours = elapsedSeconds / 3600
    val mins = (elapsedSeconds % 3600) / 60
    val secs = elapsedSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Elapsed time card ──
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                .padding(vertical = 36.dp)
        ) {
            AnimatedContent(
                targetState = String.format("%02d:%02d:%02d", hours, mins, secs),
                transitionSpec = {
                    (fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.97f))
                        .togetherWith(fadeOut(tween(120)))
                },
                label = "timerTime"
            ) { timeText ->
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp,
                        letterSpacing = 4.sp
                    ),
                    color = if (isRunning) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // ── Action buttons ──
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isRunning) {
                SheetActionButton(iconRes = R.drawable.play, label = "Start", isPrimary = true) {
                    ToolsState.startTimer()
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
                }
            } else {
                SheetActionButton(iconRes = R.drawable.pause, label = "Pause", isPrimary = true) {
                    ToolsState.pauseTimer()
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            SheetActionButton(iconRes = R.drawable.restart, label = "Reset") {
                ToolsState.resetTimer()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FullscreenChip { showFullscreen = true }
    }

    // ── Fullscreen overlay ──
    if (showFullscreen) {
        FullscreenToolDialog(onDismiss = { showFullscreen = false }) {
            TimerScreen()
        }
    }
}

// ──────────────────────────────────────────────
// Clock Tab
// ──────────────────────────────────────────────

@Composable
private fun ClockTabContent() {
    val context = LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }

    val use24h by sharedPrefManager.observeBoolean("use_24h_clock", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("use_24h_clock", true))
    val showSeconds by sharedPrefManager.observeBoolean("show_seconds", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_seconds", true))

    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    var showFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = withContext(Dispatchers.Default) { LocalDateTime.now() }
            delay(1000)
        }
    }

    val hourStr = if (use24h) {
        String.format("%02d", currentTime.hour)
    } else {
        val h = currentTime.hour % 12
        String.format("%02d", if (h == 0) 12 else h)
    }
    val minuteStr = String.format("%02d", currentTime.minute)
    val secondStr = String.format("%02d", currentTime.second)
    val amPm = if (!use24h) {
        if (currentTime.hour < 12) "AM" else "PM"
    } else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Clock display card ──
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                .padding(vertical = 28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$hourStr:$minuteStr",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 56.sp,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (showSeconds) {
                        Text(
                            text = ":$secondStr",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    if (amPm != null) {
                        Text(
                            text = " $amPm",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentTime.dayOfWeek.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold
                )
                val formattedDate = try {
                    java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy").format(currentTime)
                } catch (_: Exception) {
                    "${currentTime.month} ${currentTime.dayOfMonth}, ${currentTime.year}"
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        FullscreenChip { showFullscreen = true }
    }

    // ── Fullscreen overlay ──
    if (showFullscreen) {
        FullscreenToolDialog(onDismiss = { showFullscreen = false }) {
            TimeScreen()
        }
    }
}

// ──────────────────────────────────────────────
// Shared Components
// ──────────────────────────────────────────────

/**
 * Polished action button with an icon inside a circle.
 * Primary variant uses the theme primary color.
 */
@Composable
private fun SheetActionButton(
    iconRes: Int,
    label: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = if (isPrimary) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPrimary) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * "Fullscreen" chip — consistent across all tabs.
 */
@Composable
private fun FullscreenChip(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Go Fullscreen",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Fullscreen dialog wrapper.
 * Uses `primary` as background so the existing fullscreen screen composables
 * (PomodoroScreen, TimerScreen, TimeScreen) which use `onPrimary` for text
 * render with correct contrast across all themes.
 *
 * Temporarily unlocks screen rotation so the fullscreen tools can be used
 * in landscape. Re-locks to portrait when dismissed.
 */
@Composable
private fun FullscreenToolDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Unlock rotation while fullscreen, re-lock to portrait on dismiss
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            content()

            // Close button at top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
