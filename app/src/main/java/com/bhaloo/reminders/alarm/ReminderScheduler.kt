package com.bhaloo.reminders.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.bhaloo.reminders.data.Reminder
import com.bhaloo.reminders.data.RepeatSchedule
import com.bhaloo.reminders.ui.MainActivity
import java.time.DayOfWeek

/**
 * Turns a [Reminder] into a real OS alarm. Everything that touches [AlarmManager]
 * lives here so there is exactly one place to look when a reminder is late.
 */
object ReminderScheduler {

    const val EXTRA_REMINDER_ID = "reminder_id"
    const val ACTION_FIRE = "com.bhaloo.reminders.ACTION_FIRE"

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val manager = context.getSystemService(AlarmManager::class.java)
        return manager?.canScheduleExactAlarms() == true
    }

    /** Intent that opens the system page where exact alarms can be allowed. */
    fun exactAlarmSettingsIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .setData(android.net.Uri.parse("package:" + context.packageName))
    }

    fun schedule(context: Context, reminder: Reminder) {
        cancel(context, reminder.id)
        if (!reminder.enabled) return

        val triggerAt = nextTriggerMillis(reminder) ?: return
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val operation = firePendingIntent(context, reminder.id)

        if (canScheduleExact(context)) {
            val showIntent = PendingIntent.getActivity(
                context,
                (reminder.id + ALARM_CLOCK_OFFSET).toInt(),
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            manager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, showIntent), operation)
        } else {
            // Exact alarms not granted: still fire, just without the doze-proof guarantee.
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        }
    }

    fun cancel(context: Context, reminderId: Long) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        manager.cancel(firePendingIntent(context, reminderId))
    }

    fun rescheduleAll(context: Context, reminders: List<Reminder>) {
        reminders.forEach { schedule(context, it) }
    }

    /** One-off alarm used by the snooze action; does not touch the reminder's own schedule. */
    fun scheduleSnooze(context: Context, reminderId: Long, minutes: Int) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        val operation = firePendingIntent(context, reminderId)
        if (canScheduleExact(context)) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        } else {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        }
    }

    private fun firePendingIntent(context: Context, reminderId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            // Keeps PendingIntents for different reminders distinct even though
            // filterEquals() ignores extras.
            data = android.net.Uri.parse("bhaloo://reminder/$reminderId")
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /** @see RepeatSchedule.nextTriggerMillis */
    fun nextTriggerMillis(reminder: Reminder, fromMillis: Long = System.currentTimeMillis()): Long? =
        RepeatSchedule.nextTriggerMillis(reminder, fromMillis)

    /** @see RepeatSchedule.nextOccurrenceAfter */
    fun nextOccurrenceAfter(reminder: Reminder, fromMillis: Long): Long? =
        RepeatSchedule.nextOccurrenceAfter(reminder, fromMillis)

    /** Human-readable day label, e.g. "Mon". */
    fun dayLabel(value: Int): String =
        DayOfWeek.of(value).getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())

    private const val ALARM_CLOCK_OFFSET = 500_000L
}
