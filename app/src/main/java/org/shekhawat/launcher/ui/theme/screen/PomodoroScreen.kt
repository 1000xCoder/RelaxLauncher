package org.shekhawat.launcher.ui.theme.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun PomodoroScreen() {

    val activity = LocalContext.current as Activity
    val sharedPrefManager = remember(activity) { SharedPrefManager(activity) }
    val viewModel = PomodoroViewModel(sharedPrefManager)
    val pomodoroTimeInSecondsFlow by viewModel.pomodoroTimeStateFlow.collectAsState(viewModel.getPomodoroTime())

    val configuration = LocalConfiguration.current
    var startTime by rememberSaveable { mutableStateOf(LocalDateTime.now()) }
    var remainingSeconds by rememberSaveable(pomodoroTimeInSecondsFlow) {
        mutableLongStateOf(
            pomodoroTimeInSecondsFlow
        )
    }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var hideActionItems by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            if (isRunning) {
                remainingSeconds = withContext(Dispatchers.Default) {
                    startTime.plusSeconds(pomodoroTimeInSecondsFlow).toEpochSecond(ZoneOffset.UTC)
                        .minus(
                            LocalDateTime.now().toEpochSecond(
                                ZoneOffset.UTC
                            )
                        )
                }

                if (remainingSeconds == 0L) {
                    isRunning = false
                    // play some sound
                    val intent = Intent(activity, RingtonePlayer::class.java)
                    activity.startService(intent)
                    delay(2000)
                    remainingSeconds = pomodoroTimeInSecondsFlow
                }
            }
            delay(1000)
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    if (showBottomSheet) {
        PomodoroSettings() {
            showBottomSheet = false
        }
    }

    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        hideActionItems = !hideActionItems
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PomodoroWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 48.dp, bottom = 48.dp, end = 12.dp),
                    text = "${remainingSeconds / 60}"
                )
                PomodoroWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, top = 48.dp, bottom = 48.dp, end = 48.dp),
                    text = "${remainingSeconds % 60}"
                )
                if (!hideActionItems) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PomodoroActionItems(isRunning) { eventType ->
                            run {
                                Log.d("TESTING", eventType);
                                when (eventType) {
                                    "play" -> {
                                        isRunning = !isRunning
                                        startTime = LocalDateTime.now()
                                    }

                                    "stop" -> {
                                        isRunning = !isRunning
                                        remainingSeconds = pomodoroTimeInSecondsFlow
                                    }

                                    "restart" -> {
                                        startTime = LocalDateTime.now()
                                        remainingSeconds = pomodoroTimeInSecondsFlow
                                    }

                                    "setting" -> {
                                        showBottomSheet = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        hideActionItems = !hideActionItems
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PomodoroWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 48.dp, bottom = 12.dp, end = 48.dp),
                    text = "${remainingSeconds / 60}"
                )
                PomodoroWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 12.dp, bottom = 48.dp, end = 48.dp),
                    text = "${remainingSeconds % 60}"
                )
                if (!hideActionItems) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PomodoroActionItems(isRunning) { eventType ->
                            run {
                                when (eventType) {
                                    "play" -> {
                                        isRunning = !isRunning
                                        startTime = LocalDateTime.now()
                                    }

                                    "stop" -> {
                                        isRunning = !isRunning
                                        remainingSeconds = pomodoroTimeInSecondsFlow
                                    }

                                    "restart" -> {
                                        startTime = LocalDateTime.now()
                                        remainingSeconds = pomodoroTimeInSecondsFlow
                                    }

                                    "setting" -> {
                                        showBottomSheet = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PomodoroActionItems(isRunning: Boolean = false, onClick: (String) -> Unit) {
    val audioManager = LocalContext.current.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    if (!isRunning) {
        Icon(
            modifier = Modifier
                .padding(12.dp)
                .size(36.dp)
                .clickable {
                    // start the timer
                    onClick("play")
                    audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK,1.0f)
                },
            painter = painterResource(id = R.drawable.play), contentDescription = "Play"
        )
        Icon(
            modifier = Modifier
                .padding(12.dp)
                .size(28.dp)
                .clickable {
                    // start the timer
                    onClick("setting")
                },
            painter = painterResource(id = R.drawable.setting_new), contentDescription = "Settings"
        )
    } else {
        Icon(
            modifier = Modifier
                .padding(12.dp)
                .size(36.dp)
                .clickable {
                    // stop the timer
                    onClick("stop")
                },
            painter = painterResource(id = R.drawable.stop), contentDescription = "Stop"
        )

        Icon(
            modifier = Modifier
                .padding(12.dp)
                .size(36.dp)
                .clickable {
                    // restart the timer
                    onClick("restart")
                },
            painter = painterResource(id = R.drawable.restart), contentDescription = "Restart"
        )
    }
}

@Composable
fun PomodoroWidget(modifier: Modifier, text: String) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = String.format("%02d", text.toInt()),
            fontSize = 200.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            thickness = 10.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
