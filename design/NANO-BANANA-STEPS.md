# Nano Banana Pro — exact steps

## ⚠️ Read this — two extra lines are needed in every prompt

The first four renders were beautiful but could not be used, for one physical
reason: **the glass was see-through, so it transmitted the magenta wall**. Once
cut out and placed in the app, the lower half of each button was magenta —
because on a magenta wall, transparent violet glass really *is* partly magenta.
That colour is transmitted light, not background, so no amount of cutting out
can remove it.

Two changes fix it, and both are one line each:

1. **The button glass must be SOLID and self-coloured**, like a polished
   gemstone — you cannot see the wall through it. (Look at the reference image:
   its violet button is solidly violet. You never see through it. Only the big
   panel is see-through.)
2. **No shadow and no caustic cast on the wall.** A caustic takes its colour
   from the surface it lands on, so a magenta one is wrong everywhere else. I
   draw the shadow and the coloured glow in the app instead, where the real
   background colour is known — so you lose nothing, and they come out the
   right colour.

Everything else you already had — the thickness, the bright rim, the specular
streak along the top edge, the rainbow at the edge — stays exactly as it was.
The material was right; only these two things need changing.

---

## Before you start

1. Open the Gemini app (gemini.google.com) and start a **new chat**.
2. Pick the **Pro / Thinking** model (that is Nano Banana Pro).
3. When the image options appear, set:
   - **Aspect ratio: 1:1 (square)**
   - **Resolution: the highest offered** (2K or 4K)
4. Do everything below **in this one chat**. That matters: it copies the glass
   and the lighting forward from the first image, which is what makes all ten
   look like one set instead of ten strangers.

Download each result as **PNG** and name it exactly as shown.

---

## Do these FOUR first, send them to me, then do the rest

Four assets carry most of the look. Do 1–4, send them, I will wire them in and
build an APK so you can see it on your phone. Only then do 5–10. That way if
something about the material is wrong, we find out after four images and not
after ten.

---

## MESSAGE 1 → save as `btn_primary.png`

Paste this whole thing:

```
A flat graphic asset render, not a product photograph.

The camera looks straight at a flat vertical wall that completely fills the
frame. The wall is solid magenta #FF00FF. There is NO floor, NO ground, NO
tabletop, NO horizon and NO perspective. Nothing recedes into the distance.

Floating in front of this wall, facing the camera dead-on, is a horizontal
rounded-rectangle pill about 4 times wider than it is tall, with fully rounded
ends. It is made of thick SOLID violet purple glass #8B5CF6 — a polished
gemstone, richly and evenly coloured all the way through. You CANNOT see the
wall through it; no magenta shows anywhere inside the button.

The glass is chunky and you can clearly see the thickness of its rounded edge.
A strong blown-out white specular highlight runs evenly ALONG the top-left
edge, the full length of the pill. A second softer highlight catches the bottom
edge. Faint rainbow chromatic dispersion where light splits along the rim.

A single soft key light from the top-left at 45 degrees.

IMPORTANT: the object casts NO shadow and NO caustic on the wall. The wall
behind and around it is perfectly clean, flat, even magenta. The object simply
floats alone against it.

The object is centred and fills about 80% of the frame width, with clean empty
magenta wall all around it.

No text, no logo, no letters, no UI elements, no hands, no floor, no tabletop,
no shadow, no glow on the wall.
```

## MESSAGE 2 → save as `btn_secondary.png`

```
Exactly the same setup: same solid self-coloured glass, same lighting, same
camera, same clean flat magenta wall with NO shadow and NO caustic on it, same
size and shape. Only change the colour: make the glass bright
mint turquoise #3ED6C4, and the caustic beneath it glows turquoise.
```

## MESSAGE 3 → save as `field.png`

