package com.bhaloo.reminders.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bhaloo.reminders.BhalooApp
import com.bhaloo.reminders.R
import com.bhaloo.reminders.notify.Notifications
import com.bhaloo.reminders.speech.SpeakerService

/** Handles the Done / Snooze / Say it again buttons on the notification. */
class ActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        if (id <= 0L) return
        val app = context.applicationContext as? BhalooApp ?: return
        val store = app.store

        when (intent.action) {
            ACTION_DONE -> {
                stopSpeaking(context)
                store.markCompleted(id)
                Notifications.cancel(context, id)
                Toast.makeText(context, R.string.toast_done, Toast.LENGTH_SHORT).show()
            }

            ACTION_SNOOZE -> {
                stopSpeaking(context)
                Notifications.cancel(context, id)
                val minutes = store.snoozeMinutes
                ReminderScheduler.scheduleSnooze(context, id, minutes)
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_snoozed, minutes),
                    Toast.LENGTH_SHORT
                ).show()
            }

            ACTION_SPEAK_AGAIN -> {
                val reminder = store.byId(id) ?: return
                val speakIntent = Intent(context, SpeakerService::class.java).apply {
                    putExtra(SpeakerService.EXTRA_TEXT, reminder.speechText())
                    putExtra(SpeakerService.EXTRA_LANGUAGE, reminder.language.name)
                    putExtra(SpeakerService.EXTRA_TIMES, 1)
                    putExtra(SpeakerService.EXTRA_ID, reminder.id)
                    putExtra(SpeakerService.EXTRA_DELAY_MS, 200L)
                }
                runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(context, speakIntent)
                    } else {
                        context.startService(speakIntent)
                    }
                }
            }

            ACTION_STOP -> stopSpeaking(context)
        }
    }

    private fun stopSpeaking(context: Context) {
        runCatching { context.stopService(Intent(context, SpeakerService::class.java)) }
    }

    companion object {
        const val EXTRA_ID = "reminder_id"
        const val ACTION_DONE = "com.bhaloo.reminders.DONE"
        const val ACTION_SNOOZE = "com.bhaloo.reminders.SNOOZE"
        const val ACTION_SPEAK_AGAIN = "com.bhaloo.reminders.SPEAK_AGAIN"
        const val ACTION_STOP = "com.bhaloo.reminders.STOP"
    }
}
