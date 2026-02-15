package org.shekhawat.launcher.ui.theme.screen

import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.shekhawat.launcher.R

/**
 * Fullscreen Timer (stopwatch) display.
 * Reads all state from [ToolsState] so it stays in sync with the sheet and home screen.
 * No local timer state — single source of truth.
 */
@Composable
fun TimerScreen() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Read from ToolsState — same source as sheet & home screen
    val isRunning by ToolsState.timerRunning.collectAsState()
    val elapsedSeconds by ToolsState.timerElapsed.collectAsState()

    var hideActions by rememberSaveable { mutableStateOf(false) }

    val hours = elapsedSeconds / 3600
    val mins = (elapsedSeconds % 3600) / 60
    val secs = elapsedSeconds % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { hideActions = !hideActions }
    ) {
        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimerWidget(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 48.dp, top = 48.dp, bottom = 48.dp, end = 12.dp),
                        text = "$hours"
                    )
                    TimerWidget(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp, top = 48.dp, bottom = 48.dp, end = 12.dp),
                        text = "$mins"
                    )
                    TimerWidget(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp, top = 48.dp, bottom = 48.dp, end = 48.dp),
                        text = "$secs"
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimerWidget(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 48.dp, top = 48.dp, bottom = 12.dp, end = 48.dp),
                        text = "$hours"
                    )
                    TimerWidget(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 48.dp, top = 12.dp, bottom = 12.dp, end = 48.dp),
                        text = "$mins"
                    )
                    TimerWidget(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 48.dp, top = 12.dp, bottom = 48.dp, end = 48.dp),
                        text = "$secs"
                    )
                }
            }
        }

        // Status label at top
        AnimatedVisibility(
            visible = !hideActions,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -it },
            exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it }
        ) {
            Text(
                text = if (isRunning) "Running" else "Timer",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 80.dp)
            )
        }

        // Action buttons overlaid at bottom center
        AnimatedVisibility(
            visible = !hideActions,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
            exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 }
        ) {
            Row(
                modifier = Modifier.padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                if (!isRunning) {
                    FullscreenActionButton(iconRes = R.drawable.play, label = "Start") {
                        ToolsState.startTimer()
                        audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
                    }
                } else {
                    FullscreenActionButton(iconRes = R.drawable.pause, label = "Pause") {
                        ToolsState.pauseTimer()
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                FullscreenActionButton(iconRes = R.drawable.restart, label = "Reset") {
                    ToolsState.resetTimer()
                }
            }
        }
    }
}

@Composable
fun TimerWidget(modifier: Modifier, text: String) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
    ) {
        AnimatedContent(
            targetState = String.format("%02d", text.toIntOrNull() ?: 0),
            modifier = Modifier.align(Alignment.Center),
            transitionSpec = {
                (fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.92f))
                    .togetherWith(fadeOut(tween(200)))
            },
            label = "timerWidget"
        ) { displayText ->
            Text(
                text = displayText,
                fontSize = 180.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            thickness = 8.dp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)
        )
    }
}
