package com.bhaloo.reminders.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bhaloo.reminders.R
import com.bhaloo.reminders.alarm.ReminderScheduler
import com.bhaloo.reminders.data.NameVoice
import com.bhaloo.reminders.data.VoiceLanguage
import com.bhaloo.reminders.speech.BhalooVoice
import com.bhaloo.reminders.ui.ReminderViewModel
import com.bhaloo.reminders.ui.theme.Glass
import com.bhaloo.reminders.ui.theme.GlassButton
import com.bhaloo.reminders.ui.theme.GlassChip
import com.bhaloo.reminders.ui.theme.GlassCircleButton
import com.bhaloo.reminders.ui.theme.GlassField
import com.bhaloo.reminders.ui.theme.GlassOutlineButton
import com.bhaloo.reminders.ui.theme.GlassShapeMedium
import com.bhaloo.reminders.ui.theme.glassInk
import com.bhaloo.reminders.ui.theme.glassInkSoft

@Composable
fun SettingsScreen(viewModel: ReminderViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val store = viewModel.store

    var snooze by remember { mutableIntStateOf(store.snoozeMinutes) }
    var rate by remember { mutableFloatStateOf(store.speechRate) }
    var pitch by remember { mutableFloatStateOf(store.speechPitch) }
    var madeBy by remember { mutableStateOf(store.madeBy) }
    var spokenName by remember { mutableStateOf(store.spokenNameEnglish) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            GlassTopBar(
                title = stringResource(R.string.settings),
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ---- Saying the name right -------------------------------
                GlassSection(stringResource(R.string.settings_name)) {
                    Column {
                        Text(
                            stringResource(R.string.settings_name_help),
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassInkSoft()
                        )
                        Spacer(Modifier.height(12.dp))
                        GlassField(
                            value = spokenName,
                            onValueChange = {
                                spokenName = it
                                store.spokenNameEnglish = it
                            },
                            placeholder = NameVoice.DEFAULT_SPOKEN_EN,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NameVoice.SUGGESTIONS_EN.forEach { option ->
                                GlassChip(
                                    selected = spokenName == option,
                                    onClick = {
                                        spokenName = option
                                        store.spokenNameEnglish = option
                                        viewModel.preview(
                                            context.getString(R.string.settings_name_test, option),
                                            VoiceLanguage.ENGLISH
                                        )
                                    },
                                    label = option
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        GlassButton(
                            onClick = {
                                viewModel.preview(
                                    context.getString(
                                        R.string.settings_name_test,
                                        spokenName.ifBlank { NameVoice.DEFAULT_SPOKEN_EN }
                                    ),
                                    VoiceLanguage.ENGLISH
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            height = 50.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PlayArrow, null, tint = Color.White)
                                Text(
                                    " " + stringResource(R.string.settings_name_hear),
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // ---- Voice ------------------------------------------------
                GlassSection(stringResource(R.string.settings_voice)) {
                    Column {
                        Text(
                            stringResource(R.string.settings_rate, String.format("%.2f", rate)),
                            style = MaterialTheme.typography.labelLarge,
                            color = glassInkSoft()
                        )
                        TintedSlider(
                            value = rate,
                            onValueChange = { rate = it; store.speechRate = it },
                            valueRange = 0.5f..1.5f
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.settings_pitch, String.format("%.2f", pitch)),
                            style = MaterialTheme.typography.labelLarge,
                            color = glassInkSoft()
                        )
                        TintedSlider(
                            value = pitch,
                            onValueChange = { pitch = it; store.speechPitch = it },
                            valueRange = 0.5f..1.6f
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            GlassButton(
                                onClick = {
                                    viewModel.preview(
                                        context.getString(R.string.settings_test_line_en),
                                        VoiceLanguage.ENGLISH
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                height = 48.dp
                            ) {
                                Text(
                                    stringResource(R.string.settings_test_en),
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            GlassButton(
                                onClick = {
                                    viewModel.preview(
                                        context.getString(R.string.settings_test_line_hi),
                                        VoiceLanguage.HINDI
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                fill = Glass.secondaryFill,
                                glowColor = Glass.shadowMint,
                                height = 48.dp
                            ) {
                                Text(
                                    stringResource(R.string.settings_test_hi),
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        GlassOutlineButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        BhalooVoice.installVoiceDataIntent()
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            height = 50.dp
                        ) {
                            Text(stringResource(R.string.settings_install_voice), color = glassInk())
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.settings_install_voice_help),
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassInkSoft()
                        )
                    }
                }

                // ---- Snooze -----------------------------------------------
                GlassSection(stringResource(R.string.settings_snooze)) {
                    Column {
                        Text(
                            stringResource(R.string.settings_snooze_value, snooze),
                            style = MaterialTheme.typography.labelLarge,
                            color = glassInkSoft()
                        )
                        TintedSlider(
                            value = snooze.toFloat(),
                            onValueChange = {
                                snooze = it.toInt().coerceIn(1, 60)
                                store.snoozeMinutes = snooze
                            },
                            valueRange = 1f..60f
                        )
                    }
                }

                // ---- Reliability ------------------------------------------
                GlassSection(stringResource(R.string.settings_reliability)) {
                    Column {
                        val exactOk = viewModel.canScheduleExact()
                        Text(
                            stringResource(
                                if (exactOk) R.string.settings_exact_ok
                                else R.string.settings_exact_missing
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassInkSoft()
                        )
                        Spacer(Modifier.height(10.dp))
                        if (!exactOk) {
                            GlassButton(
                                onClick = {
                                    ReminderScheduler.exactAlarmSettingsIntent(context)?.let {
                                        runCatching {
                                            context.startActivity(
                                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                height = 50.dp
                            ) {
                                Text(
                                    stringResource(R.string.settings_allow_exact),
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        GlassOutlineButton(
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
                            modifier = Modifier.fillMaxWidth(),
                            height = 50.dp
                        ) {
                            Text(stringResource(R.string.settings_battery), color = glassInk())
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.settings_battery_help),
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassInkSoft()
                        )
                        Spacer(Modifier.height(12.dp))
                        GlassOutlineButton(
                            onClick = { viewModel.rescheduleAll() },
                            modifier = Modifier.fillMaxWidth(),
                            height = 50.dp
                        ) {
                            Text(stringResource(R.string.settings_rearm), color = glassInk())
                        }
                    }
                }

                // ---- Signature --------------------------------------------
                GlassSection(stringResource(R.string.settings_signature)) {
                    Column {
                        GlassField(
                            value = madeBy,
                            onValueChange = { madeBy = it; store.madeBy = it },
                            placeholder = stringResource(R.string.settings_made_by),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.settings_made_by_help),
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassInkSoft()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TintedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Glass.Violet,
            inactiveTrackColor = Color.White.copy(alpha = 0.45f)
        )
    )
}
