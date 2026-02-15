package org.shekhawat.launcher.ui.theme.screen

import android.content.res.Configuration
import android.os.BatteryManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.shekhawat.launcher.SharedPrefManager
import java.time.LocalDateTime

fun getHour(currentTime: LocalDateTime, hourFormat24: Boolean = true): String {
    val hour = if (hourFormat24) currentTime.hour else {
        val h = currentTime.hour % 12
        if (h == 0) 12 else h
    }
    return String.format("%02d", hour)
}

fun getMinute(currentTime: LocalDateTime): String {
    return String.format("%02d", currentTime.minute)
}

fun getSecond(currentTime: LocalDateTime): String {
    return String.format("%02d", currentTime.second)
}

@Composable
fun TimeScreen() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }

    val use24h by sharedPrefManager.observeBoolean("use_24h_clock", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("use_24h_clock", true))
    val showSeconds by sharedPrefManager.observeBoolean("show_seconds", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_seconds", true))

    val bm =
        context.getSystemService(android.content.Context.BATTERY_SERVICE) as BatteryManager
    var batteryPercentage by remember {
        mutableIntStateOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))
    }

    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(key1 = Unit) {
        while (true) {
            batteryPercentage = withContext(Dispatchers.Default) {
                bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            }
            currentTime = withContext(Dispatchers.Default) {
                LocalDateTime.now()
            }
            delay(1000)
        }
    }

    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HourTimeWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 48.dp, bottom = 48.dp, end = 12.dp),
                    time = currentTime,
                    hourFormat24 = use24h
                )
                MinuteTimeWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, top = 48.dp, bottom = 48.dp, end = 48.dp),
                    time = currentTime,
                    showSeconds = showSeconds
                )
            }
        }

        else -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HourTimeWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 48.dp, bottom = 12.dp, end = 48.dp),
                    time = currentTime,
                    hourFormat24 = use24h
                )
                MinuteTimeWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 12.dp, bottom = 48.dp, end = 48.dp),
                    time = currentTime,
                    showSeconds = showSeconds
                )
            }
        }
    }
}

@Composable
fun HourTimeWidget(
    modifier: Modifier,
    time: LocalDateTime,
    hourFormat24: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)),
    ) {
        // Animated hour text
        AnimatedContent(
            targetState = getHour(time, hourFormat24),
            modifier = Modifier.align(Alignment.Center),
            transitionSpec = {
                (fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.94f))
                    .togetherWith(fadeOut(tween(250)))
            },
            label = "hourWidget"
        ) { hourText ->
            Text(
                text = hourText,
                fontSize = 180.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            )
        }
        // Center divider
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            thickness = 8.dp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)
        )
        if (!hourFormat24) {
            AmPmWidget(modifier = Modifier.align(Alignment.TopStart), time = time)
        }
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            text = time.dayOfWeek.name.substring(0, 3),
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        )
    }
}

@Composable
fun AmPmWidget(modifier: Modifier, time: LocalDateTime) {
    Text(
        modifier = modifier.padding(12.dp),
        text = if (time.hour < 12) "AM" else "PM",
        fontSize = 32.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
    )
}

@Composable
fun MinuteTimeWidget(
    modifier: Modifier,
    time: LocalDateTime,
    showSeconds: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
    ) {
        // Animated minute text
        AnimatedContent(
            targetState = getMinute(time),
            modifier = Modifier.align(Alignment.Center),
            transitionSpec = {
                (fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.94f))
                    .togetherWith(fadeOut(tween(250)))
            },
            label = "minuteWidget"
        ) { minuteText ->
            Text(
                text = minuteText,
                fontSize = 180.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            )
        }
        // Center divider
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            thickness = 8.dp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)
        )
        if (showSeconds) {
            SecondTimeWidget(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(IntrinsicSize.Min)
                    .padding(12.dp),
                text = getSecond(time)
            )
        }
    }
}

@Composable
fun SecondTimeWidget(modifier: Modifier, text: String) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                text = text.substring(0, 1),
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )
            Text(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                text = text.substring(1, 2),
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )
        }
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)
        )
    }
}
