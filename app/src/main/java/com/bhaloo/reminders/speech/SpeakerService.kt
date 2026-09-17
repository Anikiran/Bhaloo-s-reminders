package com.bhaloo.reminders.speech

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.bhaloo.reminders.BhalooApp
import com.bhaloo.reminders.data.VoiceLanguage
import com.bhaloo.reminders.notify.Notifications

/**
 * Speaks one reminder out loud and then gets out of the way.
 *
 * It runs in the foreground because the alarm that started it may have fired
 * while the phone was asleep and the screen was off — which is exactly when
 * Bhaloo most needs to hear it.
 */
class SpeakerService : Service() {

    private var tts: TextToSpeech? = null
    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null
    private var pending: Request? = null
    private var spoken = 0

    private data class Request(
        val text: String,
        val language: VoiceLanguage,
        val times: Int,
        val reminderId: Long,
        val special: Boolean,
        val delayMs: Long
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val request = intent?.getStringExtra(EXTRA_TEXT)?.takeIf { it.isNotBlank() }
        if (intent == null || request == null) {
            // Nothing to say. Still post the notification first: on Android 8+ a
            // service started with startForegroundService must call startForeground.
            startForegroundSafely("")
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundSafely(request)

        pending = Request(
            text = request,
            language = VoiceLanguage.from(intent.getStringExtra(EXTRA_LANGUAGE)),
            times = intent.getIntExtra(EXTRA_TIMES, 2).coerceIn(1, 5),
            reminderId = intent.getLongExtra(EXTRA_ID, -1L),
            special = intent.getBooleanExtra(EXTRA_SPECIAL, false),
            delayMs = intent.getLongExtra(EXTRA_DELAY_MS, 300L)
        )
        spoken = 0

        acquireWakeLock()
        // Hard stop so a stuck engine can never hold the service forever.
        handler.postDelayed(watchdog, WATCHDOG_MS)

        if (tts == null) {
            tts = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    handler.postDelayed({ beginSpeaking() }, pending?.delayMs ?: 300L)
                } else {
                    stopEverything()
                }
            }
        } else {
            handler.postDelayed({ beginSpeaking() }, pending?.delayMs ?: 300L)
        }

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

    private fun beginSpeaking() {
        val request = pending ?: return stopEverything()
        val engine = tts ?: return stopEverything()
        val store = (applicationContext as? BhalooApp)?.store

        val locale = BhalooVoice.applyLanguage(engine, request.language)
        if (locale == null) {
            stopEverything()
            return
        }
        BhalooVoice.preferOfflineVoice(engine, locale)

        engine.setSpeechRate(store?.speechRate ?: 0.95f)
        engine.setPitch(store?.speechPitch ?: 1.0f)
        runCatching {
            engine.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
        }

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                if (utteranceId == LAST_UTTERANCE) {
                    handler.post { stopEverything() }
                }
            }

            override fun onError(utteranceId: String?) {
                handler.post { stopEverything() }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                handler.post { stopEverything() }
            }
        })

        raiseAlarmVolumeIfSilent()

        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
        }

        val lines = buildLines(request)
        lines.forEachIndexed { index, line ->
            val isLast = index == lines.lastIndex
            val id = if (isLast) LAST_UTTERANCE else "bhaloo_$index"
            val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            engine.speak(line, mode, params, id)
            if (!isLast) {
                engine.playSilentUtterance(GAP_MS, TextToSpeech.QUEUE_ADD, "gap_$index")
            }
        }
        spoken = lines.size
    }

    /** A special day gets a warm opening line before the reminder itself. */
    private fun buildLines(request: Request): List<String> {
        val lines = mutableListOf<String>()
        if (request.special) {
            lines += when (request.language) {
                VoiceLanguage.HINDI -> "भालू, आज का दिन ख़ास है।"
                VoiceLanguage.ENGLISH -> "Bhaloo, today is a special day."
            }
        }
        repeat(request.times) { lines += request.text }
        return lines
    }

    /** If the alarm stream is muted the message would be silent — nudge it up a little. */
    private fun raiseAlarmVolumeIfSilent() {
        runCatching {
            val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val current = audio.getStreamVolume(AudioManager.STREAM_ALARM)
            if (current == 0) {
                audio.setStreamVolume(AudioManager.STREAM_ALARM, (max * 0.6f).toInt(), 0)
            }
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        runCatching {
            val power = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = power.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "BhalooReminders:speak"
            ).also { it.acquire(WATCHDOG_MS) }
        }
    }

    private val watchdog = Runnable { stopEverything() }

    private fun stopEverything() {
        handler.removeCallbacks(watchdog)
        runCatching { tts?.stop() }
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
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

        private const val LAST_UTTERANCE = "bhaloo_last"
        private const val GAP_MS = 900L
        private const val WATCHDOG_MS = 90_000L
    }
}
