# Making it unmistakably Bhaloo's

You asked for ideas that prove this app was made *for him*, by *you*, and not
downloaded from a store. Here is what is already built in, and what you can add
in a few minutes each.

## Already in the app

| Touch | Where he sees it |
|---|---|
| His face is the app icon | Home screen, app drawer, recents, share sheet |
| A welcome card with his photo | Every launch, two seconds |
| "For Bhaloo" dedication page | About screen, with a note in English and Hindi |
| Your name as the author | About screen — set it in **Settings → Signature** |
| Greets him by name, by the hour | *Good morning, Bhaloo* / *सुप्रभात, भालू!* |
| A different warm line every day | Under the greeting |
| His name spoken in every reminder | Added automatically if you leave it out |
| Quick picks phrased for him | Medicine, water, call home, sleep — both languages |
| His own statistics | Times reminded, marked done, days you two have been doing this |
| A hidden message | Tap his photo five times on the About screen |
| Special-day mode | A warm extra spoken line, and a cake on the card |
| A chime that exists only here | `res/raw/bhaloo_chime.wav`, generated for this app |

## Five-minute additions

Each of these is a small, contained edit:

1. **Make the hidden message actually yours.**
   `app/src/main/res/values/strings.xml` → `easter_egg_body`. Put an inside
   joke there. This is the single highest-impact line in the app.

2. **Change the dedication to your own words.**
   Same file → `dedication_body` and `dedication_body_hi`. Write what you would
   actually say to him.

3. **Seed his birthday before you send the APK.**
   Add it in the app as a **yearly** reminder with **Special day** on and a
   Hindi message — `भालू, जन्मदिन मुबारक हो!` — then he finds it already
   waiting for him on day one.

4. **Rename the app to whatever you actually call him.**
   `app_name` in `values/strings.xml` and `values-hi/strings.xml`. If you call
   him something else, use that instead of "Bhaloo" — and `Reminder.speechText()`
   in `data/Reminder.kt` is the one place the spoken name is decided.

5. **Record your own voice instead of TTS.**
   If you want him to hear *you*: drop an MP3 in `res/raw/` and play it in
   `SpeakerService` before the spoken line. Roughly ten lines of code, using
   `MediaPlayer.create(this, R.raw.your_clip)` on the alarm stream.

6. **Set the daily line to your own rotation.**
   `util/BhalooWords.kt` → `lineOfTheDay()`. Seven lines, one per day, and they
   cycle forever. Fill them with your jokes.

7. **A first-launch message only he will see.**
   The welcome card (`ui/screens/WelcomeScreen.kt`) is two seconds of pure
   dedication space — add a line like *"Ab bhoolna band, Bhaloo."*

## When you hand it over

Send him the APK with the birthday reminder already inside, and tell him to
tap his own face five times on the About screen. That one moment — his photo,
his name, his language, and a message no one else on earth has — is what makes
it obvious this was made for him and nobody else.
