package com.bhaloo.reminders.ui

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.core.content.ContextCompat
import com.bhaloo.reminders.BhalooApp
import com.bhaloo.reminders.alarm.ReminderScheduler
import com.bhaloo.reminders.data.Reminder
import com.bhaloo.reminders.data.ReminderStore
import com.bhaloo.reminders.data.VoiceLanguage
import com.bhaloo.reminders.notify.Notifications
import com.bhaloo.reminders.speech.BhalooSpeaker
import com.bhaloo.reminders.speech.SpeakerService
import kotlinx.coroutines.flow.StateFlow

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val app: BhalooApp get() = getApplication()
    val store: ReminderStore get() = app.store

    val reminders: StateFlow<List<Reminder>> get() = store.reminders

    fun save(reminder: Reminder) {
        // Always store the next real occurrence, never a time in the past.
        val normalised = ReminderScheduler.nextTriggerMillis(reminder)
            ?.let { reminder.copy(timeMillis = it) }
            ?: reminder
        val stored = store.upsert(normalised)
        ReminderScheduler.schedule(app, stored)
    }

    fun delete(id: Long) {
        ReminderScheduler.cancel(app, id)
        Notifications.cancel(app, id)
        store.delete(id)
    }

    fun setEnabled(reminder: Reminder, enabled: Boolean) {
        store.setEnabled(reminder.id, enabled)
        val updated = store.byId(reminder.id) ?: return
        if (enabled) ReminderScheduler.schedule(app, updated)
        else ReminderScheduler.cancel(app, reminder.id)
    }

    /**
     * "Hear it now" in the editor — speaks exactly what will be said later.
     *
     * Deliberately does NOT go through [SpeakerService]. Starting a foreground
     * service costs most of a second, which is absurd for a button whose only
     * job is to answer immediately; the shared engine is already warm, so this
     * speaks more or less the moment it is tapped.
     */
    fun preview(text: String, language: VoiceLanguage, special: Boolean = false) {
        BhalooSpeaker.nudgeAlarmVolume(app)
        BhalooSpeaker.speak(
            context = app,
            text = text,
            language = language,
            times = 1,
            special = special,
            spokenNameEnglish = store.spokenNameEnglish,
            rate = store.speechRate,
            pitch = store.speechPitch,
            onFinished = {}
        )
    }

    fun stopPreview() = BhalooSpeaker.stop()

    fun rescheduleAll() = ReminderScheduler.rescheduleAll(app, reminders.value)

    /**
     * Schedules a real reminder 60 seconds out so background delivery can be
     * proved on the phone itself: press it, lock the phone, put it down.
     *
     * It is an ordinary one-shot reminder — it goes through exactly the same
     * alarm, receiver and speaking path as every other one, which is the whole
     * point. Anything simpler would test a different code path than the one
     * that matters.
     */
    fun scheduleBackgroundTest(title: String, message: String) {
        save(
            Reminder(
                id = 0L,
                title = title,
                spokenMessage = message,
                language = VoiceLanguage.ENGLISH,
                timeMillis = System.currentTimeMillis() + 60_000L,
                repeat = com.bhaloo.reminders.data.RepeatMode.ONCE,
                speakTimes = 1
            )
        )
    }

    fun canScheduleExact(): Boolean = ReminderScheduler.canScheduleExact(app)
}
