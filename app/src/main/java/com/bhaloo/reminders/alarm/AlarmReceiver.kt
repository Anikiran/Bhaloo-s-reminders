package com.bhaloo.reminders.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.bhaloo.reminders.BhalooApp
import com.bhaloo.reminders.data.AlertStyle
import com.bhaloo.reminders.notify.Notifications
import com.bhaloo.reminders.speech.SpeakerService

/**
 * The moment of truth: the OS wakes us up, Bhaloo gets a notification and,
 * a breath later, hears the message in his own language.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        if (id <= 0L) return

        val app = context.applicationContext as? BhalooApp ?: return
        val store = app.store
        val reminder = store.byId(id) ?: return

        Notifications.showReminder(context, reminder)

        if (reminder.alertStyle.speaks) {
            val speakIntent = Intent(context, SpeakerService::class.java).apply {
                putExtra(SpeakerService.EXTRA_TEXT, reminder.speechText())
                putExtra(SpeakerService.EXTRA_LANGUAGE, reminder.language.name)
                putExtra(SpeakerService.EXTRA_TIMES, reminder.speakTimes)
                putExtra(SpeakerService.EXTRA_ID, reminder.id)
                putExtra(SpeakerService.EXTRA_SPECIAL, reminder.isSpecialDay)
                // Let the notification chime finish before speaking over it.
                putExtra(
                    SpeakerService.EXTRA_DELAY_MS,
                    if (reminder.alertStyle == AlertStyle.BOTH) 1400L else 300L
                )
            }
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, speakIntent)
                } else {
                    context.startService(speakIntent)
                }
            }
        }

        // Roll the calendar rule forward and arm the next alarm.
        val next = ReminderScheduler.nextOccurrenceAfter(reminder, System.currentTimeMillis())
        store.markFired(reminder.id, next)
        store.byId(reminder.id)?.let { ReminderScheduler.schedule(context, it) }
    }
}
