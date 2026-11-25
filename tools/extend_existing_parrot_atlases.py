"""Map legacy plumages onto the six remodelled bodies and folded wings."""

import json
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
# Original breast boxes supply the painted front, flanks, mantle and belly.
# Each entry is the bottom/north/west corner followed by its integer box size.
BREASTS = {
    "cockatiel": ((-3.5, 3.7, -2.65), (7, 10, 7)),
    "green_cheek_conure": ((-3.5, 3.15, -2.60), (7, 9, 7)),
    "monk_parakeet": ((-4, 3.37, -3.1), (8, 11, 8)),
    "caique": ((-4, 3.3, -3.45), (8, 10, 8)),
    "sun_conure": ((-3.5, 3.25, -2.95), (7, 10, 7)),
    "eclectus": ((-5, 4.18, -3.9), (10, 14, 10)),
}


def box_faces(element):
    x, y = element.get("uv_offset", (0, 0))
    w, h, d = [round(b - a) for a, b in zip(element["from"], element["to"])]
    # (horizontal origin, vertical origin, size, world axis, direction)
    return (
        (x, y+d, d, h, 2, -1, 1, -1),           # east
        (x+d, y+d, w, h, 0, -1, 1, -1),         # north
        (x+d+w, y+d, d, h, 2, 1, 1, -1),        # west
        (x+2*d+w, y+d, w, h, 0, 1, 1, -1),      # south
        (x+d, y, w, d, 0, 1, 2, -1),            # top
        (x+d+w, y, w, d, 0, 1, 2, 1),           # underside
    )


def corresponding_texel(index, count, axis, direction, original, detail, source_count):
    t = (index + .5) / count
    if direction < 0:
        t = 1 - t
    low, high = detail["from"][axis], detail["to"][axis]
    position = low + t * (high - low)
    low, high = original["from"][axis], original["to"][axis]
    t = (position - low) / (high - low)
    if direction < 0:
        t = 1 - t
    return max(0, min(source_count - 1, int(t * source_count)))


def mapped_parts(species, elements):
    start, size = BREASTS[species]
    original = {"from": start, "to": [a + b for a, b in zip(start, size)]}
    by_name = {e["name"]: e for e in elements}
    for name in ("scapular_mantle", "keel_and_breast", "tapered_rump"):
        yield original, by_name[name], 1
    for side in ("left", "right"):
        # Separate source faces retain the male Eclectus's scarlet underwing.
        coverts = by_name.get(side+"_coverts") or by_name[side+"_wing_coverts"]
        yield coverts, by_name[side+"_shoulder_coverts"], 1.035
        yield by_name[side+"_primaries"], by_name[side+"_secondaries"], 1.065


def extend_atlas(image, species, project=None):
    if project is None:
        project = json.loads((ROOT / "art" / (species+".bbmodel")).read_text())
    base = image.convert("RGBA").crop((0, 0, image.width, 64))
    canvas = Image.new("RGBA", (base.width, 128))
    canvas.paste(base)
    for original, detail, factor in mapped_parts(species, project["elements"]):
        for source, target in zip(box_faces(original), box_faces(detail)):
            su, sv, sw, sh, *_ = source
            u, v, w, h, ax, sx, ay, sy = target
            for row in range(h):
                py = sv + corresponding_texel(row, h, ay, sy, original, detail, sh)
                for col in range(w):
                    px = su + corresponding_texel(col, w, ax, sx, original, detail, sw)
                    r, g, b, a = base.getpixel((px, py))
                    color = tuple(min(255, round(c * factor)) for c in (r, g, b))
                    canvas.putpixel((u+col, v+row), (*color, a))
    return canvas


if __name__ == "__main__":
    for species in BREASTS:
        for path in (ROOT / "src/main/resources/assets/parapet/textures/entity" / species).glob("*.png"):
            with Image.open(path) as image:
                result = extend_atlas(image, species)
            result.save(path, optimize=True)
