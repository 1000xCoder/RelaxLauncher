package org.shekhawat.launcher.components

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.viewmodel.PomodoroViewModel
import org.shekhawat.launcher.viewmodel.RootViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSettings(onDismissRequest: () -> Unit) {
    val activity = LocalContext.current as Activity
    val sharedPrefManager = remember(activity) { SharedPrefManager(activity) }
    val viewModel = PomodoroViewModel(sharedPrefManager)
    val rootViewModel = RootViewModel(sharedPrefManager)
    val pomodoroTimeInSecondsFlow by viewModel.pomodoroTimeStateFlow.collectAsState(viewModel.getPomodoroTime())
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = sheetState
    ) {
        // pomodoro timer settings
        var sliderPosition by remember {
            mutableLongStateOf(
                pomodoroTimeInSecondsFlow / 60
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Swipe down to close the settings",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                modifier = Modifier.padding(
                    top = 16.dp,
                    bottom = 16.dp
                )
            )

            Text(
                text = "Pomodoro Timer: ${sliderPosition.toInt()} Min",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = sliderPosition.toFloat(),
                onValueChange = {
                    sliderPosition = it.toLong()
                    viewModel.setPomodoroTime((sliderPosition * 60))
                },
                valueRange = 1f..60f,
                steps = 59,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onPrimary,
                    activeTrackColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onPrimary.copy(
                        alpha = 0.3f
                    )
                ),
            )
        }
    }
}