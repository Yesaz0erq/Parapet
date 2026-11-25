"""Paint the field guide's closed front board at Minecraft GUI resolution."""

from pathlib import Path
from random import Random

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "src/main/resources/assets/parapet/textures/gui/field_guide_cover.png"


def panel(draw, bounds, radius, color):
    x, y, right, bottom = bounds
    draw.polygon([
        (x + radius, y), (right - radius, y), (right, y + radius), (right, bottom - radius),
        (right - radius, bottom), (x + radius, bottom), (x, bottom - radius), (x, y + radius),
    ], fill=color)


def feather():
    """Stepped vanes and a single gilt shaft, echoing the inventory item's binding stamp."""
    stamp = Image.new("RGBA", (31, 35))
    draw = ImageDraw.Draw(stamp)
    draw.polygon([
        (2, 31), (5, 27), (5, 20), (7, 16), (7, 13), (10, 8), (12, 10),
        (14, 6), (18, 3), (19, 5), (22, 2), (28, 1), (28, 6),
        (26, 10), (27, 11), (24, 15), (20, 17), (22, 18), (18, 22),
        (13, 24), (14, 25), (10, 28), (7, 28), (3, 33), (1, 33),
    ], fill="#c99e52")
    draw.polygon([(7, 24), (8, 17), (11, 10), (12, 13), (16, 7), (19, 5),
                  (19, 7), (25, 3), (18, 12), (15, 14), (12, 20)], fill="#e5c47c")
    draw.line([(3, 32), (9, 25), (12, 20), (15, 16), (19, 12), (25, 5)], fill="#846439")
    draw.line([(4, 32), (10, 25), (13, 20), (16, 16), (22, 10)], fill="#f3d99a")
    draw.line([(7, 22), (8, 18), (9, 16)], fill="#f4d897")
    draw.line([(13, 23), (17, 21), (19, 19)], fill="#af8145")
    return stamp.resize((62, 70), Image.Resampling.NEAREST)


def paint():
    image = Image.new("RGBA", (160, 200))
    draw = ImageDraw.Draw(image)
    panel(draw, (0, 3, 159, 196), 4, "#332c29")
    panel(draw, (0, 2, 158, 193), 3, "#70503a")
    panel(draw, (0, 1, 158, 190), 4, "#233c33")
    panel(draw, (1, 2, 156, 188), 4, "#426c58")
    panel(draw, (2, 3, 155, 186), 3, "#315a4b")
    # A fixed pattern gives the cover the same slightly worn cloth as the inventory sprite.
    random = Random(902)
    for _ in range(1460):
        x, y = random.randrange(11, 155), random.randrange(5, 185)
        draw.line((x, y, min(x + random.choice((0, 0, 1, 2)), 155), y),
                  fill=random.choice(("#345c4c", "#345e50", "#305747", "#2f5648", "#3a614f")))
    draw.rectangle((0, 5, 8, 186), fill="#28493d")
    draw.line((2, 6, 2, 185), fill="#42674e")
    draw.line((8, 4, 9, 186), fill="#203c33")
    draw.line((10, 4, 10, 185), fill="#60806a")
    for y in range(10, 184, 6):
        draw.line((13, y, 13, y + 2), fill="#8c8861")
    for x, y, dx, dy in [(20, 12, 1, 1), (148, 12, -1, 1), (20, 176, 1, -1), (148, 176, -1, -1)]:
        draw.line([(x, y + 12*dy), (x, y), (x + 14*dx, y)], fill="#a68a54")
        draw.line([(x + 3*dx, y + 7*dy), (x + 3*dx, y + 3*dy), (x + 8*dx, y + 3*dy)], fill="#6f7d5e")
    # Single embossed fillet and two brass caps match the bound boards in field_guide.png.
    draw.line((23, 15, 145, 15), fill="#50705a")
    draw.line((23, 173, 145, 173), fill="#50705a")
    for y, sign in [(3, 1), (188, -1)]:
        draw.line([(143, y), (152, y), (157, y + 4*sign), (157, y + 12*sign)], fill="#ad884e", width=2)
        draw.line([(145, y), (151, y), (155, y + 3*sign)], fill="#e6c47f")
    stamp = feather()
    shadow = Image.new("RGBA", stamp.size, "#294335")
    image.paste(shadow, (53, 62), stamp.getchannel("A"))
    image.alpha_composite(stamp, (53, 60))
    DEST.parent.mkdir(parents=True, exist_ok=True)
    image.save(DEST, optimize=True)


if __name__ == "__main__":
    paint()
