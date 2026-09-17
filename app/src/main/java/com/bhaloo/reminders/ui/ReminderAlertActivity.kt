package com.bhaloo.reminders.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhaloo.reminders.BhalooApp
import com.bhaloo.reminders.R
import com.bhaloo.reminders.alarm.ActionReceiver
import com.bhaloo.reminders.alarm.ReminderScheduler
import com.bhaloo.reminders.data.Reminder
import com.bhaloo.reminders.notify.Notifications
import com.bhaloo.reminders.speech.SpeakerService
import com.bhaloo.reminders.ui.theme.BhalooTheme
import com.bhaloo.reminders.ui.theme.Glass
import com.bhaloo.reminders.ui.theme.GlassButton
import com.bhaloo.reminders.ui.theme.GlassOutlineButton
import com.bhaloo.reminders.ui.theme.GlassPane
import com.bhaloo.reminders.ui.theme.MeshBackground
import com.bhaloo.reminders.ui.theme.glassInk
import com.bhaloo.reminders.ui.theme.glassInkSoft
import com.bhaloo.reminders.util.BhalooWords

/**
 * The full-screen face of a reminder: shown over the lock screen when one fires,
 * so Bhaloo sees it even if the phone is face-down on the table.
 */
class ReminderAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        val id = intent.getLongExtra(EXTRA_ID, -1L)
        val store = (applicationContext as? BhalooApp)?.store
        val reminder = store?.byId(id)

        setContent {
            BhalooTheme {
                AlertContent(
                    reminder = reminder,
                    onDone = {
                        stopSpeaking()
                        if (reminder != null) {
                            store?.markCompleted(reminder.id)
                            Notifications.cancel(this, reminder.id)
                        }
                        finish()
                    },
                    onSnooze = {
                        stopSpeaking()
                        if (reminder != null) {
                            Notifications.cancel(this, reminder.id)
                            ReminderScheduler.scheduleSnooze(
                                this,
                                reminder.id,
                                store?.snoozeMinutes ?: 10
                            )
                        }
                        finish()
                    },
                    onRepeat = {
                        if (reminder != null) {
                            startService(
                                Intent(this, SpeakerService::class.java).apply {
                                    putExtra(SpeakerService.EXTRA_TEXT, reminder.speechText())
                                    putExtra(SpeakerService.EXTRA_LANGUAGE, reminder.language.name)
                                    putExtra(SpeakerService.EXTRA_TIMES, 1)
                                    putExtra(SpeakerService.EXTRA_ID, reminder.id)
                                    putExtra(SpeakerService.EXTRA_DELAY_MS, 150L)
                                }
                            )
                        }
                    },
                    onDismiss = {
                        stopSpeaking()
                        finish()
                    }
                )
            }
        }
    }

    private fun stopSpeaking() {
        runCatching {
            sendBroadcast(
                Intent(this, ActionReceiver::class.java).setAction(ActionReceiver.ACTION_STOP)
                    .putExtra(ActionReceiver.EXTRA_ID, intent.getLongExtra(EXTRA_ID, -1L))
            )
        }
        runCatching { stopService(Intent(this, SpeakerService::class.java)) }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        const val EXTRA_ID = "reminder_id"
    }
}

@androidx.compose.runtime.Composable
private fun AlertContent(
    reminder: Reminder?,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
    onRepeat: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        GlassPane(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 22.dp)
                .fillMaxWidth(),
            elevation = 30.dp,
            tint = if (reminder?.isSpecialDay == true) Glass.heroFill else null,
            iridescent = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.bhaloo_portrait),
                    contentDescription = stringResource(R.string.bhaloo_photo_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(112.dp)
                        .shadow(22.dp, CircleShape, clip = false, spotColor = Glass.shadowViolet)
                        .clip(CircleShape)
                        .border(3.dp, Glass.iridescentRim, CircleShape)
                )
                Spacer(Modifier.height(20.dp))

                if (reminder == null) {
                    Text(
                        stringResource(R.string.alert_missing),
                        style = MaterialTheme.typography.titleLarge,
                        color = glassInk(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    GlassButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.action_close),
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    return@Column
                }

                Text(
                    text = if (reminder.isSpecialDay) "\uD83C\uDF82 " + reminder.title else reminder.title,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    color = glassInk()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = reminder.speechText(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = glassInk()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = BhalooWords.whenLabel(reminder.dateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassInkSoft()
                )
                if (reminder.note.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = reminder.note,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = glassInkSoft()
                    )
                }

                Spacer(Modifier.height(30.dp))
                GlassButton(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                    height = 56.dp
                ) {
                    Text(
                        stringResource(R.string.action_done),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                GlassButton(
                    onClick = onSnooze,
                    modifier = Modifier.fillMaxWidth(),
                    fill = Glass.secondaryFill,
                    glowColor = Glass.shadowMint,
                    height = 56.dp
                ) {
                    Text(
                        stringResource(R.string.action_snooze),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                GlassOutlineButton(
                    onClick = onRepeat,
                    modifier = Modifier.fillMaxWidth(),
                    height = 50.dp
                ) {
                    Text(stringResource(R.string.action_repeat), color = glassInk())
                }
            }
        }
    }
}
