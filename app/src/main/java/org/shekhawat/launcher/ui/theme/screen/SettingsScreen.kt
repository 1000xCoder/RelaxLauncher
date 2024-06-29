package org.shekhawat.launcher.ui.theme.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen() {
    // show view model here
    val activity = LocalContext.current as Activity
    val sharedPrefManager = remember(activity) { SharedPrefManager(activity) }

    val viewModel = SettingsViewModel(sharedPrefManager)
    var dynamicColorChecked by remember {
        mutableStateOf(viewModel.getBoolean("dynamic_color", false))
    }

    // select from dropdown
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("LIGHT") }
    val options = listOf("LIGHT", "DARK", "PURPLE", "BLUE")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = SpaceBetween,
            verticalAlignment = CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Theme", style = MaterialTheme.typography.titleMedium
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(onClick = { expanded = true })
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,

            ) {
                BasicTextField(
                    value = selectedOption,
                    onValueChange = { selectedOption = it },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onPrimary),
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            onClick = {
                                selectedOption = option
                                expanded = false
                                viewModel.saveString("theme", option)
                            },
                            text = {
                                Text(
                                    text = option,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = SpaceBetween,
            verticalAlignment = CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Dynamic Color", style = MaterialTheme.typography.titleMedium
            )
            // select from dropdown
            Switch(
                checked = dynamicColorChecked, onCheckedChange = {
                    dynamicColorChecked = it
                    viewModel.saveBoolean("dynamic_color", it)
                },
                modifier = Modifier.align(CenterVertically),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedBorderColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        }
    }
}


@Composable
fun ThemeDropdownMenu() {

}