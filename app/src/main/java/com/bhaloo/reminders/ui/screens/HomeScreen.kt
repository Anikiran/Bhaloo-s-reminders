package com.bhaloo.reminders.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.bhaloo.reminders.util.BhalooWords

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ReminderViewModel,
    onAdd: () -> Unit,
    onEdit: (Reminder) -> Unit,
    onAbout: () -> Unit,
    onSettings: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, stringResource(R.string.settings))
                    }
                    IconButton(onClick = onAbout) {
                        Icon(Icons.Filled.Info, stringResource(R.string.about))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text(stringResource(R.string.new_reminder)) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { GreetingCard(onAbout = onAbout) }

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
}

@Composable
private fun GreetingCard(onAbout: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onAbout)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.bhaloo_portrait),
                contentDescription = stringResource(R.string.bhaloo_photo_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = BhalooWords.greeting(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = BhalooWords.greetingHindi(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = BhalooWords.lineOfTheDay(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun ExactAlarmWarning() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable {
                    ReminderScheduler.exactAlarmSettingsIntent(context)?.let { intent ->
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(intent) }
                    }
                }
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.exact_alarm_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.exact_alarm_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🐻", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.empty_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val dimmed = !reminder.enabled || reminder.isDone
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dimmed) 0.55f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isSpecialDay) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
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
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = BhalooWords.whenLabel(reminder.dateTime),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = repeatLabel(reminder) + " · " + languageLabel(reminder.language) +
                        " · " + BhalooWords.relative(reminder.dateTime),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (reminder.spokenMessage.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "“" + reminder.spokenMessage + "”",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Box(contentAlignment = Alignment.Center) {
                Switch(checked = reminder.enabled, onCheckedChange = onToggle)
            }
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
