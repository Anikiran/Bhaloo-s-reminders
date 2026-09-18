#!/usr/bin/env python3
"""
Pull a clean RGBA glass asset out of a render shot against a coloured backdrop.

A plain chroma key fails on these renders for four reasons, so this does
something more careful:

1. The backdrop is a *gradient*, not a flat colour, so a fixed key colour
   misses it. The backdrop is estimated per-pixel by fitting a smooth quadratic
   to the border of the frame.

2. Glass is transparent, so the glow, the caustic and the soft edges need
   *partial* alpha. A hard threshold turns them into jagged cut-outs.

3. Where a pixel is partly transparent, its colour is contaminated by the
   backdrop behind it. Since both the backdrop and the coverage are known, the
   object's true colour is recovered by unpremultiplying rather than by
   guessing at a despill.

4. The shadow and the caustic are not objects — they are the backdrop being
   darkened and brightened. They have no colour of their own to extract, so a
   brightness-based matte hands back a slab of raw backdrop colour. Matching on
   *chromaticity* instead separates them cleanly: a shadow keeps the backdrop's
   hue and only changes in brightness, while the glass has a hue of its own.
   The shadow and glow are therefore dropped here and redrawn in the app, where
   the real background colour is known — a caustic's colour depends on the
   surface it lands on, so a magenta-tinted one would be wrong anywhere else.

It also drops every disconnected speck, which is what removes the generator's
little sparkle watermark without touching the artwork.
"""
import numpy as np
from PIL import Image
from scipy import ndimage


def _fit_backdrop(rgb: np.ndarray, border_frac: float = 0.045) -> np.ndarray:
    """Least-squares quadratic through the frame's border pixels, per channel."""
    h, w, _ = rgb.shape
    bw, bh = max(2, int(w * border_frac)), max(2, int(h * border_frac))

    mask = np.zeros((h, w), bool)
    mask[:bh, :] = mask[-bh:, :] = True
    mask[:, :bw] = mask[:, -bw:] = True

    ys, xs = np.nonzero(mask)
    xn, yn = xs / w, ys / h
    A = np.stack([np.ones_like(xn), xn, yn, xn * xn, yn * yn, xn * yn], 1)

    gy, gx = np.mgrid[0:h, 0:w]
    gxn, gyn = gx / w, gy / h
    G = np.stack([np.ones_like(gxn), gxn, gyn, gxn * gxn, gyn * gyn, gxn * gyn], -1)

    out = np.empty_like(rgb)
    for c in range(3):
        coef, *_ = np.linalg.lstsq(A, rgb[ys, xs, c], rcond=None)
        out[..., c] = G @ coef
    return out


def extract(path: str, feather: float = 1.2) -> Image.Image:
    src = Image.open(path).convert("RGB")
    rgb = np.asarray(src, np.float32) / 255.0
    bg = _fit_backdrop(rgb)

    # Chromaticity: colour with brightness divided out. A shadow or a glow is
    # the backdrop scaled up or down, so it sits at the same chromaticity and
    # falls away here; the glass, being genuinely another colour, does not.
    def chroma(x):
        s = x.sum(-1, keepdims=True) + 1e-5
        return x / s

    dist = np.linalg.norm(chroma(rgb) - chroma(bg), axis=-1) * 2.2

    # Near-white specular survives too: it is far from magenta in chromaticity
    # but can be close in a plain difference, so it is folded in explicitly.
    lum = rgb.mean(-1)
    bglum = bg.mean(-1)
    blown = np.clip((lum - np.maximum(bglum, 0.55)) / 0.35, 0.0, 1.0)
    dist = np.maximum(dist, blown)

    # Thresholds from the backdrop's own noise, so no magic numbers.
    h, w = dist.shape
    bh, bw = max(2, int(h * 0.045)), max(2, int(w * 0.045))
    border = np.concatenate([dist[:bh].ravel(), dist[-bh:].ravel(),
                             dist[:, :bw].ravel(), dist[:, -bw:].ravel()])
    noise = float(np.percentile(border, 99.5))
    lo, hi = noise * 1.6 + 0.012, noise * 1.6 + 0.085

    alpha = np.clip((dist - lo) / max(hi - lo, 1e-6), 0.0, 1.0)
    alpha = alpha * alpha * (3.0 - 2.0 * alpha)          # smoothstep
    alpha = ndimage.gaussian_filter(alpha, feather)

    # Keep only the artwork: every other blob is a watermark speck or noise.
    lab, n = ndimage.label(alpha > 0.12)
    if n > 1:
        sizes = ndimage.sum(np.ones_like(lab), lab, range(1, n + 1))
        keep = int(np.argmax(sizes)) + 1
        alpha = np.where(lab == keep, alpha, 0.0)
    elif n == 0:
        raise SystemExit(f"{path}: nothing found against the backdrop")

    # Recover the object's own colour from behind the backdrop it was shot on.
    a = alpha[..., None]
    safe = np.maximum(a, 0.18)
    obj = (rgb - (1.0 - a) * bg) / safe
    obj = np.clip(obj, 0.0, 1.0)

    out = np.concatenate([obj, alpha[..., None]], -1)
    return Image.fromarray((out * 255).astype(np.uint8), "RGBA")


