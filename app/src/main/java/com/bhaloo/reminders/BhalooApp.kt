package com.bhaloo.reminders

import android.app.Application
import com.bhaloo.reminders.alarm.ReminderScheduler
import com.bhaloo.reminders.data.BirthdaySeed
import com.bhaloo.reminders.data.ReminderStore
import com.bhaloo.reminders.notify.Notifications
import com.bhaloo.reminders.speech.BhalooSpeaker

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
        // Binding a TTS engine takes one to three seconds. Starting it now —
        // which also happens when an alarm wakes the process — means that cost
        // overlaps the notification work instead of delaying the speech.
        BhalooSpeaker.warmUp(this)
        seedBirthday()
        ReminderScheduler.rescheduleAll(this, store.reminders.value)
    }

    /** On the very first launch, the birthday is already there waiting. */
    private fun seedBirthday() {
        if (store.hasSeeded) return
        store.hasSeeded = true
        val seeded = store.upsert(
            BirthdaySeed.build(
                title = getString(R.string.seed_birthday_title),
                note = getString(R.string.seed_birthday_note)
            )
        )
        ReminderScheduler.schedule(this, seeded)
    }
}
