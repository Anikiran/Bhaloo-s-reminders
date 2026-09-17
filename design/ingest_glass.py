#!/usr/bin/env python3
"""
Turn rendered glass images into Android 9-patch drawables.

    python3 design/ingest_glass.py design/renders/

For each render it keys out the magenta backdrop, crops to what is left,
resizes to the exact size the app expects, and — for the stretchable ones —
writes a .9.png with the corners locked and only the middle allowed to repeat.

The magenta key is what makes this reliable: #FF00FF appears nowhere in glass,
so the cut is exact even along a soft caustic, which a white or checkerboard
background could never give.
"""
import os
import sys
from PIL import Image

# name -> (width dp, height dp, corner radius dp, stretchable)
ASSETS = {
    "btn_primary":   (280, 54, 27, True),
    "btn_secondary": (280, 54, 27, True),
    "btn_clear":     (280, 50, 25, True),
    "chip":          (120, 40, 20, True),
    "field":         (328, 56, 28, True),
    "circle_48":     (48, 48, 24, False),
    "circle_42":     (42, 42, 21, False),
    "card":          (328, 180, 30, True),
    "switch_track":  (56, 34, 17, False),
    "switch_knob":   (26, 26, 13, False),
}

# Android density buckets. Assets are authored at 4x and downsampled.
DENSITIES = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}

KEY = (255, 0, 255)
TOLERANCE = 70


def key_out(img: Image.Image) -> Image.Image:
    """Replace the magenta backdrop with alpha, feathering near-matches."""
    img = img.convert("RGBA")
    px = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            # Distance from the key, weighted so magenta's green dip dominates.
            dist = abs(r - KEY[0]) + abs(g - KEY[1]) + abs(b - KEY[2])
            if dist < TOLERANCE:
                px[x, y] = (r, g, b, 0)
            elif dist < TOLERANCE * 2.4:
                # Soft edge: partial alpha, and pull the magenta spill out.
                frac = (dist - TOLERANCE) / (TOLERANCE * 1.4)
                px[x, y] = (r, min(255, int(g * 1.05)), b, int(a * frac))
    return img


def autocrop(img: Image.Image) -> Image.Image:
    bbox = img.split()[-1].getbbox()
    return img.crop(bbox) if bbox else img


def write_ninepatch(img: Image.Image, radius_px: int, path: str) -> None:
    """
    A .9.png carries its stretch markers in a 1px border of black pixels.
    Corners are locked by marking only the middle of each axis.
    """
    w, h = img.size
    out = Image.new("RGBA", (w + 2, h + 2), (0, 0, 0, 0))
    out.paste(img, (1, 1))
    d = out.load()
    inset = min(radius_px + 2, max(1, w // 2 - 1))
    vinset = min(radius_px + 2, max(1, h // 2 - 1))

    # Top and left edges: which pixels may stretch.
    for x in range(1 + inset, 1 + w - inset):
        d[x, 0] = (0, 0, 0, 255)
    for y in range(1 + vinset, 1 + h - vinset):
        d[0, y] = (0, 0, 0, 255)
    # Bottom and right edges: where content is allowed to sit (padding box).
    for x in range(1 + inset, 1 + w - inset):
        d[x, h + 1] = (0, 0, 0, 255)
    for y in range(1 + vinset, 1 + h - vinset):
        d[w + 1, y] = (0, 0, 0, 255)
    out.save(path)


def main(src_dir: str) -> int:
    if not os.path.isdir(src_dir):
        print(f"No such directory: {src_dir}", file=sys.stderr)
        return 1

    res = "app/src/main/res"
    done, missing = [], []

    for name, (wdp, hdp, rdp, stretch) in ASSETS.items():
        src = None
        for ext in (".png", ".jpg", ".jpeg", ".webp"):
            cand = os.path.join(src_dir, name + ext)
            if os.path.exists(cand):
                src = cand
                break
        if src is None:
            missing.append(name)
            continue

        cut = autocrop(key_out(Image.open(src)))

        for bucket, mult in DENSITIES.items():
            w, h = max(1, round(wdp * mult)), max(1, round(hdp * mult))
            scaled = cut.resize((w, h), Image.LANCZOS)
            folder = os.path.join(res, f"drawable-{bucket}")
            os.makedirs(folder, exist_ok=True)
            if stretch:
                write_ninepatch(scaled, round(rdp * mult),
                                os.path.join(folder, f"{name}.9.png"))
            else:
                scaled.save(os.path.join(folder, f"{name}.png"))
        done.append(name)

    for n in done:
        print(f"  ok       {n}")
    for n in missing:
        print(f"  MISSING  {n}")
    print(f"\n{len(done)}/{len(ASSETS)} assets ingested into {res}/drawable-*/")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else "design/renders"))
