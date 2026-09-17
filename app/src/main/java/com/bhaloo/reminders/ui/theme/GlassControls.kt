package com.bhaloo.reminders.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The smaller controls — fields, switches, chips — all cut from the same glass
 * as [GlassPane] so nothing in the app looks like a stock Material widget.
 */

/**
 * A pill-shaped text field on clear glass.
 *
 * Built on BasicTextField rather than a Material text field: the Material ones
 * bring their own container, indicator line and label animation, all of which
 * fight the glass and would have to be neutralised one colour at a time.
 */
@Composable
fun GlassField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    singleLine: Boolean = true,
    minHeight: Dp = 56.dp,
    shape: Shape = GlassPill,
    trailing: (@Composable () -> Unit)? = null
) {
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Glass.InkOnDark else Glass.Ink
    val hintColor = if (dark) Glass.InkSoftOnDark else Glass.InkSoft

    Box(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                color = hintColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 20.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (label != null) 22.dp else 0.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = shape,
                    clip = false,
                    ambientColor = Glass.shadowAmbient.copy(alpha = 0.3f),
                    spotColor = Glass.shadowSpot.copy(alpha = 0.35f)
                )
                .clip(shape)
                .background(Glass.innerPaneFill(dark))
                .border(1.dp, Glass.rim(dark), shape)
        ) {
            GlossOverlay(shape, strength = 0.7f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = if (singleLine) 0.dp else 16.dp)
                    .then(if (singleLine) Modifier.height(minHeight) else Modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = hintColor,
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = singleLine,
                        textStyle = LocalTextStyle.current.copy(
                            color = textColor,
                            fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(Glass.Violet),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (trailing != null) {
                    Spacer(Modifier.width(10.dp))
                    trailing()
                }
            }
        }
    }
}

/** Glass toggle: the knob is a lit bead that slides on a tinted track. */
@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val trackWidth = 56.dp
    val knob = 26.dp
    val offset by animateDpAsState(
        targetValue = if (checked) trackWidth - knob - 4.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 520f),
        label = "knob"
    )
    val track by animateColorAsState(
        targetValue = if (checked) Glass.Violet.copy(alpha = 0.85f)
        else if (dark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.55f),
        label = "track"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = 34.dp)
            .shadow(
                elevation = 6.dp,
                shape = GlassPill,
                clip = false,
                ambientColor = if (checked) Glass.shadowViolet.copy(alpha = 0.5f)
                else Glass.shadowAmbient.copy(alpha = 0.3f),
                spotColor = if (checked) Glass.shadowViolet.copy(alpha = 0.55f)
                else Glass.shadowSpot.copy(alpha = 0.3f)
            )
            .clip(GlassPill)
            .background(track)
            .border(1.dp, Glass.rim(dark), GlassPill)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        GlossOverlay(GlassPill, strength = 0.85f)
        Box(
            modifier = Modifier
                .offset(x = offset)
                .size(knob)
                .shadow(
                    elevation = 5.dp,
                    shape = CircleShape,
                    clip = false,
                    spotColor = Glass.shadowSpot.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White, Color.White.copy(alpha = 0.86f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape)
        )
    }
}

/** Selectable glass chip. Selected chips fill with the accent gradient. */
@Composable
fun GlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    fill: Brush = Glass.primaryFill,
    glowColor: Color = Glass.shadowViolet
) {
    val dark = isSystemInDarkTheme()
    val scale by animateFloatAsState(if (selected) 1f else 0.985f, label = "chip")
    val labelColor = when {
        selected -> Color.White
        dark -> Glass.InkOnDark
        else -> Glass.Ink
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(40.dp)
            .shadow(
                elevation = if (selected) 12.dp else 6.dp,
                shape = GlassPill,
                clip = false,
                ambientColor = if (selected) glowColor.copy(alpha = 0.45f)
                else Glass.shadowAmbient.copy(alpha = 0.25f),
                spotColor = if (selected) glowColor.copy(alpha = 0.5f)
                else Glass.shadowSpot.copy(alpha = 0.3f)
            )
            .clip(GlassPill)
            .background(if (selected) fill else Glass.innerPaneFill(dark))
            .border(1.dp, Glass.rim(dark), GlassPill)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        GlossOverlay(GlassPill, strength = if (selected) 1f else 0.7f)
        Text(
            text = label,
            color = labelColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}

/** A row of label + control, used all over the editor and settings. */
@Composable
fun GlassToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null
) {
    val dark = isSystemInDarkTheme()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Column {
                Text(
                    text = label,
                    color = if (dark) Glass.InkOnDark else Glass.Ink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (supporting != null) {
                    Text(
                        text = supporting,
                        color = if (dark) Glass.InkSoftOnDark else Glass.InkSoft,
                        fontSize = 13.sp
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        GlassSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
