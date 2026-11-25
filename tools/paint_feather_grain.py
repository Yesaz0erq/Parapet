"""Finish six native-resolution skins after painting their base atlases (requires Pillow).

Run `python tools/paint_feather_grain.py` to update the game PNGs and the matching
embedded Blockbench textures. With an editor session open, use --textures-only
and refresh those named textures in the editor before saving the projects.
"""
import argparse
import base64
import io
import json
from pathlib import Path
import random
import zlib

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/parapet/textures/entity"
SKINS = {
    "african_grey": ("congo", "timneh"),
    "sulfur_crested_cockatoo": ("normal",),
    "eclectus": ("male", "female"),
    "macaw_ara": ("blue_and_yellow",),
}
TORSO = {"narrow_waist", "rounded_breast", "shoulder_mantle", "breast_keel",
         "scapular_mantle", "keel_and_breast", "tapered_rump"}
WING = {"coverts", "scapular_feathers", "shoulder_coverts", "secondaries"}


def uv_faces(element):
    """Use the authored face UVs, including flipped faces and mirrored wings."""
    for direction, face in element["faces"].items():
        u0, v0, u1, v1 = map(round, face["uv"])
        if u0 == u1 or v0 == v1:
            continue
        yield direction, (min(u0, u1), min(v0, v1), max(u0, u1), max(v0, v1))


def face_marks(bounds, key, wing, grey):
    x0, y0, x1, y1 = bounds
    width, height = x1 - x0, y1 - y0
    rng = random.Random(zlib.crc32(key.encode()))
    # Offset successive rows like overlapping coverts, breaking the rhythm with gaps.
    for row, y in enumerate(range(rng.randrange(min(2, height)), height, 3)):
        for x in range((2 * row + rng.randrange(2)) % 4, width, 4):
            if rng.random() < 0.18:
                continue
            highlight = rng.random() < (0.69 if grey else 0.52)
            strength = rng.uniform(0.80, 1.08)
            yield (x0 + x, y0 + y), highlight, strength
            if rng.random() < 0.61:
                # Feather tips run along the breast, and down the folded wing.
                end_x, end_y = (x, y + 1) if wing else (x + 1, y)
                if end_x < width and end_y < height:
                    yield (x0 + end_x, y0 + end_y), highlight, strength * 0.78


def color_delta(base, skin, highlight):
    if skin == "african_grey":
        return (16, 17, 18) if highlight else (-15, -14, -12)
    if skin == "sulfur_crested_cockatoo":
        return (6, 6, 3) if highlight else (-11, -9, -6)
    red, green, blue = base[:3]
    if red > blue * 1.5 and green > red * 0.63:
        return (13, 10, 2) if highlight else (-11, -9, -3)
    if red == max(red, green, blue):
        return (16, 7, 8) if highlight else (-13, -5, -5)
    if green == max(red, green, blue):
        return (6, 15, 7) if highlight else (-6, -13, -7)
    return (8, 13, 17) if highlight else (-5, -10, -15)


def apply_grain(image, model, skin, variant):
    src = image.convert("RGBA")
    result = src.copy()
    reserved = set()
    marks = {}
    for element in model["elements"]:
        name = element["name"]
        wing = name.removeprefix("left_").removeprefix("right_") in WING
        for direction, bounds in uv_faces(element):
            x0, y0, x1, y1 = bounds
            if name not in TORSO and not wing:
                reserved.update((x, y) for y in range(y0, y1) for x in range(x0, x1))
                continue
            key = f"{skin}/{variant}/{name}/{direction}"
            for xy, light, amount in face_marks(bounds, key, wing, skin == "african_grey"):
                marks[xy] = bounds, light, amount

    # Each stroke takes its base from nearby untouched texels of the same UV face.
    # Anchors never include any destination, so a second pass gives identical colors.
    for (x, y), (bounds, light, amount) in marks.items():
        old = src.getpixel((x, y))
        if (x, y) in reserved or old[3] != 255:
            continue
        x0, y0, x1, y1 = bounds
        samples = []
        for sy in range(max(y0, y - 1), min(y1, y + 2)):
            for sx in range(max(x0, x - 1), min(x1, x + 2)):
                if (sx, sy) not in marks:
                    pixel = src.getpixel((sx, sy))
                    if pixel[3] == 255:
                        samples.append(pixel)
        if not samples:
            continue
        base = sorted(samples, key=lambda c: c[0] + 2 * c[1] + c[2])[len(samples) // 2]
        # Preserve an existing band, contrasting feather edge or hue transition.
        if max(abs(a - b) for a, b in zip(old, base)) > 9:
            continue
        delta = color_delta(base, skin, light)
        rgb = [max(0, min(255, round(c + d * amount))) for c, d in zip(base, delta)]
        result.putpixel((x, y), (*rgb, old[3]))
    return result


def replace_sources(project_text, replacements):
    """Keep every other byte of the editable project, including its formatting."""
    for old, new in replacements:
        if old == new:
            continue
        if project_text.count(old) != 1:
            raise ValueError("The embedded texture source is ambiguous in this project")
        project_text = project_text.replace(old, new, 1)
    return project_text


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--textures-only", action="store_true")
    args = parser.parse_args()
    for skin, variants in SKINS.items():
        path = ROOT / "art" / (skin + ".bbmodel")
        text = path.read_text()
        model = json.loads(text)
        textures = {t["name"]: t for t in model["textures"]}
        directory = ASSETS / ("macaw" if skin == "macaw_ara" else skin)
        replacements = []
        for variant in variants:
            png = directory / (variant + ".png")
            with Image.open(png) as image:
                colored = apply_grain(image, model, skin, variant)
                if colored.tobytes() != image.convert("RGBA").tobytes():
                    colored.save(png, optimize=True)
            source = textures[png.name]["source"]
            original = base64.b64decode(source.split(",", 1)[1])
            if Image.open(io.BytesIO(original)).convert("RGBA").tobytes() != colored.tobytes():
                encoded = base64.b64encode(png.read_bytes()).decode()
                replacements.append((source, "data:image/png;base64," + encoded))
        if replacements and not args.textures_only:
            path.write_text(replace_sources(text, replacements))


if __name__ == "__main__":
    main()
