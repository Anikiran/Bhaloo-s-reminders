#!/usr/bin/env python3
"""
Render the glass UI kit with Cycles.

    blender -b -P design/blender/glass_kit.py -- --out design/renders/blender

Cycles writes real alpha via film_transparent, so the assets come out already
cut out: there is no backdrop behind the glass to transmit through it and no
chroma key to undo afterwards. That is the whole reason for rendering rather
than generating these.

Four findings from getting here, each of which cost a render to learn:

1. Blender's default AgX view transform deliberately desaturates saturated
   colour. UI assets need `Standard`, or violet arrives as dusty lavender.

2. Geometry built by converting a filled curve to a mesh renders FLAT through
   glass — no refraction, no specular — even after welding and recalculating
   normals. Cube plus bevel modifiers renders correctly. The curve route is
   avoided entirely.

3. A pill needs two bevels, not one. A single angle-limited bevel has its width
   capped by the thinnest dimension — the thickness — so the ends stay stubby.
   Bevel the four upright edges by weight to round the outline into a true
   pill, then bevel by angle for the top and bottom rim.

4. Seen face-on, a glossy top reflects whatever is BEHIND the camera. Softboxes
   placed between object and camera light it but never appear in it, which is
   why early renders had no highlight. The reflectors sit behind the lens and
   are hidden from it with visible_camera = False.

The cast shadow and caustic are deliberately NOT rendered. A caustic takes its
colour from the surface it lands on, so one baked here would be wrong against
the app's background; both are drawn in the app where that colour is known.
"""
import bpy, bmesh, math, os, sys

VIOLET = (0.48, 0.28, 0.98, 1.0)
MINT   = (0.16, 0.85, 0.76, 1.0)
PINK   = (0.98, 0.62, 0.83, 1.0)
CLEAR  = None

# name -> (half width, half height, half thickness, colour, absorption, px)
ASSETS = {
    "btn_primary":   (1.45, 0.40, 0.21, VIOLET, 9.0, (1120, 216)),
    "btn_secondary": (1.45, 0.40, 0.21, MINT,   9.0, (1120, 216)),
    "btn_clear":     (1.45, 0.38, 0.20, CLEAR,  0.0, (1120, 200)),
    "chip":          (0.80, 0.40, 0.20, CLEAR,  0.0, (480, 160)),
    "field":         (1.64, 0.36, 0.17, CLEAR,  0.0, (1312, 224)),
    "circle_48":     (0.42, 0.42, 0.22, CLEAR,  0.0, (192, 192)),
    "circle_42":     (0.40, 0.40, 0.20, CLEAR,  0.0, (168, 168)),
    "switch_track":  (0.56, 0.34, 0.16, CLEAR,  0.0, (224, 136)),
    "switch_knob":   (0.26, 0.26, 0.24, CLEAR,  0.0, (104, 104)),
}


def reset():
    for o in list(bpy.data.objects):
        bpy.data.objects.remove(o, do_unlink=True)


def setup_scene(res, samples=150):
    sc = bpy.context.scene
    sc.render.engine = "CYCLES"
    sc.cycles.device = "CPU"
    sc.cycles.samples = samples
    sc.cycles.use_denoising = True
    sc.cycles.caustics_refractive = True
    sc.cycles.caustics_reflective = True
    sc.cycles.transmission_bounces = 24
    sc.cycles.max_bounces = 32
    sc.render.film_transparent = True
    sc.render.resolution_x, sc.render.resolution_y = res
    sc.render.image_settings.media_type = "IMAGE"
    sc.render.image_settings.file_format = "PNG"
    sc.render.image_settings.color_mode = "RGBA"
    sc.view_settings.view_transform = "Standard"      # finding 1
    sc.view_settings.look = "None"

    w = bpy.data.worlds.new("w"); sc.world = w; w.use_nodes = True
    w.node_tree.nodes["Background"].inputs[0].default_value = (0.92, 0.94, 1.0, 1)
    w.node_tree.nodes["Background"].inputs[1].default_value = 0.95
    return sc


