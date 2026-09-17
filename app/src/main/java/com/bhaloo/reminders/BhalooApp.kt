package com.bhaloo.reminders

import android.app.Application
import com.bhaloo.reminders.alarm.ReminderScheduler
import com.bhaloo.reminders.data.ReminderStore
import com.bhaloo.reminders.notify.Notifications

/**
 * Bhaloo's Reminders — built for one person in particular.
 *
 * Everything hangs off this class: one store, one set of notification channels,
 * and a re-arm pass every time the app starts so a missed alarm can never
 * silently stay missed.
 */
class BhalooApp : Application() {

    lateinit var store: ReminderStore
        private set

    override fun onCreate() {
        super.onCreate()
        store = ReminderStore(this)
        Notifications.createChannels(this)
        store.installedAt // stamps the "together since" date on first launch
        ReminderScheduler.rescheduleAll(this, store.reminders.value)
    }
}
