package com.bhaloo.reminders.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bhaloo.reminders.R
import com.bhaloo.reminders.alarm.ReminderScheduler
import com.bhaloo.reminders.data.VoiceLanguage
import com.bhaloo.reminders.speech.BhalooVoice
import com.bhaloo.reminders.ui.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ReminderViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val store = viewModel.store

    var snooze by remember { mutableIntStateOf(store.snoozeMinutes) }
    var rate by remember { mutableFloatStateOf(store.speechRate) }
    var pitch by remember { mutableFloatStateOf(store.speechPitch) }
    var madeBy by remember { mutableStateOf(store.madeBy) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            SettingsCard(stringResource(R.string.settings_voice)) {
                Text(
                    stringResource(R.string.settings_rate, String.format("%.2f", rate)),
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = rate,
                    onValueChange = {
                        rate = it
                        store.speechRate = it
                    },
                    valueRange = 0.5f..1.5f
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.settings_pitch, String.format("%.2f", pitch)),
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = pitch,
                    onValueChange = {
                        pitch = it
                        store.speechPitch = it
                    },
                    valueRange = 0.5f..1.6f
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        viewModel.preview(
                            context.getString(R.string.settings_test_line_en),
                            VoiceLanguage.ENGLISH
                        )
                    }) { Text(stringResource(R.string.settings_test_en)) }
                    Button(onClick = {
                        viewModel.preview(
                            context.getString(R.string.settings_test_line_hi),
                            VoiceLanguage.HINDI
                        )
                    }) { Text(stringResource(R.string.settings_test_hi)) }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                BhalooVoice.installVoiceDataIntent()
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.settings_install_voice)) }
                Text(
                    stringResource(R.string.settings_install_voice_help),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SettingsCard(stringResource(R.string.settings_snooze)) {
                Text(
                    stringResource(R.string.settings_snooze_value, snooze),
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = snooze.toFloat(),
                    onValueChange = {
                        snooze = it.toInt().coerceIn(1, 60)
                        store.snoozeMinutes = snooze
                    },
                    valueRange = 1f..60f
                )
            }

            SettingsCard(stringResource(R.string.settings_reliability)) {
                val exactOk = viewModel.canScheduleExact()
                Text(
                    stringResource(
                        if (exactOk) R.string.settings_exact_ok else R.string.settings_exact_missing
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                if (!exactOk) {
                    OutlinedButton(
                        onClick = {
                            ReminderScheduler.exactAlarmSettingsIntent(context)?.let {
                                runCatching {
                                    context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.settings_allow_exact)) }
                }
                OutlinedButton(
                    onClick = {
                        runCatching {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.parse("package:" + context.packageName))
                            }
                            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.settings_battery)) }
                Text(
                    stringResource(R.string.settings_battery_help),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.rescheduleAll() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.settings_rearm)) }
            }

            SettingsCard(stringResource(R.string.settings_signature)) {
                OutlinedTextField(
                    value = madeBy,
                    onValueChange = {
                        madeBy = it
                        store.madeBy = it
                    },
                    label = { Text(stringResource(R.string.settings_made_by)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    stringResource(R.string.settings_made_by_help),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
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
