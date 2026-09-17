package com.bhaloo.reminders.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhaloo.reminders.R
import com.bhaloo.reminders.data.Maker
import com.bhaloo.reminders.ui.ReminderViewModel
import com.bhaloo.reminders.ui.theme.Glass
import com.bhaloo.reminders.ui.theme.GlassCircleButton
import com.bhaloo.reminders.ui.theme.GlassPane
import com.bhaloo.reminders.ui.theme.glassInk
import com.bhaloo.reminders.ui.theme.glassInkSoft
import com.bhaloo.reminders.util.BhalooWords

@Composable
fun AboutScreen(viewModel: ReminderViewModel, onClose: () -> Unit) {
    val store = viewModel.store
    val reminders by viewModel.reminders.collectAsState()
    var taps by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            GlassTopBar(
                title = stringResource(R.string.about),
                navigation = {
                    GlassCircleButton(onClick = onClose, diameter = 42.dp) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back), tint = glassInk())
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassPane(
                    modifier = Modifier.fillMaxWidth(),
                    tint = Glass.heroFill,
                    elevation = 22.dp,
                    iridescent = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.bhaloo_portrait),
                            contentDescription = stringResource(R.string.bhaloo_photo_desc),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(126.dp)
                                .shadow(20.dp, CircleShape, clip = false, spotColor = Glass.shadowSpot)
                                .clip(CircleShape)
                                .border(3.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { taps++ }
                        )
                        Spacer(Modifier.height(18.dp))
                        Text(
                            stringResource(R.string.dedication_headline),
                            style = MaterialTheme.typography.displaySmall,
                            color = Glass.Ink,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.dedication_body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Glass.Ink.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.dedication_body_hi),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Glass.Ink.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                GlassPane(modifier = Modifier.fillMaxWidth(), elevation = 14.dp) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        GlassSectionTitle(stringResource(R.string.stats_title))
                        Spacer(Modifier.height(14.dp))
                        StatRow(stringResource(R.string.stats_times), store.timesReminded.toString())
                        StatRow(
                            stringResource(R.string.stats_active),
                            reminders.count { it.enabled }.toString()
                        )
                        StatRow(
                            stringResource(R.string.stats_completed),
                            reminders.sumOf { it.timesCompleted }.toString()
                        )
                        StatRow(
                            stringResource(R.string.stats_days),
                            BhalooWords.daysTogether(store.installedAt).toString()
                        )
                    }
                }

                // Baked in, not stored: see data/Maker.kt.
                Text(
                    text = stringResource(R.string.signature_named, Maker.NAME),
                    style = MaterialTheme.typography.bodyLarge,
                    color = glassInk(),
                    textAlign = TextAlign.Center
                )

                if (taps >= 5) {
                    GlassPane(
                        modifier = Modifier.fillMaxWidth(),
                        tint = Glass.heroFill,
                        elevation = 18.dp,
                        iridescent = true
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                stringResource(R.string.easter_egg_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = Glass.Ink
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.easter_egg_body),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Glass.Ink.copy(alpha = 0.82f)
                            )
                        }
                    }
                } else {
                    Text(
                        stringResource(R.string.easter_egg_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = glassInkSoft(),
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    stringResource(R.string.version_line),
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassInkSoft()
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = glassInkSoft())
        Text(value, style = MaterialTheme.typography.titleLarge, color = glassInk())
    }
}
