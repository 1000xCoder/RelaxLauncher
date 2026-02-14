package org.shekhawat.launcher.ui.theme.screen

import android.content.res.Configuration
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import java.time.LocalDateTime

fun getHour(currentTime: LocalDateTime, hourFormat24: Boolean = true): String {
    return String.format("%02d", if (hourFormat24) currentTime.hour else currentTime.hour % 12)
}

fun getMinute(currentTime: LocalDateTime): String {
    return String.format("%02d", currentTime.minute)
}

fun getSecond(currentTime: LocalDateTime): String {
    return String.format("%02d", currentTime.second)
}

@Composable
fun TimeScreen() {

    val configuration = LocalConfiguration.current

    val bm =
        LocalContext.current.getSystemService(android.content.Context.BATTERY_SERVICE) as BatteryManager
    var batteryPercentage by remember {
        mutableIntStateOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))
    }
    var hourFormat24 by remember {
        mutableStateOf(true)
    }

    // method to change the time format
    fun toggleTimeFormat() {
        hourFormat24 = !hourFormat24
    }

    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(key1 = Unit) {
        while (true) {
            batteryPercentage = withContext(Dispatchers.Default) {
                // get the battery percentage
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
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HourTimeWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 48.dp, bottom = 48.dp, end = 12.dp),
                    time = currentTime,
                    hourFormat24
                ) {
                    toggleTimeFormat()
                }
                MinuteTimeWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, top = 48.dp, bottom = 48.dp, end = 48.dp),
                    time = currentTime
                )
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HourTimeWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 48.dp, bottom = 12.dp, end = 48.dp),
                    time = currentTime,
                    hourFormat24 = hourFormat24
                ) {
                    toggleTimeFormat()
                }
                MinuteTimeWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 12.dp, bottom = 48.dp, end = 48.dp),
                    time = currentTime
                )
            }
        }
    }
}

@Composable
fun HourTimeWidget(
    modifier: Modifier,
    time: LocalDateTime,
    hourFormat24: Boolean,
    toggleTimeFormat: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .clickable {
                    toggleTimeFormat()
                },
            text = getHour(time, hourFormat24),
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
        if (!hourFormat24) {
            AM_PMWidget(modifier = Modifier.align(Alignment.TopStart), time = time)
        }
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            text = time.dayOfWeek.name.substring(0, 3),
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AM_PMWidget(modifier: Modifier, time: LocalDateTime) {
    Text(
        modifier = modifier.padding(12.dp),
        text = if (time.hour < 12) "AM" else "PM",
        fontSize = 32.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun MinuteTimeWidget(modifier: Modifier, time: LocalDateTime) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = getMinute(time),
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
        SecondTimeWidget(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(IntrinsicSize.Min)
                .padding(12.dp),
            text = getSecond(time)
        )
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
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    .padding(start = 4.dp, end = 4.dp),
                text = text.substring(0, 1),
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    .padding(start = 4.dp, end = 4.dp),
                text = text.substring(1, 2),
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