def autocrop(img: Image.Image, keep: float = 0.02) -> Image.Image:
    a = np.asarray(img)[..., 3]
    ys, xs = np.nonzero(a > int(keep * 255))
    if len(xs) == 0:
        return img
    return img.crop((xs.min(), ys.min(), xs.max() + 1, ys.max() + 1))


if __name__ == "__main__":
    import sys, os
    for p in sys.argv[1:]:
        out = autocrop(extract(p))
        dst = os.path.splitext(p)[0] + "_cut.png"
        out.save(dst)
        print(f"{os.path.basename(p)} -> {os.path.basename(dst)}  {out.size}")


def fit_silhouette(img: Image.Image, corner: str = "pill") -> Image.Image:
    """
    Clip the cut-out to the component's true outline.

    The cast shadow and caustic sit on the backdrop, share its hue, and trail
    off to one side, so they survive a hue matte as ragged residue. The parts
    that matter are a rounded rectangle, so the silhouette is measured and the
    alpha clipped to it — which removes the residue regardless of its colour.

    The extent is taken from column and row coverage rather than a bounding
    box: the residue is faint and narrow, so it never reaches the coverage of
    the body itself and drops out of the measurement.
    """
    a = np.asarray(img)[..., 3].astype(np.float32) / 255.0
    core = ndimage.binary_fill_holes(a > 0.5)

    lab, n = ndimage.label(core)
    if n > 1:
        sizes = ndimage.sum(np.ones_like(lab), lab, range(1, n + 1))
        core = lab == (int(np.argmax(sizes)) + 1)
    if not core.any():
        return img

    cols, rows = core.sum(0), core.sum(1)
    xs = np.nonzero(cols > cols.max() * 0.55)[0]
    ys = np.nonzero(rows > rows.max() * 0.55)[0]
    if len(xs) < 2 or len(ys) < 2:
        return img
    x0, x1, y0, y1 = int(xs[0]), int(xs[-1]), int(ys[0]), int(ys[-1])

    w, h = x1 - x0 + 1, y1 - y0 + 1
    radius = h / 2.0 if corner == "pill" else min(w, h) * 0.16

    # Antialiased rounded-rectangle mask by distance to the inner rectangle.
    yy, xx = np.mgrid[0:a.shape[0], 0:a.shape[1]].astype(np.float32)
    dx = np.maximum(np.maximum(x0 + radius - xx, xx - (x1 - radius)), 0.0)
    dy = np.maximum(np.maximum(y0 + radius - yy, yy - (y1 - radius)), 0.0)
    dist = np.hypot(dx, dy)
    mask = np.clip((radius + 0.7 - dist) / 1.4, 0.0, 1.0)

    out = np.asarray(img).copy()
    out[..., 3] = (a * mask * 255).astype(np.uint8)
    return Image.fromarray(out, "RGBA")
