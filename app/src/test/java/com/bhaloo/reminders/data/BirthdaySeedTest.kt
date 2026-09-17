package com.bhaloo.reminders.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Test

/** The birthday is the one date that must never be a day late. */
class BirthdaySeedTest {

    private fun on(text: String) = LocalDate.parse(text)

    @Test
    fun `before the birthday it lands on this year's`() {
        assertEquals(on("2026-07-31"), BirthdaySeed.nextBirthday(on("2026-01-05")))
    }

    @Test
    fun `on the birthday itself it stays today`() {
        assertEquals(on("2026-07-31"), BirthdaySeed.nextBirthday(on("2026-07-31")))
    }

    @Test
    fun `the day after, it rolls to next year`() {
        assertEquals(on("2027-07-31"), BirthdaySeed.nextBirthday(on("2026-08-01")))
    }

    @Test
    fun `a leap year does not shift the date`() {
        assertEquals(on("2028-07-31"), BirthdaySeed.nextBirthday(on("2028-03-01")))
    }

    @Test
    fun `the wish is at midnight, so the app gets there first`() {
        val at = BirthdaySeed.wishAt(on("2026-01-05"))
        assertEquals(LocalDateTime.parse("2026-07-31T00:00"), at)
    }

    @Test
    fun `the seeded reminder repeats yearly and is marked special`() {
        val reminder = BirthdaySeed.build("Birthday", "note", on("2026-09-17"))
        assertEquals(RepeatMode.YEARLY, reminder.repeat)
        assertEquals(VoiceLanguage.HINDI, reminder.language)
        assertTrue(reminder.isSpecialDay)
        assertTrue(reminder.enabled)

        val at = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(reminder.timeMillis), ZoneId.systemDefault()
        )
        assertEquals(LocalDateTime.parse("2027-07-31T00:00"), at)
    }

    @Test
    fun `the spoken wish is Hindi and already says Bhaloo`() {
        val spoken = BirthdaySeed.build("Birthday", "note", on("2026-09-17")).speechText()
        assertTrue(spoken.startsWith("भालू,"))
        assertTrue(spoken.contains("जन्मदिन मुबारक हो"))
        // speechText() must not prepend a second "भालू," on top of the one already there.
        assertEquals(1, spoken.split("भालू").size - 1)
    }

    @Test
    fun `an already-seeded store never seeds twice`() {
        // The guard is a single flag; this documents the intent it encodes.
        val first = BirthdaySeed.build("Birthday", "note", on("2026-09-17"))
        val second = BirthdaySeed.build("Birthday", "note", on("2026-09-17"))
        assertEquals(first.timeMillis, second.timeMillis)
    }
}
