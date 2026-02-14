package org.shekhawat.launcher.ui.theme.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen() {
    // show view model here
    val context = LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }

    val viewModel = SettingsViewModel(sharedPrefManager)
    var dynamicColorChecked by remember {
        mutableStateOf(viewModel.getBoolean("dynamic_color", false))
    }

    // select from dropdown
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("LIGHT") }
    val options = listOf("LIGHT", "DARK", "PURPLE", "BLUE")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Theme Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Theme",
                style = MaterialTheme.typography.titleMedium
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = { expanded = !expanded })
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp),
                    text = selectedOption,
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
                                viewModel.setTheme(option)
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

        // Dynamic Color Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Dynamic Color",
                style = MaterialTheme.typography.titleMedium
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.CenterEnd
            ) {
                Switch(
                    checked = dynamicColorChecked,
                    onCheckedChange = {
                        dynamicColorChecked = it
                        viewModel.saveBoolean("dynamic_color", it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    )
                )
            }
        }

        // Exit Current Launcher Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = "Exit Launcher")
            }
        }
    }
}
