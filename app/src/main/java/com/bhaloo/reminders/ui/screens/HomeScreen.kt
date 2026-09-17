package com.bhaloo.reminders.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhaloo.reminders.R
import com.bhaloo.reminders.alarm.ReminderScheduler
import com.bhaloo.reminders.data.Reminder
import com.bhaloo.reminders.data.RepeatMode
import com.bhaloo.reminders.data.VoiceLanguage
import com.bhaloo.reminders.ui.ReminderViewModel
import com.bhaloo.reminders.ui.theme.Glass
import com.bhaloo.reminders.ui.theme.GlassButton
import com.bhaloo.reminders.ui.theme.GlassCircleButton
import com.bhaloo.reminders.ui.theme.GlassPane
import com.bhaloo.reminders.ui.theme.GlassShapeMedium
import com.bhaloo.reminders.ui.theme.GlassSwitch
import com.bhaloo.reminders.ui.theme.glassInk
import com.bhaloo.reminders.ui.theme.glassInkSoft
import com.bhaloo.reminders.util.BhalooWords

@Composable
fun HomeScreen(
    viewModel: ReminderViewModel,
    onAdd: () -> Unit,
    onEdit: (Reminder) -> Unit,
    onAbout: () -> Unit,
    onSettings: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            GlassTopBar(
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.home_subtitle),
                actions = {
                    GlassCircleButton(onClick = onSettings, diameter = 42.dp) {
                        Icon(
                            Icons.Filled.Settings,
                            stringResource(R.string.settings),
                            tint = glassInk()
                        )
                    }
                    GlassCircleButton(onClick = onAbout, diameter = 42.dp) {
                        Icon(
                            Icons.Filled.Info,
                            stringResource(R.string.about),
                            tint = glassInk()
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 6.dp, 16.dp, 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { GreetingCard(onClick = onAbout) }

                if (!viewModel.canScheduleExact()) {
                    item { ExactAlarmWarning() }
                }

                if (reminders.isEmpty()) {
                    item { EmptyState() }
                }

                items(reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onClick = { onEdit(reminder) },
                        onToggle = { viewModel.setEnabled(reminder, it) }
                    )
                }
            }
        }

        GlassButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 22.dp)
                .fillMaxWidth(0.72f),
            height = 58.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, null, tint = androidx.compose.ui.graphics.Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.new_reminder),
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** The pastel hero card, lifted straight from the reference's bottom-right. */
@Composable
private fun GreetingCard(onClick: () -> Unit) {
    GlassPane(
        modifier = Modifier.fillMaxWidth(),
        tint = Glass.heroFill,
        elevation = 20.dp,
        iridescent = true
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.bhaloo_portrait),
                contentDescription = stringResource(R.string.bhaloo_photo_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(62.dp)
                    .shadow(14.dp, CircleShape, clip = false, spotColor = Glass.shadowSpot)
                    .clip(CircleShape)
                    .border(2.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = BhalooWords.greeting(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Glass.Ink
                )
                Text(
                    text = BhalooWords.greetingHindi(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Glass.Ink.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = BhalooWords.lineOfTheDay(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Glass.Ink.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun ExactAlarmWarning() {
    val context = androidx.compose.ui.platform.LocalContext.current
    GlassPane(
        modifier = Modifier.fillMaxWidth(),
        tint = androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(
                Glass.Peach.copy(alpha = 0.9f),
                Glass.Pink.copy(alpha = 0.85f)
            )
        ),
        elevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    ReminderScheduler.exactAlarmSettingsIntent(context)?.let { intent ->
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(intent) }
                    }
                }
                .padding(18.dp)
        ) {
            Text(
                stringResource(R.string.exact_alarm_title),
                style = MaterialTheme.typography.titleMedium,
                color = Glass.Ink
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.exact_alarm_body),
                style = MaterialTheme.typography.bodyMedium,
                color = Glass.Ink.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    GlassPane(modifier = Modifier.fillMaxWidth(), elevation = 12.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🐻", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = glassInk(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = glassInkSoft(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val dimmed = !reminder.enabled || reminder.isDone
    GlassPane(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dimmed) 0.62f else 1f),
        shape = GlassShapeMedium,
        cornerRadius = 22.dp,
        elevation = if (dimmed) 8.dp else 16.dp,
        tint = if (reminder.isSpecialDay) Glass.heroFill else null,
        iridescent = reminder.isSpecialDay
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (reminder.isSpecialDay) {
                        Text("🎂 ", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = reminder.title.ifBlank { stringResource(R.string.untitled) },
                        style = MaterialTheme.typography.titleLarge,
                        color = if (reminder.isSpecialDay) Glass.Ink else glassInk()
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    text = BhalooWords.whenLabel(reminder.dateTime),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (reminder.isSpecialDay) Glass.Ink.copy(alpha = 0.85f) else glassInk()
                )
                Text(
                    text = repeatLabel(reminder) + " · " + languageLabel(reminder.language) +
                        " · " + BhalooWords.relative(reminder.dateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (reminder.isSpecialDay) Glass.Ink.copy(alpha = 0.7f) else glassInkSoft()
                )
                if (reminder.spokenMessage.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "“" + reminder.spokenMessage + "”",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (reminder.isSpecialDay) Glass.Ink.copy(alpha = 0.7f) else glassInkSoft()
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            GlassSwitch(checked = reminder.enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun repeatLabel(reminder: Reminder): String = when (reminder.repeat) {
    RepeatMode.ONCE -> stringResource(R.string.repeat_once)
    RepeatMode.DAILY -> stringResource(R.string.repeat_daily)
    RepeatMode.WEEKLY -> stringResource(R.string.repeat_weekly)
    RepeatMode.MONTHLY -> stringResource(R.string.repeat_monthly)
    RepeatMode.YEARLY -> stringResource(R.string.repeat_yearly)
    RepeatMode.CUSTOM_DAYS -> reminder.daysOfWeek.sorted()
        .joinToString(" ") { ReminderScheduler.dayLabel(it) }
        .ifBlank { stringResource(R.string.repeat_custom) }
}

@Composable
private fun languageLabel(language: VoiceLanguage): String = when (language) {
    VoiceLanguage.ENGLISH -> stringResource(R.string.lang_english)
    VoiceLanguage.HINDI -> stringResource(R.string.lang_hindi)
}
