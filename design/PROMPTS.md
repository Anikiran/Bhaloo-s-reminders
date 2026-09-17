# Glass asset pack — what to generate, and how

Ten renders. Four of them do most of the work; if you only do four, do
**btn_primary, btn_secondary, card, field** and the app already changes
character completely.

---

## Read this first — five rules that decide whether the assets are usable

**1. Straight-on, orthographic. No perspective, no tilt, no angle.**
The button must face the camera dead-on, as if scanned. Any perspective and it
cannot be sliced for stretching, and it will look wrong beside the others.

**2. Flat magenta `#FF00FF` background. Nothing else in frame.**
Image models will not give you clean transparency. Magenta appears nowhere in
glass, so I can cut it out perfectly. Do not ask for "transparent background"
or a checkerboard — ask for solid magenta.

**3. The shadow and the caustic must be INSIDE the frame.**
Below and to the right of the shape, since the light is top-left. Leave room —
tell it "generous empty margin around the object". If the caustic is clipped at
the frame edge, the asset is unusable.

**4. The middle must be laterally uniform.**
For anything pill-shaped, the bright highlight runs ALONG the top edge, left to
right, evenly. No single glare blob in the centre, no logo, no mark. The middle
column gets repeated when the button stretches, so anything localised there
smears into a streak.

**5. Light from the top-left at 45°, in every single asset.**
This is what makes ten separate renders look like one set. Say it in every
prompt. If one asset is lit from the right, it will look pasted on.

---

## Framing: generate square, I'll crop

Image models only output fixed ratios (1:1, 16:9…), and a 280×54 button is
about 5:1 — no model offers that. So:

> Generate **1:1 (square)** with the object **centred**, filling roughly
> **80% of the width**, floating on magenta.

I crop to the object's bounding box and resize to exact pixels. You do not need
to hit the dimensions — that is my job. You only need to get the **look** right.

---

## The base style block — paste this into every prompt

> Photorealistic 3D product render of a single piece of thick polished glass,
> octane render quality, studio lighting. The glass has real thickness and you
> can see through its body; its curved edge bends and distorts what is behind
> it. A strong blown-out specular highlight runs along the top-left edge where
> the light strikes it. Faint rainbow chromatic dispersion where the light
> splits at the rim. Below the object, a soft transparent coloured shadow and a
> bright focused caustic — light concentrated through the glass onto the
> surface. Key light from the top-left at 45 degrees. Shot perfectly straight
> on, orthographic, no perspective, no tilt. Centred on a flat solid magenta
> #FF00FF background, generous empty margin all around. No text, no logo, no
> letters, no UI, no hands, no reflections of a room.

---

## The ten assets

### 1. `btn_primary` — violet pill  ⭐ do this one
Base style block, plus:
> The glass object is a horizontal rounded-rectangle pill, roughly 5 times wider
> than it is tall, with fully rounded ends. The glass is **saturated violet
> purple (#8B5CF6)**, deeply coloured but still translucent. The top edge
> highlight runs evenly along the whole length. The caustic beneath it glows
> violet.

### 2. `btn_secondary` — mint pill  ⭐
Same as 1, but:
> The glass is **bright mint turquoise (#3ED6C4)**. The caustic beneath glows
> turquoise.

### 3. `btn_clear` — clear pill
Same shape, but:
> The glass is **colourless and clear**, like a thick water droplet, picking up
> only faint cool tints from its surroundings. Edge highlight and caustic are
> white.

### 4. `chip` — small pill
> …a small horizontal pill, roughly 3 times wider than tall, fully rounded ends,
> **clear colourless glass**, thick and chunky for its size.

### 5. `field` — input trough  ⭐
> …a horizontal rounded-rectangle pill, about 6 times wider than tall. **Clear
> glass, but concave — a trough pressed INTO the surface** rather than a raised
> button. The highlight sits along the INNER top edge and the shadow falls
> inside the recess. It should read as a groove you could type into.

### 6. `circle_48` — round button
> …a **perfect circle** of thick clear glass, like a polished lens or a glass
> bead. The centre is slightly domed. Bright specular point at the top-left,
> circular caustic beneath.

### 7. `circle_42` — small round button
Same as 6, smaller and slightly flatter.

### 8. `card` — hero pane  ⭐
> …a large **rounded-rectangle glass panel**, slightly wider than tall, with
> softly rounded corners. Frosted, iridescent glass shading across its face from
> **soft pink through lavender to mint to peach** — a pastel rainbow, like an
> oil-slick or dichroic film. The edges are thick and clear with strong rainbow
> dispersion. A wide, soft, colourful caustic spreads beneath it.

### 9. `switch_track` — toggle track
> …a small horizontal pill, about 1.6 times wider than tall, **clear glass,
> concave** — a recessed groove. Soft inner shadow.

### 10. `switch_knob` — toggle bead
> …a **perfect sphere-like glass bead**, brilliant white and highly polished,
> like a pearl of clear glass. Strong point highlight top-left, tight round
> caustic beneath.

---

## Sending them back

Any format (PNG or JPG — the magenta survives either), any size, named so I can
tell them apart:

```
btn_primary.png   btn_secondary.png   btn_clear.png   chip.png   field.png
circle_48.png     circle_42.png       card.png        switch_track.png
switch_knob.png
```

Generate 2–3 variants of each and send your favourite. If one looks off beside
the others, say which and we redo just that one.

## What I do with them

1. Key out the magenta → true alpha.
2. Auto-crop to the object, resize to the exact pixel sizes in `design/specs/`.
3. Slice the stretchable ones into Android **9-patch** (`.9.png`) so corners
   stay sharp at any width.
4. Swap the drawn-glass components for the rendered ones, keeping all the
   layout, text and behaviour exactly as it is.
5. Push, CI builds, you install and we tune.
