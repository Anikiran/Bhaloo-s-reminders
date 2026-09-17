package com.bhaloo.reminders.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.bhaloo_portrait),
                contentDescription = stringResource(R.string.bhaloo_photo_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(20.dp))

            if (reminder == null) {
                Text(
                    stringResource(R.string.alert_missing),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_close))
                }
                return@Column
            }

            Text(
                text = if (reminder.isSpecialDay) "🎂 " + reminder.title else reminder.title,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = reminder.speechText(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = BhalooWords.whenLabel(reminder.dateTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (reminder.note.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = reminder.note,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) { Text(stringResource(R.string.action_done)) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) { Text(stringResource(R.string.action_snooze)) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onRepeat,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.action_repeat)) }
        }
    }
}
