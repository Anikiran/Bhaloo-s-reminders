package com.bhaloo.reminders.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bhaloo.reminders.ui.theme.GlassPane
import com.bhaloo.reminders.ui.theme.GlassShapeLarge
import com.bhaloo.reminders.ui.theme.glassInk
import com.bhaloo.reminders.ui.theme.glassInkSoft

/**
 * The floating glass header every screen wears, matching the title bar of the
 * reference: a pane that hovers over the backdrop rather than a bar welded to
 * the top of the window.
 */
@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigation: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
) {
    GlassPane(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = GlassShapeLarge,
        elevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigation != null) {
                navigation()
                Spacer(Modifier.width(12.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = glassInk(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = glassInkSoft(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (actions != null) {
                Spacer(Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) { actions() }
            }
        }
    }
}

/** Section heading inside a glass card. */
@Composable
fun GlassSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = glassInk(),
        modifier = modifier
    )
}

/** A titled pane, used to group controls on the editor and settings screens. */
@Composable
fun GlassSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassPane(modifier = modifier.fillMaxWidth(), elevation = 14.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            GlassSectionTitle(title)
            Box(modifier = Modifier.padding(top = 14.dp)) { content() }
        }
    }
}
