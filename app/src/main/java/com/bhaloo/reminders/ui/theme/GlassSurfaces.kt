package com.bhaloo.reminders.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Every pane of glass in the app is built here, out of the same four layers:
 *
 *   1. a soft tinted drop shadow,
 *   2. a translucent fill that is brighter at the top,
 *   3. a hairline rim that catches the light,
 *   4. a specular sheen across the upper third.
 *
 * The backdrop behind them ([MeshBackground]) is deliberately low-frequency —
 * broad, soft colour fields — so a translucent fill reads as frosted glass
 * without a real backdrop blur, which Android only offers from API 31.
 *
 * Presses animate scale rather than showing a ripple: a ripple punches a hard
 * circle through the sheen and breaks the illusion.
 */

val GlassShapeLarge = RoundedCornerShape(30.dp)
val GlassShapeMedium = RoundedCornerShape(22.dp)
val GlassShapeSmall = RoundedCornerShape(16.dp)
val GlassPill = RoundedCornerShape(percent = 50)

/** A pane of frosted glass. The workhorse: cards, bars, sheets. */
@Composable
fun GlassPane(
    modifier: Modifier = Modifier,
    shape: Shape = GlassShapeLarge,
    elevation: Dp = 18.dp,
    inner: Boolean = false,
    tint: Brush? = null,
    iridescent: Boolean = false,
    cornerRadius: Dp = 30.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = Glass.shadowAmbient.copy(alpha = 0.5f),
                spotColor = Glass.shadowSpot.copy(alpha = 0.6f)
            )
            .clip(shape)
            .background(if (inner) Glass.innerPaneFill(dark) else Glass.paneFill(dark))
            .then(if (tint != null) Modifier.background(tint) else Modifier)
            .border(1.dp, Glass.rim(dark), shape)
            .then(if (iridescent) Modifier.iridescentEdge(cornerRadius) else Modifier)
    ) {
        GlossOverlay(shape)
        content()
    }
}

/**
 * The sheen. Kept to the top half and clipped to the shape, so a control looks
 * lit from above rather than merely tinted white.
 */
@Composable
fun BoxScope.GlossOverlay(shape: Shape, strength: Float = 1f) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(shape)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Glass.gloss,
                    size = Size(size.width, size.height * 0.52f),
                    alpha = strength
                )
            }
    )
}

/** A whisper of prism along the edge, drawn just inside the rim. */
fun Modifier.iridescentEdge(cornerRadius: Dp = 26.dp, alpha: Float = 0.5f): Modifier =
    this.drawWithContent {
        drawContent()
        val stroke = 1.5.dp.toPx()
        val radius = cornerRadius.toPx()
        drawRoundRect(
            brush = Glass.iridescentRim,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(width = stroke),
            alpha = alpha
        )
    }

/**
 * Filled pill button — the violet "primary" and mint "secondary" of the
 * reference. Presses shrink it slightly, the way a soft physical key would.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Brush = Glass.primaryFill,
    glowColor: Color = Glass.shadowViolet,
    shape: Shape = GlassPill,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "press")

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 16.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = glowColor.copy(alpha = 0.45f),
                spotColor = glowColor.copy(alpha = 0.55f)
            )
            .clip(shape)
            .background(fill)
            .border(1.dp, Glass.rim(false), shape)
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        GlossOverlay(shape)
        content()
    }
}

/** Unfilled pill: clear glass with a rim, for secondary actions. */
@Composable
fun GlassOutlineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = GlassPill,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "press")

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(
                elevation = 10.dp,
                shape = shape,
                clip = false,
                ambientColor = Glass.shadowAmbient.copy(alpha = 0.35f),
                spotColor = Glass.shadowSpot.copy(alpha = 0.4f)
            )
            .clip(shape)
            .background(Glass.innerPaneFill(dark))
            .border(1.dp, Glass.rim(dark), shape)
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        GlossOverlay(shape, strength = 0.8f)
        content()
    }
}

/** The round glass buttons dotted around the reference. */
@Composable
fun GlassCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 48.dp,
    fill: Brush? = null,
    glowColor: Color = Glass.shadowSpot,
    iridescent: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, label = "press")

    Box(
        modifier = modifier
            .size(diameter)
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = glowColor.copy(alpha = 0.4f),
                spotColor = glowColor.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(fill ?: Glass.innerPaneFill(dark))
            .border(
                1.dp,
                if (iridescent) Glass.iridescentRim else Glass.rim(dark),
                CircleShape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        GlossOverlay(CircleShape)
        content()
    }
}