```
Same solid self-coloured glass, same lighting, same camera, same clean flat magenta wall with NO shadow and NO caustic cast on it. Now make a
horizontal rounded pill about 6 times wider than tall, in clear colourless
glass, and make it CONCAVE — a trough pressed INTO the surface, not a raised
button. The highlight sits along the inner top edge and the shadow falls inside
the recess. It should look like a smooth groove you could type into.
```

## MESSAGE 4 → save as `card.png`

```
Same glass material and lighting, same clean flat magenta wall with NO shadow and NO caustic cast on it. Now make a large
rounded-rectangle glass panel, slightly wider than tall, with softly rounded
corners. Unlike the buttons this one IS frosted and milky, but it still shows
none of the magenta wall — its face is its own iridescent colour, shading from
soft pink through lavender to mint to peach — a pastel rainbow like an oil slick or dichroic
film. The edges are thick and clear with strong rainbow dispersion. A wide,
soft, colourful caustic spreads beneath it.
```

### → Stop here and send me these four.

---

## The remaining six (after we have checked the first four)

## MESSAGE 5 → `btn_clear.png`
```
Same glass, same lighting, same camera, same magenta background, same pill
shape as the first violet button. Now make the glass completely colourless and
clear, like a thick water droplet. Edge highlight and caustic are white.
```

## MESSAGE 6 → `chip.png`
```
Same solid self-coloured glass, same lighting, same camera, same clean flat magenta wall with NO shadow and NO caustic cast on it. Now a small
pill only about 3 times wider than tall, clear colourless glass, thick and
chunky for its size.
```

## MESSAGE 7 → `circle_48.png`
```
Same solid self-coloured glass, same lighting, same camera, same clean flat magenta wall with NO shadow and NO caustic cast on it. Now a perfect
circle of thick clear glass, like a polished lens or glass bead, slightly domed
in the centre. Bright specular point at the top-left, round caustic beneath.
```

## MESSAGE 8 → `circle_42.png`
```
Same as the last one, a perfect circle of clear glass, but slightly smaller and
a little flatter.
```

## MESSAGE 9 → `switch_track.png`
```
Same solid self-coloured glass, same lighting, same camera, same clean flat magenta wall with NO shadow and NO caustic cast on it. Now a small
horizontal pill only about 1.6 times wider than tall, clear glass, CONCAVE — a
recessed groove with a soft inner shadow.
```

## MESSAGE 10 → `switch_knob.png`
```
Same solid self-coloured glass, same lighting, same camera, same clean flat magenta wall with NO shadow and NO caustic cast on it. Now a small
glass bead like a polished sphere, brilliant white and highly polished, like a
pearl of clear glass. Strong point highlight top-left, tight round caustic
beneath.
```

---

## Check each image before you accept it

Four quick looks. If any fails, just say "try again" in the chat.

- [ ] **Straight on?** The pill must look face-on, not tilted or in perspective.
- [ ] **No floor!** If you can see a ground plane, a horizon, or the magenta
      getting darker towards the bottom, it built a room instead of a wall. Redo.
- [ ] **No magenta anywhere inside the button.** This is the important one. If
      you can see the wall through the glass, or the lower half is pinkish, it
      is unusable — ask for "solid opaque coloured glass, you cannot see through
      it at all".
- [ ] **Clean wall.** No shadow, no glow, no caustic painted on the magenta
      around the object. I add those in the app.
- [ ] **Magenta all around?** Solid pink-purple background, nothing else in frame.
- [ ] **Shadow fully visible?** Not cut off at the bottom or right edge.
- [ ] **Highlight along the edge, not a blob in the middle?** A bright spot in
      the centre will smear when the button stretches.

Do not worry about: exact size, exact proportions, the image being square.
I fix all of that.

---

## If the images start drifting apart

After several messages the chat sometimes forgets the material and the glass
starts looking different. If image 7 looks unlike image 1: start a **new chat**
and paste the full MESSAGE 1 text again, then continue from where you were.

## Sending them to me

Just attach the PNGs here with their names. Two or three tries of the same
asset is fine — send the one you like best.
