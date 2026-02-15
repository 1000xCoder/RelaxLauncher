package org.shekhawat.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.shekhawat.launcher.ui.theme.RelaxLauncherTheme
import org.shekhawat.launcher.ui.theme.fontFamilyFromName
import org.shekhawat.launcher.ui.theme.screen.SettingsScreen

class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefManager = SharedPrefManager(this)

        setContent {
            // Observe theme reactively so changes apply instantly
            val theme by sharedPrefManager.observeString("theme", ThemeType.LIGHT.name)
                .collectAsState(initial = sharedPrefManager.getString("theme", ThemeType.LIGHT.name))
            val themeType = ThemeType.entries.find { it.name == theme } ?: ThemeType.LIGHT

            val dynamicColor by sharedPrefManager.observeBoolean("dynamic_color", false)
                .collectAsState(initial = sharedPrefManager.getBoolean("dynamic_color", false))

            val fontPref by sharedPrefManager.observeString("font_family", "Default")
                .collectAsState(initial = sharedPrefManager.getString("font_family", "Default"))
            val selectedFontFamily = remember(fontPref) { fontFamilyFromName(fontPref) }

            RelaxLauncherTheme(
                theme = themeType,
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = dynamicColor,
                fontFamily = selectedFontFamily
            ) {
                // Animate the surface color to avoid a blank flash on theme switch
                val surfaceColor by animateColorAsState(
                    targetValue = MaterialTheme.colorScheme.primary,
                    animationSpec = tween(durationMillis = 300),
                    label = "surfaceBg"
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = surfaceColor
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}
