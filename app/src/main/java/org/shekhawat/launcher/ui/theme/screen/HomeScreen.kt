package org.shekhawat.launcher.ui.theme.screen

import android.content.Intent
import android.os.BatteryManager
import android.provider.MediaStore
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.R
import org.shekhawat.launcher.data.fetchContacts
import org.shekhawat.launcher.permissions.RequestMicrophonePermission
import org.shekhawat.launcher.utils.SpeechRecognizerHelper
import java.time.LocalDateTime

@Composable
fun HomeScreen(appList: List<AppInfo>) {
    val context = LocalContext.current
    // get battery percentage
    val bm =
        LocalContext.current.getSystemService(android.content.Context.BATTERY_SERVICE) as BatteryManager
    var isCharging by remember {
        mutableStateOf(bm.isCharging)
    }
    var batteryPercentage by remember {
        mutableIntStateOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))
    }
    var time by remember {
        mutableStateOf(LocalDateTime.now())
    }
    // check microphone permission
    var hasPermission by remember { mutableStateOf(false) }

    RequestMicrophonePermission(
        context = context,
        onPermissionResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    LaunchedEffect(key1 = Unit) {
        while (true) {
            batteryPercentage = withContext(Dispatchers.Default) {
                // get the battery percentage
                bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            }
            time = withContext(Dispatchers.Default) {
                LocalDateTime.now()
            }
            isCharging = withContext(Dispatchers.Default) {
                bm.isCharging
            }
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { batteryPercentage / 100.0f },
                modifier = Modifier
                    .size(225.dp)
                    .align(Alignment.TopCenter)
                    .animateContentSize(),
                color = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            )
            TimeScreen(batteryPercentage, isCharging, time)
        }

        val isListening = remember { mutableStateOf(false) }
        var resultText by remember { mutableStateOf("") }
        val speechRecognizerHelper = remember {
            SpeechRecognizerHelper(context, isListening) { result ->
                resultText = result
                // take action
                if (result.contains("open")) {
                    val appName = result.split("open")[1].trim().replace(" ", "")
                    // ignore white space in both
                    val app = appList.map {
                        (
                                AppInfo(
                                    it.name.trim().replace(" ", ""),
                                    it.icon,
                                    it.intent,
                                    it.packageName.trim()
                                )
                                )
                    }.find { it.name.equals(appName, ignoreCase = true) }
                    // show error message when no action performed
                    if (app == null) {
                        resultText += "\nApp not found"
                    } else {
                        app.intent?.let {
                            context.startActivity(it)
                        }
                    }
                } else if (result.contains("call")) {
                    val contacts = fetchContacts(context)
                    // either search by name or number
                    val number = contacts.find {
                        it.name.replace(" ", "").contains(
                            result.split("call")[1].trim().replace(" ", ""),
                            ignoreCase = true
                        )
                    }?.phoneNumber
                        ?: result.split("call")[1].trim().replace(" ", "")

                    Log.d("HomeScreen", "Number: $number")
                    if (isInValidPhoneNumber(number)) {
                        resultText += "\nContact not found"
                    } else {
                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = android.net.Uri.parse("tel:$number")
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(bottom = 120.dp, start = 32.dp, end = 32.dp)
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        if (hasPermission) {
                            // listen to the mic
                            if (isListening.value) {
                                isListening.value = false
                                speechRecognizerHelper.stopListening()
                            } else {
                                isListening.value = true
                                resultText = ""
                                speechRecognizerHelper.startListening()
                            }
                        }
                    },
                painter = painterResource(id = R.drawable.microphone),
                contentDescription = "Mic"
            )

            Text(
                text = if (isListening.value) "Listening..." else "Press the button and speak",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (resultText.isNotEmpty()) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

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
                        // open the dialer
                        context.startActivity(Intent(Intent.ACTION_DIAL))
                    },
                painter = painterResource(id = R.drawable.telephone),
                contentDescription = "Phone"
            )
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        // open the camera
                        context.startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                    },
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "Camera"
            )
        }
    }
}

fun isInValidPhoneNumber(number: String): Boolean {
    return number.length < 10 || number.contains("[a-zA-Z]".toRegex())
}

@Composable
fun TimeScreen(batteryPercentage: Int, isCharging: Boolean, time: LocalDateTime) {
    val context = LocalContext.current
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .padding(end = 8.dp)
                .clickable {
                    // open the battery settings
                    context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY))
                },
            text = "${batteryPercentage}%"
        )
        if (isCharging) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Charging ")
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(id = R.drawable.battery),
                    contentDescription = "Charging"
                )
            }
        }
        Text(
            // show in two digit format
            text = String.format("%02d:%02d", time.hour, time.minute),
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable {
                    // open the clock
                    context.startActivity(Intent(Intent.ACTION_QUICK_CLOCK))
                },
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "${time.month}, ${time.dayOfMonth} ${time.dayOfWeek.toString().substring(0, 3)}"
        )
    }
}