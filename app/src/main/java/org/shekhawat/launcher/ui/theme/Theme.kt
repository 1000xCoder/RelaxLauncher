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
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import org.shekhawat.launcher.ThemeType

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightPrimary,
    surface = LightPrimary,
    surfaceVariant = LightSecondary,
    onPrimary = Color(0xFF1C1C1C),
    onSecondary = Color(0xFF1C1C1C),
    onTertiary = Color(0xFF1C1C1C),
    onBackground = Color(0xFF1C1C1C),
    onSurface = Color(0xFF1C1C1C),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0),
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkPrimary,
    surface = DarkSecondary,
    surfaceVariant = DarkTertiary,
    onPrimary = Color(0xFFE0E0E0),
    onSecondary = Color(0xFFE0E0E0),
    onTertiary = Color(0xFFE0E0E0),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF2C2C2C),
)

private val BlueColorScheme = lightColorScheme(
    primary = LightBlueColor,
    secondary = DarkBlueAccent,
    tertiary = LightBlueAccent,
    background = LightBlueColor,
    surface = DarkBlueAccent,
    surfaceVariant = Color(0xFF0277BD),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF01579B),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFE1F5FE),
    outline = Color(0xFF4FC3F7),
    outlineVariant = Color(0xFF0288D1),
)

private val PurpleColorScheme = lightColorScheme(
    primary = PurpleColor,
    secondary = DeepPurpleColor,
    tertiary = LightPurpleColor,
    background = PurpleColor,
    surface = DeepPurpleColor,
    surfaceVariant = Color(0xFF7B1FA2),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF4A148C),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFF3E5F5),
    outline = Color(0xFFBA68C8),
    outlineVariant = Color(0xFF7B1FA2),
)

@Composable
fun RelaxLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    theme: ThemeType = if (isSystemInDarkTheme()) ThemeType.DARK else ThemeType.LIGHT,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    fontFamily: FontFamily = FontFamily.Default,
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
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                theme == ThemeType.LIGHT
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                theme == ThemeType.LIGHT
        }
    }

    val typography = if (fontFamily == FontFamily.Default) Typography else typographyWithFont(fontFamily)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
