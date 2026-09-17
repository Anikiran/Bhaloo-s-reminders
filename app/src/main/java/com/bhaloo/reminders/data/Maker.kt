package com.bhaloo.reminders.data

/**
 * Who made this.
 *
 * Deliberately a compile-time constant rather than a setting: the credit is
 * part of the app, not a preference. There is no code path that writes it, so
 * it cannot be edited from inside the app, cleared by wiping app data, or
 * changed by a backup restore.
 */
object Maker {
    const val NAME = "Kiran"
}
