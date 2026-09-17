# Bhaloo's Reminders 🐻

An Android reminder app built for one person: **Bhaloo**.

It does what every reminder app does — and then it speaks. At the exact minute
you picked, the phone shows a notification *and* reads your own words out loud,
in **English or Hindi**, using the phone's text-to-speech voice. No accounts,
no ads, no internet, no analytics. One app, one person.

---

## What it does

| | |
|---|---|
| 🔔 **Notification + voice** | High-priority alarm-style notification with a custom chime, then the message is spoken aloud over the alarm channel — so it is heard even on silent-but-alarms, screen off, phone in a pocket. |
| 🗣️ **Your exact words** | Type the sentence you want said. "Bhaloo" is woven in automatically if you leave it out. Say it 1–5 times, at whatever speed and pitch sounds right. |
| 🇮🇳 **English or Hindi** | Per reminder. Hindi messages are spoken in Devanagari — `भालू, दवाई लेने का समय हो गया है।` The whole app UI is translated too. |
| 📅 **Calendar-aware** | Pick any date and time. Repeat once, daily, weekly, monthly, yearly, or on chosen weekdays (Mon/Wed/Fri…). Month-end dates clamp correctly; birthdays roll to next year. |
| 🎂 **Special days** | Mark a reminder as a special day and it gets a warm extra line before the message, plus a cake on the card. Bhaloo's birthday — 31 July — is already in the app. |
| ⏰ **Full-screen alert** | When a reminder fires, a full-screen card appears over the lock screen with **Done / Snooze / Say it again**. |
| 🔁 **Survives everything** | Reminders are re-armed after reboot, app update, time change and time-zone change. |
| 🔒 **Entirely offline** | Everything is stored on the phone. The app requests no internet permission at all. |

## The parts that make it Bhaloo's

This is not a generic app with a name pasted on:

- **Bhaloo's face is the app icon** and appears on the welcome card, the home
  screen and every full-screen alert.
- **Their birthday is already in there.** On the very first launch the app
  seeds 31 July as a yearly reminder that wishes them, in Hindi, at midnight —
  so the list is never empty and the first thing in it is the date that matters.
- **A dedication screen** — "For Bhaloo" — with a personal note in English and
  Hindi, signed with your name (set it in Settings → Signature).
- **It greets him by name**, switching through the day and in both languages:
  *Good morning, Bhaloo* / *सुप्रभात, भालू!*
- **A different line every day** underneath the greeting — *"Paani peena mat
  bhoolna, Bhaloo."*
- **Quick picks** written for him: medicine, water, call home, sleep — already
  phrased in his name in both languages.
- **His own stats**: times reminded, reminders completed, and how many days the
  two of you have been doing this.
- **A hidden message**: tap his photo five times on the About screen.
- **Bear-coloured everything** — honey and cocoa, light and dark.
- Even the notification sound is a chime generated for this app alone.

---

## Getting the APK onto Bhaloo's phone

### Easiest: grab the latest release

Every push builds the app and republishes it here:

**https://github.com/Anikiran/Bhaloo-s-reminders/releases/latest**

Download the `BhalooReminders-vX.Y.apk` file attached to it and send that to
Bhaloo (WhatsApp, Drive, cable — anything). That link always points at the
newest build, so there is never a question of which file is the right one.

On their phone: tap the APK, allow *Install unknown apps* when asked, install.
Installing over an older copy is fine — existing reminders are kept.

> The per-run artifacts under the **Actions** tab are build outputs from that
> particular commit, including old ones. Use the release link above unless you
> specifically want an older build.

### Or build it yourself

Needs Android Studio, or a JDK 17 and the Android SDK:

```bash
./gradlew assembleRelease
# app/build/outputs/apk/release/app-release.apk
```

The release build is signed with the standard debug key on purpose, so the APK
installs on any phone without you setting up a keystore. (That also means Play
Store publishing would need a real signing key — this app was never meant for
the Play Store.)

## The birthday, and changing it

31 July is seeded on first launch as a yearly, special-day reminder that speaks
in Hindi at **midnight** — the classic *12 baje* wish. Everything about it is
editable in the app (tap it on the home screen), and the defaults live in one
file, `data/BirthdaySeed.kt`:

- `BIRTHDAY` — the date.
- `WISH_AT` — change `LocalTime.MIDNIGHT` to e.g. `LocalTime.of(8, 0)` for a
  morning wish instead.
- `WISH` — the exact words spoken.

The seed runs once, guarded by a flag, so if Bhaloo deletes the birthday in the
app it stays deleted.

## Swapping the photo

Their photo is already installed. To replace it later:

```bash
tools/set_bhaloo_photo.sh ~/Pictures/new-photo.jpg
./gradlew assembleRelease
```

The script crops the photo square, replaces it on every screen, and rebuilds the
launcher icon (adaptive + legacy densities). Needs ImageMagick; if you would
rather do it by hand, see the notes at the top of the script.

## First run, on his phone

Three taps, once, and then it just works:

1. **Allow notifications** when the app asks.
2. If the home screen shows an orange **"Alarms are not exact yet"** card, tap
   it and allow exact alarms.
3. **Settings → Ignore battery optimisation.** On Xiaomi, Oppo, Vivo, Realme
   and OnePlus phones also enable **Autostart** for the app in system settings —
   otherwise Android kills it and reminders arrive late or not at all.

If Hindi comes out silent or sounds wrong, go to **Settings → Install / check
voice data** once and install the Hindi voice. After that it works offline.

## How it is put together

```
app/src/main/java/com/bhaloo/reminders/
├── BhalooApp.kt              Application: store, channels, re-arm on launch
├── data/
│   ├── Reminder.kt           The model, and the rule that adds Bhaloo's name
│   ├── RepeatSchedule.kt     Calendar maths (pure Kotlin, unit-tested)
│   └── ReminderStore.kt      JSON in SharedPreferences — no database to corrupt
├── alarm/
│   ├── ReminderScheduler.kt  The only thing that touches AlarmManager
│   ├── AlarmReceiver.kt      Fires: notify, speak, re-arm the next occurrence
│   ├── ActionReceiver.kt     Done / Snooze / Say it again
│   └── BootReceiver.kt       Re-arm after reboot, update, time change
├── notify/Notifications.kt   Channels and the alarm-style notification
├── speech/
│   ├── BhalooVoice.kt        Language selection, offline-voice preference
│   └── SpeakerService.kt     Foreground service that actually speaks
├── ui/                       Compose screens: home, editor, alert, about, settings
└── util/BhalooWords.kt       Greetings, dates, and the line of the day
```

- **Kotlin + Jetpack Compose**, Material 3, minSdk 26 (Android 8) → targetSdk 34.
- **No database, no dependency injection, no network library.** The whole
  dependency list is AndroidX Core, Lifecycle, Activity and Compose.
- `app/src/test/` holds the schedule tests — every repeat mode plus the
  awkward cases. They run on every push.

## Licence

Written for a friend. Use it, change it, no conditions attached.
