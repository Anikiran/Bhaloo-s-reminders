#!/usr/bin/env bash
#
# Put Bhaloo's face on the app.
#
# Their photo is already installed — run this only to replace it with a
# different one.
#
#   tools/set_bhaloo_photo.sh ~/Pictures/bhaloo.jpg
#
# It does two things:
#   1. Replaces the placeholder portrait shown on the welcome, home and
#      alert screens with the real photo.
#   2. Rebuilds the launcher icon from that photo, so the app on the
#      home screen is literally Bhaloo.
#
# Needs ImageMagick ("magick" or "convert"). On Ubuntu: sudo apt install imagemagick
set -euo pipefail

PHOTO="${1:-}"
if [[ -z "$PHOTO" || ! -f "$PHOTO" ]]; then
    echo "usage: $0 /path/to/bhaloo.jpg" >&2
    exit 1
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RES="$ROOT/app/src/main/res"

if command -v magick >/dev/null 2>&1; then
    IM="magick"
elif command -v convert >/dev/null 2>&1; then
    IM="convert"
else
    echo "ImageMagick not found. Install it, or drop the photo in by hand:" >&2
    echo "  - save a square PNG as $RES/drawable-nodpi/bhaloo_portrait.png" >&2
    echo "  - delete $RES/drawable/bhaloo_portrait.xml" >&2
    exit 1
fi

echo "==> In-app portrait"
mkdir -p "$RES/drawable-nodpi"
# Square centre-crop, 512px: sharp on every screen, small in the APK.
$IM "$PHOTO" -auto-orient -resize 512x512^ -gravity center -extent 512x512 \
    "$RES/drawable-nodpi/bhaloo_portrait.png"
rm -f "$RES/drawable/bhaloo_portrait.xml"

echo "==> Launcher icon"
# The adaptive icon is masked and can be zoomed by the launcher, so the face
# must sit inside the middle 66% of the canvas.
mkdir -p "$RES/mipmap-mdpi" "$RES/mipmap-hdpi" "$RES/mipmap-xhdpi" \
         "$RES/mipmap-xxhdpi" "$RES/mipmap-xxxhdpi" "$RES/drawable-nodpi"

$IM "$PHOTO" -auto-orient -resize 720x720^ -gravity center -extent 720x720 \
    -resize 66% -gravity center -background none -extent 1080x1080 \
    "$RES/drawable-nodpi/ic_launcher_photo.png"

cat > "$RES/mipmap-anydpi-v26/ic_launcher.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_photo" />
    <monochrome android:drawable="@drawable/ic_stat_bhaloo" />
</adaptive-icon>
XML
cp "$RES/mipmap-anydpi-v26/ic_launcher.xml" "$RES/mipmap-anydpi-v26/ic_launcher_round.xml"

# Legacy round/square icons for launchers that ignore adaptive icons.
for spec in "mdpi 48" "hdpi 72" "xhdpi 96" "xxhdpi 144" "xxxhdpi 192"; do
    set -- $spec
    $IM "$PHOTO" -auto-orient -resize "$2x$2^" -gravity center -extent "$2x$2" \
        "$RES/mipmap-$1/ic_launcher.png"
    cp "$RES/mipmap-$1/ic_launcher.png" "$RES/mipmap-$1/ic_launcher_round.png"
done

echo
echo "Done. Bhaloo is now the app."
echo "Rebuild with:  ./gradlew assembleRelease"
