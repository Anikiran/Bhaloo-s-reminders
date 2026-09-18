# Fix: the background is a WALL, not a floor

## What went wrong

The old prompt said "product render", "shadow **beneath** the object" and
"caustic onto **the surface**". All three of those say *floor* to an image
model, so it built a ground plane receding into the distance and stood the
button on it.

We want the opposite: a flat vertical wall facing the camera, with the button
floating slightly in front of it, casting its shadow **onto that wall**.

---

## To fix the two images you already made

Paste this into the same chat:

```
The background should be a flat vertical WALL facing the camera, not a floor.
Redo that image: no ground plane, no tabletop, no horizon, no perspective,
nothing receding into the distance. The glass pill floats a couple of
centimetres in FRONT of a flat magenta wall, parallel to it, and casts its soft
shadow and its caustic ONTO THAT WALL behind it, offset slightly down and to
the right. The wall is flat, head-on, and fills the entire frame.
```

---

## The corrected MESSAGE 1 (use this from now on)

```
A flat graphic asset render, not a product photograph.

The camera looks straight at a flat vertical wall that completely fills the
frame. The wall is solid magenta #FF00FF. There is NO floor, NO ground, NO
tabletop, NO horizon and NO perspective. Nothing recedes into the distance.

A single piece of thick polished glass floats about two centimetres IN FRONT of
this wall, parallel to it, facing the camera dead-on. It is a horizontal
rounded-rectangle pill, about 5 times wider than it is tall, with fully rounded
ends. The glass is saturated violet purple #8B5CF6, deeply coloured but still
translucent.

The glass has real thickness; you can see through its body and its curved edge
bends and distorts what is behind it. A strong blown-out white specular
highlight runs evenly ALONG the top-left edge, for the full length of the pill.
Faint rainbow chromatic dispersion where light splits at the rim.

A single soft key light comes from the top-left at 45 degrees. Because the glass
floats in front of the wall, it casts a soft transparent violet shadow ONTO THE
WALL BEHIND IT, offset slightly down and to the right — a close, tight shadow,
not a long dramatic one. A bright focused violet caustic sits on the wall inside
that shadow.

The object is centred and fills about 80% of the frame width, with generous
empty magenta wall all around it.

No text, no logo, no letters, no UI elements, no hands, no floor, no tabletop.
```

---

## For messages 2–10, add one line

Keep them short as before, but now say "wall" explicitly so it does not drift
back to a floor:

```
Exactly the same glass material, same lighting, same flat magenta WALL behind,
same floating-in-front-of-the-wall setup, same camera. Only change: <the shape
or colour>.
```

---

## Extra check when you look at each image

On top of the four checks already in the steps file:

- [ ] **No floor line.** If you can see where a "ground" meets a "back wall",
      or the magenta gets darker towards the bottom, it built a room. Redo.
- [ ] **Shadow is behind, not below.** The shadow should look painted on the
      wall just down-right of the button, not pooling under it like an object
      standing on a table.

## Why this matters for the app

In the app the button sits on a flat background, exactly like a sticker on a
wall. A shadow rendered as if cast on a floor points the wrong way once it is
placed in the UI, and the eye notices immediately even if it cannot say why.
