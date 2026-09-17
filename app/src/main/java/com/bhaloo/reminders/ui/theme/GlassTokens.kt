package com.bhaloo.reminders.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The palette, read straight off the liquid-glass reference.
 *
 * Three families do all the work:
 *  - a warm neutral backdrop (beige → taupe) that the glass sits on and tints,
 *  - two saturated accents, violet and mint, for anything interactive,
 *  - a pastel iridescence (pink → lavender → mint → peach) for the hero card
 *    and for the faint rainbow rim light along glass edges.
 */
object Glass {

    // ---- Backdrop -----------------------------------------------------------
    val Sand = Color(0xFFEFE7DC)
    val SandDeep = Color(0xFFDCD1C4)
    val Taupe = Color(0xFFBFB3A8)
    val TaupeDeep = Color(0xFF8E8379)

    val NightBase = Color(0xFF221E29)
    val NightDeep = Color(0xFF14121A)

    // ---- Accents ------------------------------------------------------------
    val Violet = Color(0xFF8B5CF6)
    val VioletBright = Color(0xFFA78BFA)
    val VioletDeep = Color(0xFF6D3BEF)

    val Mint = Color(0xFF3ED6C4)
    val MintBright = Color(0xFF7BEDDD)
    val MintDeep = Color(0xFF16B8A6)

    val Pink = Color(0xFFF9A8D4)
    val Lavender = Color(0xFFC4B5FD)
    val Peach = Color(0xFFFCD5A8)
    val Aqua = Color(0xFF9EE7DE)

    val Ink = Color(0xFF1B1720)
    val InkSoft = Color(0xFF5A5364)
    val InkOnDark = Color(0xFFF2EDF7)
    val InkSoftOnDark = Color(0xFFBFB6CC)

    val Success = Color(0xFF22C55E)
    val Danger = Color(0xFFE5484D)

    // ---- Glass surfaces -----------------------------------------------------
    /** Fill of a pane: brighter at the top, as if lit from above. */
    fun paneFill(dark: Boolean): Brush = if (dark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.07f),
                Color.White.copy(alpha = 0.10f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.62f),
                Color.White.copy(alpha = 0.32f),
                Color.White.copy(alpha = 0.44f)
            )
        )
    }

    /** A lighter pane for cards resting on top of another pane. */
    fun innerPaneFill(dark: Boolean): Brush = if (dark) {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.05f))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.72f), Color.White.copy(alpha = 0.38f))
        )
    }

    /**
     * The edge of a pane: bright where the light hits it, fading round the
     * bottom. This single stroke is what sells the material as glass.
     */
    fun rim(dark: Boolean): Brush = Brush.linearGradient(
        colors = if (dark) {
            listOf(
                Color.White.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.04f),
                Color.White.copy(alpha = 0.22f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.65f)
            )
        }
    )

    /** Faint prism along an edge — the giveaway detail of real glass. */
    val iridescentRim: Brush = Brush.sweepGradient(
        listOf(
            Pink.copy(alpha = 0.85f),
            Lavender.copy(alpha = 0.75f),
            Aqua.copy(alpha = 0.80f),
            Peach.copy(alpha = 0.70f),
            Pink.copy(alpha = 0.85f)
        )
    )

    /** The specular sheen laid over the top third of a control. */
    val gloss: Brush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.58f),
            Color.White.copy(alpha = 0.14f),
            Color.Transparent
        )
    )

    // ---- Interactive fills --------------------------------------------------
    val primaryFill: Brush = Brush.linearGradient(
        listOf(VioletBright, Violet, VioletDeep)
    )

    val secondaryFill: Brush = Brush.linearGradient(
        listOf(MintBright, Mint, MintDeep)
    )

    /** The pastel wash on the hero card. */
    val heroFill: Brush = Brush.linearGradient(
        listOf(
            Pink.copy(alpha = 0.92f),
            Lavender.copy(alpha = 0.88f),
            Aqua.copy(alpha = 0.85f),
            Peach.copy(alpha = 0.88f)
        )
    )

    // ---- Shadows ------------------------------------------------------------
    /** Glass casts a soft, tinted shadow, never a hard grey one. */
    val shadowSpot = Color(0xFF4A3F5C)
    val shadowAmbient = Color(0xFF6B5E7A)
    val shadowViolet = Color(0xFF6D3BEF)
    val shadowMint = Color(0xFF16B8A6)
}

/** Text colours that follow the backdrop, so nothing goes invisible at night. */
@androidx.compose.runtime.Composable
fun glassInk(): Color =
    if (androidx.compose.foundation.isSystemInDarkTheme()) Glass.InkOnDark else Glass.Ink

@androidx.compose.runtime.Composable
fun glassInkSoft(): Color =
    if (androidx.compose.foundation.isSystemInDarkTheme()) Glass.InkSoftOnDark else Glass.InkSoft
