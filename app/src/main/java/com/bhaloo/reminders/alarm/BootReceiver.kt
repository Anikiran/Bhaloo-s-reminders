package com.bhaloo.reminders.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bhaloo.reminders.BhalooApp

/**
 * Alarms do not survive a reboot, a time-zone change or an app update,
 * so every reminder is re-armed whenever any of those happen.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? BhalooApp ?: return
        val reminders = app.store.reminders.value
        ReminderScheduler.rescheduleAll(context, reminders)
    }
}
