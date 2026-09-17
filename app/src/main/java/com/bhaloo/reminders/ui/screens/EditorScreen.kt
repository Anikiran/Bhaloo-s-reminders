package com.bhaloo.reminders.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bhaloo.reminders.R
import com.bhaloo.reminders.alarm.ReminderScheduler
import com.bhaloo.reminders.data.AlertStyle
import com.bhaloo.reminders.data.Reminder
import com.bhaloo.reminders.data.RepeatMode
import com.bhaloo.reminders.data.VoiceLanguage
import com.bhaloo.reminders.data.dayOfWeekValues
import com.bhaloo.reminders.ui.ReminderViewModel
import com.bhaloo.reminders.util.BhalooWords
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: ReminderViewModel,
    reminderId: Long?,
    onClose: () -> Unit
) {
    val existing = remember(reminderId) { reminderId?.let { viewModel.store.byId(it) } }
    val start = remember { existing?.dateTime ?: BhalooWords.nextRoundTime() }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var message by remember { mutableStateOf(existing?.spokenMessage ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var language by remember { mutableStateOf(existing?.language ?: VoiceLanguage.ENGLISH) }
    var date by remember { mutableStateOf(start.toLocalDate()) }
    var time by remember { mutableStateOf(start.toLocalTime().withSecond(0).withNano(0)) }
    var repeat by remember { mutableStateOf(existing?.repeat ?: RepeatMode.ONCE) }
    var days by remember { mutableStateOf(existing?.daysOfWeek ?: emptySet()) }
    var speakTimes by remember { mutableStateOf(existing?.speakTimes ?: 2) }
    var alertStyle by remember { mutableStateOf(existing?.alertStyle ?: AlertStyle.BOTH) }
    var vibrate by remember { mutableStateOf(existing?.vibrate ?: true) }
    var special by remember { mutableStateOf(existing?.isSpecialDay ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (existing == null) R.string.new_reminder else R.string.edit_reminder
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, stringResource(R.string.delete))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.field_title)) },
                placeholder = { Text(stringResource(R.string.field_title_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            SectionCard(title = stringResource(R.string.section_voice)) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(stringResource(R.string.field_message)) },
                    placeholder = { Text(BhalooWords.sampleMessage(language)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.field_message_help),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(10.dp))

                ChipRow {
                    VoiceLanguage.entries.forEach { option ->
                        FilterChip(
                            selected = language == option,
                            onClick = { language = option },
                            label = {
                                Text(
                                    when (option) {
                                        VoiceLanguage.ENGLISH -> stringResource(R.string.lang_english)
                                        VoiceLanguage.HINDI -> stringResource(R.string.lang_hindi)
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.field_quick_picks), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                ChipRow {
                    quickPicks(language).forEach { pick ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                if (title.isBlank()) title = pick.title
                                message = pick.message
                            },
                            label = { Text(pick.title) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val preview = Reminder(
                                id = 0L,
                                title = title,
                                spokenMessage = message,
                                language = language,
                                timeMillis = System.currentTimeMillis()
                            ).speechText()
                            viewModel.preview(preview, language, special)
                        }
                    ) {
                        Icon(Icons.Filled.PlayArrow, null)
                        Spacer(Modifier.height(0.dp))
                        Text(" " + stringResource(R.string.action_hear_it))
                    }
                    OutlinedButton(onClick = { viewModel.stopPreview() }) {
                        Text(stringResource(R.string.action_stop))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.field_speak_times, speakTimes),
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = speakTimes.toFloat(),
                    onValueChange = { speakTimes = it.toInt().coerceIn(1, 5) },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }

            SectionCard(title = stringResource(R.string.section_when)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(BhalooWords.formatDate(LocalDateTime.of(date, time)))
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(BhalooWords.formatTime(LocalDateTime.of(date, time)))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.field_repeat), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                ChipRow {
                    RepeatMode.entries.forEach { option ->
                        FilterChip(
                            selected = repeat == option,
                            onClick = { repeat = option },
                            label = { Text(repeatName(option)) }
                        )
                    }
                }

                if (repeat == RepeatMode.CUSTOM_DAYS) {
                    Spacer(Modifier.height(10.dp))
                    ChipRow {
                        dayOfWeekValues().forEach { day ->
                            FilterChip(
                                selected = days.contains(day),
                                onClick = {
                                    days = if (days.contains(day)) days - day else days + day
                                },
                                label = { Text(ReminderScheduler.dayLabel(day)) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = previewSchedule(
                        date = date,
                        time = time,
                        repeat = repeat,
                        days = days
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SectionCard(title = stringResource(R.string.section_how)) {
                ChipRow {
                    AlertStyle.entries.forEach { option ->
                        FilterChip(
                            selected = alertStyle == option,
                            onClick = { alertStyle = option },
                            label = { Text(alertStyleName(option)) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                ToggleRow(
                    label = stringResource(R.string.field_vibrate),
                    checked = vibrate,
                    onCheckedChange = { vibrate = it }
                )
                ToggleRow(
                    label = stringResource(R.string.field_special),
                    supporting = stringResource(R.string.field_special_help),
                    checked = special,
                    onCheckedChange = { special = it }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.field_note)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            error?.let {
                Text(
                    stringResource(it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    when {
                        title.isBlank() && message.isBlank() -> error = R.string.error_no_title
                        repeat == RepeatMode.CUSTOM_DAYS && days.isEmpty() ->
                            error = R.string.error_no_days

                        else -> {
                            val millis = LocalDateTime.of(date, time)
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            viewModel.save(
                                (existing ?: Reminder(id = 0L, title = "", timeMillis = millis)).copy(
                                    title = title.ifBlank { message.take(40) },
                                    spokenMessage = message,
                                    language = language,
                                    timeMillis = millis,
                                    repeat = repeat,
                                    daysOfWeek = days,
                                    enabled = true,
                                    speakTimes = speakTimes,
                                    alertStyle = alertStyle,
                                    vibrate = vibrate,
                                    isSpecialDay = special,
                                    note = note
                                )
                            )
                            onClose()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.action_save))
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(state.hour, state.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            title = { Text(stringResource(R.string.pick_time)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = state)
                }
            }
        )
    }

    if (showDeleteConfirm && existing != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_body, existing.title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(existing.id)
                    showDeleteConfirm = false
                    onClose()
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    supporting: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (supporting != null) {
                Text(supporting, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private data class QuickPick(val title: String, val message: String)

private fun quickPicks(language: VoiceLanguage): List<QuickPick> = when (language) {
    VoiceLanguage.HINDI -> listOf(
        QuickPick("दवाई", "भालू, दवाई लेने का समय हो गया है।"),
        QuickPick("पानी", "भालू, एक गिलास पानी पी लो।"),
        QuickPick("खाना", "भालू, खाना खा लो, देर मत करो।"),
        QuickPick("कॉल", "भालू, घर पर फ़ोन करना है।"),
        QuickPick("सोना", "भालू, अब सो जाओ, कल जल्दी उठना है।")
    )

    VoiceLanguage.ENGLISH -> listOf(
        QuickPick("Medicine", "Bhaloo, it's time for your medicine."),
        QuickPick("Water", "Bhaloo, drink a glass of water right now."),
        QuickPick("Meeting", "Bhaloo, your meeting starts in ten minutes."),
        QuickPick("Call home", "Bhaloo, call home. They are waiting."),
        QuickPick("Sleep", "Bhaloo, put the phone down and sleep.")
    )
}

@Composable
private fun repeatName(mode: RepeatMode): String = when (mode) {
    RepeatMode.ONCE -> stringResource(R.string.repeat_once)
    RepeatMode.DAILY -> stringResource(R.string.repeat_daily)
    RepeatMode.WEEKLY -> stringResource(R.string.repeat_weekly)
    RepeatMode.MONTHLY -> stringResource(R.string.repeat_monthly)
    RepeatMode.YEARLY -> stringResource(R.string.repeat_yearly)
    RepeatMode.CUSTOM_DAYS -> stringResource(R.string.repeat_custom)
}

@Composable
private fun alertStyleName(style: AlertStyle): String = when (style) {
    AlertStyle.NOTIFICATION -> stringResource(R.string.alert_notification)
    AlertStyle.VOICE -> stringResource(R.string.alert_voice)
    AlertStyle.BOTH -> stringResource(R.string.alert_both)
}

@Composable
private fun previewSchedule(
    date: LocalDate,
    time: LocalTime,
    repeat: RepeatMode,
    days: Set<Int>
): String {
    val reminder = Reminder(
        id = 0L,
        title = "",
        timeMillis = LocalDateTime.of(date, time)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        repeat = repeat,
        daysOfWeek = days
    )
    val next = ReminderScheduler.nextTriggerMillis(reminder)
        ?: return stringResource(R.string.schedule_in_past)
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(next), ZoneId.systemDefault())
    return stringResource(
        R.string.schedule_next,
        BhalooWords.whenLabel(dateTime),
        BhalooWords.relative(dateTime)
    )
}
