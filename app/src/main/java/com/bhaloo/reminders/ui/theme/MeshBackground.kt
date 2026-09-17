package com.bhaloo.reminders.ui.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * What the glass is looking at.
 *
 * A warm sand-to-taupe wash with a light source in the top-left, plus four
 * slow-drifting colour blobs — violet, mint, pink, peach. Keeping it soft and
 * low-frequency is the whole trick: a translucent white pane laid over broad
 * colour fields is visually indistinguishable from a real frosted blur, and it
 * works on every Android version instead of only API 31+.
 */
@Composable
fun MeshBackground(modifier: Modifier = Modifier, animated: Boolean = true) {
    val dark = isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "mesh")
    val wander by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(22_000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val drift = if (animated) wander else 0.5f

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base wash, lit from the top-left like the reference.
        drawRect(
            brush = Brush.linearGradient(
                colors = if (dark) {
                    listOf(Glass.NightBase, Glass.NightDeep, Color(0xFF241F2E))
                } else {
                    listOf(Glass.Sand, Glass.SandDeep, Glass.Taupe)
                },
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        val a = if (dark) 0.40f else 0.52f
        fun blob(cx: Float, cy: Float, radius: Float, color: Color, strength: Float = 1f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = a * strength), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )
        }

        val sway = (drift - 0.5f) * 2f
        blob(w * (0.16f + 0.05f * sway), h * (0.12f + 0.03f * sway), w * 0.75f, Glass.Lavender)
        blob(w * (0.92f - 0.06f * sway), h * (0.20f - 0.04f * sway), w * 0.70f, Glass.Aqua)
        blob(w * (0.85f + 0.05f * sway), h * (0.80f + 0.04f * sway), w * 0.80f, Glass.Pink, 0.9f)
        blob(w * (0.10f - 0.04f * sway), h * (0.88f - 0.03f * sway), w * 0.72f, Glass.Peach, 0.8f)

        // A broad highlight so the top-left of every pane has something to catch.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (dark) 0.10f else 0.42f),
                    Color.Transparent
                ),
                center = Offset(w * 0.22f, h * 0.05f),
                radius = w * 0.85f
            ),
            radius = w * 0.85f,
            center = Offset(w * 0.22f, h * 0.05f)
        )

        // …and a soft vignette so the glass edges read against the backdrop.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Glass.TaupeDeep.copy(alpha = if (dark) 0.5f else 0.30f)),
                center = Offset(w * 0.5f, h * 0.45f),
                radius = maxOf(w, h) * 0.78f
            )
        )
    }
}
