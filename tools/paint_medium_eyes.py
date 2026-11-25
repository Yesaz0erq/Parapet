"""Paint the eyes of the existing medium and large parrots on their native UV grids."""

from pathlib import Path

from PIL import Image


# Box UV origin, head width/depth and original pupil position, measured from the bill.
HEADS = {
    "cockatiel": ((30, 0), (6, 6), (1, 2)),
    "green_cheek_conure": ((30, 0), (7, 7), (2, 2)),
    "monk_parakeet": ((33, 0), (8, 7), (2, 2)),
    "caique": ((44, 0), (7, 7), (1, 3)),
    "sun_conure": ((44, 0), (7, 7), (2, 3)),
    "eclectus": ((44, 0), (9, 9), (3, 4)),
}


def rgba(hex_value):
    return tuple(bytes.fromhex(hex_value)) + (255,)


def blend(a, b, amount):
    return tuple(round(x + (y - x) * amount) for x, y in zip(a, b))


def eye_origins(uv, head_width, head_depth, s, t):
    """Return left and right pupils and the direction towards the nape on each cheek."""
    x, y = uv
    return ((x+head_depth-1-s, y+head_depth+t, -1),
            (x+head_depth+head_width+s, y+head_depth+t, 1))


def eclectus_eye(image, x, y, back, female):
    # A thin blue *feathered* ring surrounds the Papuan female's pale iris.
    # Two horizontal runs formerly placed iris pixels too far forward on the lower row.
    iris = rgba("D7D09C" if female else "C88744")
    pupil = rgba("211D24" if female else "201F1D")
    rim = rgba("5962BC" if female else "3C9C57")
    cheek = rgba("E75048" if female else "48A85C")
    patch = {
        (0, -1): pupil, (0, 0): pupil, (1, 0): iris, (-1, 0): blend(iris, pupil, .31),
        (1, -1): blend(iris, pupil, .52), (0, -2): rim, (2, 0): rim,
        (-2, 0): rim, (0, 1): rim,
        (-2, 1): cheek, (-1, 1): blend(cheek, rim, .37),
    }
    for (dx, dy), value in patch.items():
        image.putpixel((x+back*dx, y+dy), value)


def detail_eyes(image, species, variant):
    uv, (width, depth), (s, t) = HEADS[species]
    if species == "green_cheek_conure" and variant == "suncheek":
        t = 3
    if species == "caique" and variant == "white_bellied":
        s = 2
    for px, py, back in eye_origins(uv, width, depth, s, t):
        if species == "eclectus":
            eclectus_eye(image, px, py, back, variant == "female")
            continue
        pupil = image.getpixel((px, py))
        if species in {"green_cheek_conure", "sun_conure"}:
            # Keep the forward bare-skin pixel and at least one feathered crown row.
            rim = image.getpixel((px-back, py))
            iris = rgba("895056" if variant == "suncheek" else "755541")
            if variant == "juvenile":
                iris = rgba("64513F")
            step = 1 if t == 2 else -1
            if step == 1:
                image.putpixel((px, py-2), image.getpixel((px+back, py-2)))
                image.putpixel((px+back, py-1), image.getpixel((px+2*back, py-1)))
            upper_rim = blend(rim, rgba("FFFAF1"), .14)
            lower_rim = blend(rim, pupil, .20)
            image.putpixel((px, py+2*step), lower_rim if step == 1 else upper_rim)
            image.putpixel((px, py+step), blend(pupil, iris, .12))
            image.putpixel((px+back, py), iris)
            image.putpixel((px+back, py+step), blend(iris, pupil, .60))
            image.putpixel((px+2*back, py), rim)
            image.putpixel((px, py-step), upper_rim if step == 1 else lower_rim)
        elif species == "caique":
            iris = rgba("B97843" if variant == "black_headed" else "C67F49")
            image.putpixel((px, py+1), blend(pupil, iris, .12))
            image.putpixel((px+back, py), iris)
            image.putpixel((px+back, py+1), blend(iris, pupil, .50))
            if variant == "white_bellied":
                image.putpixel((px, py+2), rgba("D6AEA1"))
        else:
            # Raising the upper edge preserves the cockatiel's colored cheek patch.
            ino = variant in {"lutino", "albino"}
            iris = rgba("AA6665" if ino else "705848")
            image.putpixel((px, py-1), blend(pupil, iris, .15))
            image.putpixel((px+back, py), iris)
            image.putpixel((px+back, py-1), blend(iris, pupil, .58))


def large_eye_color(paint, dx, dy):
    """A slim two-texel dark center framed by a rounded, species-colored iris."""
    pupil, iris = paint["pupil"], paint["iris"]
    if (dx, dy) == (0, 0):
        return pupil
    if (dx, dy) == (0, -1):
        return blend(pupil, iris, .08)
    if (dx, dy) == (1, 0):
        return iris
    if (dx, dy) == (1, -1):
        return blend(iris, pupil, .53)
    if paint["face"] == "grey_parrot" and (dx, dy) == (-1, 0):
        return blend(iris, pupil, .24)
    if paint["face"] in {"crescent", "teardrop", "cockatoo", "grey_lores"}:
        if (dx, dy) in {(0, -2), (2, 0)}:
            return paint["skin"]
    return None


def apply_large_eyes(image, paint, uv, head_size):
    width, height, depth = head_size
    for px, py, back in eye_origins(uv, width, depth, int(depth*.28), int(height*.40)):
        for dy in range(-2, 1):
            for dx in range(-1, 3):
                eye = large_eye_color(paint, dx, dy)
                if eye is not None:
                    image.putpixel((px+back*dx, py+dy), eye)


def main():
    from paint_macaws import COCKATOO, GREYS, SHAPES, SPECIES, UV, VARIANTS

    assets = Path(__file__).resolve().parents[1] / "src/main/resources/assets/parapet/textures/entity"
    for species in HEADS:
        for path in sorted((assets / species).glob("*.png")):
            with Image.open(path) as source:
                image = source.convert("RGBA")
            detail_eyes(image, species, path.stem)
            image.save(path, optimize=True)
    for group, variants in VARIANTS.items():
        for variant in variants:
            if group == "sulfur_crested_cockatoo":
                palette = COCKATOO
            elif group == "african_grey":
                palette = GREYS[variant]
            else:
                palette = SPECIES[variant]
            directory = "macaw" if group.startswith("macaw_") else group
            path = assets / directory / (variant+".png")
            with Image.open(path) as source:
                image = source.convert("RGBA")
            apply_large_eyes(image, palette, UV["head"], SHAPES[group]["head"])
            image.save(path, optimize=True)


if __name__ == "__main__":
    main()
