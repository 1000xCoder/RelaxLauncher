package org.shekhawat.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.shekhawat.launcher.ui.theme.RelaxLauncherTheme
import org.shekhawat.launcher.ui.theme.screen.SettingsScreen

class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefManager = SharedPrefManager(this)
        val dynamicColor = sharedPrefManager.getBoolean("dynamic_color", false)
        val theme = sharedPrefManager.getString("theme", "PURPLE")

        setContent {
            RelaxLauncherTheme(
                theme = ThemeType.valueOf(theme),
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = dynamicColor
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}
