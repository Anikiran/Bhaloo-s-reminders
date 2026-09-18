package com.bhaloo.reminders.speech

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import com.bhaloo.reminders.BhalooApp
import com.bhaloo.reminders.data.NameVoice
import com.bhaloo.reminders.data.VoiceLanguage
import com.bhaloo.reminders.notify.Notifications

/**
 * Speaks one reminder out loud and then gets out of the way.
 *
 * It runs in the foreground because the alarm that started it may have fired
 * while the phone was asleep and the screen was off — which is exactly when
 * Bhaloo most needs to hear it.
 *
 * The engine itself lives in [BhalooSpeaker], warmed when the process starts,
 * so this no longer pays a one-to-three-second engine bind on every reminder.
 */
class SpeakerService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val text = intent?.getStringExtra(EXTRA_TEXT)?.takeIf { it.isNotBlank() }
        if (intent == null || text == null) {
            // Nothing to say. Still post the notification first: on Android 8+ a
            // service started with startForegroundService must call startForeground.
            startForegroundSafely("")
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundSafely(text)

        val language = VoiceLanguage.from(intent.getStringExtra(EXTRA_LANGUAGE))
        val times = intent.getIntExtra(EXTRA_TIMES, 2)
        val special = intent.getBooleanExtra(EXTRA_SPECIAL, false)
        val delayMs = intent.getLongExtra(EXTRA_DELAY_MS, 0L)
        val store = (applicationContext as? BhalooApp)?.store

        acquireWakeLock()
        handler.postDelayed(watchdog, WATCHDOG_MS)
        BhalooSpeaker.nudgeAlarmVolume(this)

        val start = {
            BhalooSpeaker.speak(
                context = this,
                text = text,
                language = language,
                times = times,
                special = special,
                spokenNameEnglish = store?.spokenNameEnglish ?: NameVoice.DEFAULT_SPOKEN_EN,
                rate = store?.speechRate ?: 0.95f,
                pitch = store?.speechPitch ?: 1.0f,
                onFinished = { stopEverything() }
            )
        }
        if (delayMs > 0L) handler.postDelayed(start, delayMs) else start()

        return START_NOT_STICKY
    }

    private fun startForegroundSafely(text: String) {
        val notification = Notifications.buildSpeakingNotification(this, text)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    Notifications.SPEAKING_NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(Notifications.SPEAKING_NOTIFICATION_ID, notification)
            }
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        runCatching {
            val power = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = power.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "BhalooReminders:speak"
            ).also { it.acquire(WATCHDOG_MS) }
        }
    }

    private val watchdog = Runnable { stopEverything() }

    private fun stopEverything() {
        handler.removeCallbacks(watchdog)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        // The engine is shared and stays warm for the next reminder; only the
        // speech in flight is cancelled.
        BhalooSpeaker.stop()
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TEXT = "text"
        const val EXTRA_LANGUAGE = "language"
        const val EXTRA_TIMES = "times"
        const val EXTRA_ID = "id"
        const val EXTRA_SPECIAL = "special"
        const val EXTRA_DELAY_MS = "delay"

        private const val WATCHDOG_MS = 90_000L
    }
}
