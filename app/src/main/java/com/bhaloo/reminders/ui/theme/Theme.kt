package com.bhaloo.reminders.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * Colours come from [Glass]; this scheme exists so the few Material widgets
 * left in the app (text selection handles, the date and time pickers) land in
 * the same palette as everything hand-built.
 */
private val LightColors = lightColorScheme(
    primary = Glass.Violet,
    onPrimary = Color.White,
    primaryContainer = Glass.Lavender,
    onPrimaryContainer = Glass.Ink,
    secondary = Glass.MintDeep,
    onSecondary = Color.White,
    secondaryContainer = Glass.Aqua,
    onSecondaryContainer = Glass.Ink,
    tertiary = Glass.Pink,
    onTertiary = Glass.Ink,
    background = Glass.Sand,
    onBackground = Glass.Ink,
    surface = Color.White.copy(alpha = 0.86f),
    onSurface = Glass.Ink,
    surfaceVariant = Color.White.copy(alpha = 0.64f),
    onSurfaceVariant = Glass.InkSoft,
    outline = Color.White.copy(alpha = 0.7f),
    error = Glass.Danger,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Glass.VioletBright,
    onPrimary = Color(0xFF1B1030),
    primaryContainer = Glass.VioletDeep,
    onPrimaryContainer = Glass.InkOnDark,
    secondary = Glass.MintBright,
    onSecondary = Color(0xFF04322D),
    secondaryContainer = Glass.MintDeep,
    onSecondaryContainer = Glass.InkOnDark,
    tertiary = Glass.Pink,
    onTertiary = Color(0xFF3A1128),
    background = Glass.NightBase,
    onBackground = Glass.InkOnDark,
    surface = Color(0xFF2B2635),
    onSurface = Glass.InkOnDark,
    surfaceVariant = Color(0xFF342E40),
    onSurfaceVariant = Glass.InkSoftOnDark,
    outline = Color.White.copy(alpha = 0.28f),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF3A0A0C)
)

private val GlassTypography = Typography(
    displaySmall = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun BhalooTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            // The backdrop is pale in light mode and deep in dark mode, so the
            // status-bar icons have to flip with it.
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = GlassTypography,
        content = content
    )
}