def pill(name, hw, hh, ht):
    """Cube plus two bevels — see finding 2 and finding 3."""
    bpy.ops.mesh.primitive_cube_add(size=2, location=(0, 0, 0))
    ob = bpy.context.active_object; ob.name = name
    ob.scale = (hw, hh, ht)
    bpy.ops.object.transform_apply(scale=True)

    me = ob.data
    bm = bmesh.new(); bm.from_mesh(me)
    lay = (bm.edges.layers.float.get("bevel_weight_edge")
           or bm.edges.layers.float.new("bevel_weight_edge"))
    for e in bm.edges:
        a, b = e.verts
        if abs(a.co.x - b.co.x) < 1e-5 and abs(a.co.y - b.co.y) < 1e-5:
            e[lay] = 1.0                      # the four upright edges
    bm.to_mesh(me); bm.free()

    b1 = ob.modifiers.new("outline", "BEVEL")
    b1.limit_method, b1.width, b1.segments = "WEIGHT", hh * 0.995, 32
    b2 = ob.modifiers.new("rim", "BEVEL")
    b2.limit_method, b2.angle_limit = "ANGLE", math.radians(30)
    # Kept well under the half-thickness: at ~0.9 the top and bottom roundings
    # meet at the equator and leave a ridge that reads as a dark seam.
    b2.width, b2.segments = ht * 0.58, 20
    ob.data.shade_smooth()
    ob.select_set(False)
    return ob


def glass(name, rgb, density):
    m = bpy.data.materials.new(name); m.use_nodes = True
    nt = m.node_tree; b = nt.nodes["Principled BSDF"]
    def s(k, v):
        if k in b.inputs: b.inputs[k].default_value = v
    s("Base Color", (1, 1, 1, 1)); s("Roughness", 0.0); s("IOR", 1.50)
    s("Transmission Weight", 1.0); s("Coat Weight", 1.0); s("Coat Roughness", 0.0)
    if rgb is not None:
        vol = nt.nodes.new("ShaderNodeVolumeAbsorption")
        vol.inputs["Color"].default_value = rgb
        vol.inputs["Density"].default_value = density
        nt.links.new(vol.outputs["Volume"], nt.nodes["Material Output"].inputs["Volume"])
    return m


def reflector(name, loc, rot, sx, sy, strength):
    """Behind the lens and hidden from it — see finding 4."""
    bpy.ops.mesh.primitive_plane_add(size=2, location=loc, rotation=rot)
    p = bpy.context.active_object; p.name = name
    p.scale = (sx, sy, 1); bpy.ops.object.transform_apply(scale=True)
    em = bpy.data.materials.new(name + "_m"); em.use_nodes = True
    e = em.node_tree.nodes.new("ShaderNodeEmission")
    e.inputs[0].default_value = (1, 1, 1, 1); e.inputs[1].default_value = strength
    em.node_tree.links.new(e.outputs[0],
                           em.node_tree.nodes["Material Output"].inputs["Surface"])
    p.data.materials.append(em)
    p.visible_camera = False
    p.visible_shadow = False
    p.select_set(False)


def studio(ortho_scale):
    reflector("key",    (0.0,  3.2, 6.0), (math.radians(36), 0, 0), 4.0, 1.25, 16)
    reflector("bottom", (0.2, -3.2, 5.0), (math.radians(-32), 0, 0), 3.6, 0.55, 11)
    reflector("side",   (-3.4, 0.0, 5.0), (0, math.radians(-38), 0), 1.0, 2.60, 13)
    cam = bpy.data.cameras.new("cam"); cam.type = "ORTHO"
    cam.ortho_scale = ortho_scale
    ob = bpy.data.objects.new("cam", cam)
    bpy.context.scene.collection.objects.link(ob)
    ob.location = (0, 0, 9); ob.rotation_euler = (0, 0, 0)
    bpy.context.scene.camera = ob


def render_asset(name, out_dir):
    hw, hh, ht, rgb, density, res = ASSETS[name]
    reset()
    sc = setup_scene(res)
    ob = pill(name, hw, hh, ht)
    ob.data.materials.append(glass(name + "_m", rgb, density))
    studio(ortho_scale=hw * 2.28)
    sc.render.filepath = os.path.join(out_dir, name + ".png")
    bpy.ops.render.render(write_still=True)
    return sc.render.filepath


if __name__ == "__main__":
    argv = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    out = "design/renders/blender"
    if "--out" in argv:
        out = argv[argv.index("--out") + 1]
    os.makedirs(out, exist_ok=True)
    only = argv[argv.index("--only") + 1].split(",") if "--only" in argv else ASSETS
    for n in only:
        print("rendering", n, "->", render_asset(n, out))
