package org.shekhawat.launcher.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import org.shekhawat.launcher.ThemeType

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightPrimary,
    surface = LightPrimary,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkPrimary,
    surface = DarkPrimary,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val BlueColorScheme = lightColorScheme(
    primary = LightBlueColor,
    secondary = Color.Cyan,
    tertiary = LightBlueColor,
    background = LightBlueColor,
    surface = LightBlueColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightBlueColor,
    onSurface = Color.White,
)

private val PurpleColorScheme = lightColorScheme(
    primary = PurpleColor,
    secondary = Color.Magenta,
    tertiary = DeepPurpleColor,
    background = PurpleColor,
    surface = PurpleColor,
    onPrimary = Color.White,
    onSecondary = PurpleColor,
    onTertiary = PurpleColor,
    onBackground = PurpleColor,
    onSurface = PurpleColor,
)

@Composable
fun RelaxLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    theme: ThemeType = if (isSystemInDarkTheme()) ThemeType.DARK else ThemeType.LIGHT,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        } else {
            when (theme) {
                ThemeType.DARK -> DarkColorScheme
                ThemeType.LIGHT -> LightColorScheme
                ThemeType.BLUE -> BlueColorScheme
                ThemeType.PURPLE -> PurpleColorScheme
            }
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                theme == ThemeType.LIGHT
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                theme == ThemeType.LIGHT
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}