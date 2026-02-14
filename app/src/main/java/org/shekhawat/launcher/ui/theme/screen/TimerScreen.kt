package org.shekhawat.launcher.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun TimerScreen() {

    val configuration = LocalConfiguration.current
    val startTime by remember { mutableStateOf(LocalDateTime.now()) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            elapsedSeconds = withContext(Dispatchers.Default) {
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                    .minus(startTime.toEpochSecond(ZoneOffset.UTC))
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
                TimerWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 48.dp, bottom = 48.dp, end = 12.dp),
                    text = "${elapsedSeconds / (3600)}"
                )
                TimerWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, top = 48.dp, bottom = 48.dp, end = 12.dp),
                    text = "${(elapsedSeconds % 3600) / 60}"
                )
                TimerWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, top = 48.dp, bottom = 48.dp, end = 48.dp),
                    text = "${elapsedSeconds % 60}"
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
                TimerWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 48.dp, bottom = 12.dp, end = 48.dp),
                    text = "${elapsedSeconds / (3600)}"
                )
                TimerWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 12.dp, bottom = 12.dp, end = 48.dp),
                    text = "${(elapsedSeconds % 3600) / 60}"
                )
                TimerWidget(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, top = 12.dp, bottom = 48.dp, end = 48.dp),
                    text = "${elapsedSeconds % 60}"
                )
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
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
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
