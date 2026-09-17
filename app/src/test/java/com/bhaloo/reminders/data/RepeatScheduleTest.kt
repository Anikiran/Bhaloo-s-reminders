package com.bhaloo.reminders.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Test

/**
 * A late reminder is a broken promise, so the calendar arithmetic gets tested
 * properly: every repeat mode, plus the awkward cases (month-end clamping,
 * a reminder untouched for a year, custom days that skip the chosen date).
 */
class RepeatScheduleTest {

    private fun millis(text: String): Long =
        LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun show(value: Long?): String? = value?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()).toString()
    }

    private fun reminder(
        at: String,
        repeat: RepeatMode = RepeatMode.ONCE,
        days: Set<Int> = emptySet()
    ) = Reminder(
        id = 1L,
        title = "Test",
        timeMillis = millis(at),
        repeat = repeat,
        daysOfWeek = days
    )

    private val now = millis("2026-09-17T09:00")

    @Test
    fun `one-shot in the future keeps its time`() {
        assertEquals(
            "2026-09-17T18:30",
            show(RepeatSchedule.nextTriggerMillis(reminder("2026-09-17T18:30"), now))
        )
    }

    @Test
    fun `one-shot already past has no next time`() {
        assertNull(RepeatSchedule.nextTriggerMillis(reminder("2026-09-16T18:30"), now))
    }

    @Test
    fun `daily moves to tomorrow once today's time has passed`() {
        assertEquals(
            "2026-09-18T07:00",
            show(
                RepeatSchedule.nextTriggerMillis(
                    reminder("2026-09-10T07:00", RepeatMode.DAILY), now
                )
            )
        )
    }

    @Test
    fun `daily stays on today when the time is still ahead`() {
        assertEquals(
            "2026-09-17T21:00",
            show(
                RepeatSchedule.nextTriggerMillis(
                    reminder("2026-09-10T21:00", RepeatMode.DAILY), now
                )
            )
        )
    }

    @Test
    fun `weekly keeps the same weekday`() {
        assertEquals(
            "2026-09-24T07:00",
            show(
                RepeatSchedule.nextTriggerMillis(
                    reminder("2026-09-10T07:00", RepeatMode.WEEKLY), now
                )
            )
        )
    }

    @Test
    fun `monthly on the 31st clamps to a short month`() {
        assertEquals(
            "2026-02-28T08:00",
            show(
                RepeatSchedule.nextTriggerMillis(
                    reminder("2026-01-31T08:00", RepeatMode.MONTHLY),
                    millis("2026-02-01T00:00")
                )
            )
        )
    }

    @Test
    fun `a birthday rolls over to next year`() {
        assertEquals(
            "2027-03-04T09:00",
            show(
                RepeatSchedule.nextTriggerMillis(
                    reminder("2026-03-04T09:00", RepeatMode.YEARLY), now
                )
            )
        )
    }

    @Test
    fun `custom days picks the next selected weekday`() {
        // Mon, Wed, Fri. 17 Sep 2026 is a Thursday, so Friday is next.
        assertEquals(
            "2026-09-18T06:30",
            show(
                RepeatSchedule.nextTriggerMillis(
                    reminder("2026-09-14T06:30", RepeatMode.CUSTOM_DAYS, setOf(1, 3, 5)), now
                )
            )
        )
    }

    @Test
    fun `custom days skips a future date that is not selected`() {
        // 19 Sep 2026 is a Saturday; only Monday is selected.
        assertEquals(
            "2026-09-21T06:30",
            show(
                RepeatSchedule.nextTriggerMillis(
                    reminder("2026-09-19T06:30", RepeatMode.CUSTOM_DAYS, setOf(1)), now
                )
            )
        )
    }

    @Test
    fun `custom days with nothing selected never fires`() {
        assertNull(
            RepeatSchedule.nextTriggerMillis(
                reminder("2026-09-10T06:30", RepeatMode.CUSTOM_DAYS), now
            )
        )
    }

    @Test
    fun `a daily reminder left alone for years still lands on the right time`() {
        assertEquals(
            "2026-09-18T05:45",
            show(
                RepeatSchedule.nextTriggerMillis(
                    reminder("2025-01-01T05:45", RepeatMode.DAILY), now
                )
            )
        )
    }

    @Test
    fun `Bhaloo's name is added to a message that leaves it out`() {
        val english = Reminder(1L, "Medicine", "take your medicine", timeMillis = now)
        assertEquals("Bhaloo, take your medicine", english.speechText())

        val hindi = Reminder(
            2L, "दवाई", "दवाई ले लो", VoiceLanguage.HINDI, timeMillis = now
        )
        assertEquals("भालू, दवाई ले लो", hindi.speechText())
    }

    @Test
    fun `a message that already says Bhaloo is left alone`() {
        val reminder = Reminder(1L, "Water", "Bhaloo, drink water", timeMillis = now)
        assertEquals("Bhaloo, drink water", reminder.speechText())
    }

    @Test
    fun `an empty message falls back to the title, then to a default nudge`() {
        assertEquals("Bhaloo, Gym", Reminder(1L, "Gym", timeMillis = now).speechText())
        assertEquals(
            "Bhaloo, this is your reminder.",
            Reminder(1L, "", timeMillis = now).speechText()
        )
    }
}
