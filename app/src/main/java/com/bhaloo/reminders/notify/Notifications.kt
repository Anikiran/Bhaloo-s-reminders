package com.bhaloo.reminders.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bhaloo.reminders.R
import com.bhaloo.reminders.alarm.ActionReceiver
import com.bhaloo.reminders.data.AlertStyle
import com.bhaloo.reminders.data.Reminder
import com.bhaloo.reminders.ui.ReminderAlertActivity

object Notifications {

    const val CHANNEL_REMINDERS = "bhaloo_reminders_v2"
    const val CHANNEL_SPEAKING = "bhaloo_speaking"

    /** Foreground-service notification id; reminder notifications use the reminder id. */
    const val SPEAKING_NOTIFICATION_ID = 424242

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val reminders = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.channel_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_reminders_desc)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
            setSound(
                Uri.parse("android.resource://" + context.packageName + "/" + R.raw.bhaloo_chime),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        val speaking = NotificationChannel(
            CHANNEL_SPEAKING,
            context.getString(R.string.channel_speaking),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.channel_speaking_desc)
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannel(reminders)
        manager.createNotificationChannel(speaking)
    }

    /** The notification Bhaloo actually sees when a reminder goes off. */
    fun showReminder(context: Context, reminder: Reminder) {
        if (reminder.alertStyle == AlertStyle.VOICE) return
        val notification = buildReminderNotification(context, reminder)
        runCatching {
            NotificationManagerCompat.from(context).notify(reminder.id.toInt(), notification)
        }
    }

    fun buildReminderNotification(
        context: Context,
        reminder: Reminder
    ): android.app.Notification {
        val alertIntent = Intent(context, ReminderAlertActivity::class.java).apply {
            putExtra(ReminderAlertActivity.EXTRA_ID, reminder.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            alertIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (reminder.isSpecialDay) {
            context.getString(R.string.notif_special_title, reminder.title)
        } else {
            context.getString(R.string.notif_title, reminder.title)
        }

        val body = reminder.spokenMessage.ifBlank { reminder.note }
            .ifBlank { context.getString(R.string.notif_default_body) }

        return NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_bhaloo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(contentIntent, true)
            .setVibrate(if (reminder.vibrate) longArrayOf(0, 400, 200, 400) else longArrayOf(0))
            .addAction(
                R.drawable.ic_stat_bhaloo,
                context.getString(R.string.action_done),
                actionIntent(context, ActionReceiver.ACTION_DONE, reminder.id, 10_000)
            )
            .addAction(
                R.drawable.ic_stat_bhaloo,
                context.getString(R.string.action_snooze),
                actionIntent(context, ActionReceiver.ACTION_SNOOZE, reminder.id, 20_000)
            )
            .addAction(
                R.drawable.ic_stat_bhaloo,
                context.getString(R.string.action_repeat),
                actionIntent(context, ActionReceiver.ACTION_SPEAK_AGAIN, reminder.id, 30_000)
            )
            .build()
    }

    /** Quiet notification the speaking service runs in the foreground with. */
    fun buildSpeakingNotification(context: Context, spokenText: String): android.app.Notification =
        NotificationCompat.Builder(context, CHANNEL_SPEAKING)
            .setSmallIcon(R.drawable.ic_stat_bhaloo)
            .setContentTitle(context.getString(R.string.speaking_title))
            .setContentText(spokenText.take(120))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .build()

    fun cancel(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(reminderId.toInt())
    }

    private fun actionIntent(
        context: Context,
        action: String,
        reminderId: Long,
        requestOffset: Int
    ): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java).apply {
            this.action = action
            putExtra(ActionReceiver.EXTRA_ID, reminderId)
            data = Uri.parse("bhaloo://action/$action/$reminderId")
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt() + requestOffset,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
