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

/** Warm honey-and-cocoa palette — a bear's colours. */
private val Honey = Color(0xFFF2A413)
private val HoneyDark = Color(0xFFC77F05)
private val Cocoa = Color(0xFF5A3921)
private val CocoaLight = Color(0xFF8B5E3C)
private val Cream = Color(0xFFFFF8EE)
private val Berry = Color(0xFFB3261E)

private val LightColors = lightColorScheme(
    primary = HoneyDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDA6),
    onPrimaryContainer = Color(0xFF2B1700),
    secondary = Cocoa,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3DFCC),
    onSecondaryContainer = Color(0xFF2B1700),
    tertiary = Color(0xFF4A6543),
    background = Cream,
    onBackground = Color(0xFF211A13),
    surface = Cream,
    onSurface = Color(0xFF211A13),
    surfaceVariant = Color(0xFFF1E0CE),
    onSurfaceVariant = Color(0xFF51443A),
    error = Berry
)

private val DarkColors = darkColorScheme(
    primary = Honey,
    onPrimary = Color(0xFF412D00),
    primaryContainer = Color(0xFF5E4200),
    onPrimaryContainer = Color(0xFFFFDDA6),
    secondary = CocoaLight,
    onSecondary = Color(0xFF2B1700),
    secondaryContainer = Color(0xFF4A3524),
    onSecondaryContainer = Color(0xFFF3DFCC),
    tertiary = Color(0xFFB0CCA6),
    background = Color(0xFF191210),
    onBackground = Color(0xFFEDE0D4),
    surface = Color(0xFF191210),
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF51443A),
    onSurfaceVariant = Color(0xFFD5C3B5),
    error = Color(0xFFFFB4AB)
)

private val BhalooTypography = Typography(
    displaySmall = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = BhalooTypography,
        content = content
    )
}
