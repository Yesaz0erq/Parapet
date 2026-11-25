"""Paint the macaws, grey parrots and Sulphur-crested Cockatoo at native UV resolution.

Run with Python and Pillow. All marks below are original pixel drawings. Bird
accounts and field photographs from the World Parrot Trust informed the choice
of areas to paint, including the bare cheek, wing speculum and central tail.
"""

from pathlib import Path

from PIL import Image

from paint_medium_eyes import large_eye_color
from shape_parrot_plumage import add_layers


ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/parapet/textures/entity"
SIDES = {"east", "west"}


def color(hexadecimal):
    return tuple(bytes.fromhex(hexadecimal)) + (255,)


def mix(first, second, fraction):
    return tuple(round(a + (b - a) * fraction) for a, b in zip(first, second))


def zones(head, torso, wings, tail, bill, face, pattern="lined"):
    """Group related plumage fields so individual body regions stay editable."""
    fields = {
        "forehead crown throat skin cheek_line iris": head,
        "breast belly mantle rump thigh": torso,
        "shoulder covert flight underwing": wings,
        "tail_base tail_tip undertail": tail,
        "upper_bill lower_bill bill_hook": bill,
    }
    paint = {}
    for labels, hex_values in fields.items():
        for label, value in zip(labels.split(), hex_values.split()):
            paint[label] = color(value)
    paint["face"] = face
    paint["pattern"] = pattern
    paint["pupil"] = color("212125")
    paint["foot"] = color("A09A99")
    paint["foot_shadow"] = color("817E83")
    paint["breast_shadow"] = mix(paint["breast"], paint["mantle"], .26)
    paint["flight_light"] = mix(paint["flight"], paint["covert"], .24)
    paint["covert_light"] = mix(paint["covert"], color("FDFAE9"), .12)
    paint["tail_shaft"] = mix(paint["tail_base"], paint["tail_tip"], .36)
    return paint


# Head fields: forehead, crown, throat, cheek skin, feather lines, iris.
# Torso: breast, belly, mantle, rump, thighs. Wings: shoulder, median coverts,
# primaries and inner surface. Tail: proximal feathers, distal feathers, lining.
SPECIES = {
    "blue_and_yellow": zones("59A258 4596BD 303B3A F3E9D9 46675D DDCA8B",
        "F5C049 F2BC45 3C9BC3 479AC8 E1A843", "408EB1 479DC2 375C87 CE9B48",
        "508BAE 3E6B99 C39D51", "4A4A48 383A3B 3A3A3C", "broad"),
    "blue_throated": zones("52A5AB 55A4B0 479C9F F2EAE0 548790 D7BF7D",
        "F2BB4E EEB54E 4B9DAE 4C9DB6 E6A947", "529DA8 5299B3 3A648C C7A267",
        "4A95AA 417792 B89A65", "535453 414342 414446", "broad", "bold"),
    "scarlet": zones("E95847 DF5247 D94D46 F6EBDD DCA99B E0C798",
        "E95847 E15344 C64D48 549AB9 DC5045", "D95146 F0C24B 3A71A6 C75C53",
        "CC524D 48789E AD5250", "E4C6A4 403E40 BD9C7F", "broad", "sparse"),
    "red_and_green": zones("C84B4B BB494C B14449 F5E8DC B55656 DEC894",
        "C64B4C BB4449 9C4A53 5088B0 B7464B", "BC494B 64A264 3D73A4 AF5157",
        "BD5459 557EA7 B25356", "DFBEA2 3F3D40 BB9A84", "broad", "bold"),
    "military": zones("DC5449 5F9780 797B4E ECD4C6 6B6E55 E5BD87",
        "78A45D 79A15C 718E57 6B9EBC 739C57", "6E8C57 73945B 426F9E B69C58",
        "995B53 43698D A58B52", "575854 3C4142 444746", "broad"),
    "great_green": zones("DF5C50 94B66D 9C9F5A F2DCD1 8B8864 E4C599",
        "A9C76D ABC76D 8DAF66 729EA8 A0BB6A", "96B568 97B46A 527CA0 C3AF6A",
        "BC7256 5B7F9A B49669", "585B58 424645 A69F92", "broad"),
    "red_fronted": zones("DC7153 8AA56B 839B61 ECBCA5 D88568 DDB57B",
        "92AB64 91A85D 839D5F 77986B CF6851", "D56C4E 87A261 4B7497 CC714F",
        "819A61 4D7A9B B49666", "54524C 404143 3A4141", "small", "ear_patch"),
    "chestnut_fronted": zones("8F6352 628F82 875F50 F0E2CC 596057 DDBB75",
        "83AC5F 83AB5C 688F54 669264 78A159", "CE6055 6B985C 527CA1 BE6650",
        "996254 4C7CA3 A86450", "555750 414442 444644", "broad"),
    "blue_winged": zones("CF6355 648F84 6A9D7E ECDCA4 B8AA80 C98A57",
        "77AC61 B66954 62975A BA6754 77A763", "6DA261 5B9A81 4F78A3 A89360",
        "AB6854 4C7DA3 9B805E", "515551 383C3C 3E4645", "small", "abdomen"),
    "blue_headed": zones("6091B2 598BAD 638FA7 76848A 576A74 CFB477",
        "78A868 7BA769 65915A 638959 739D64", "66935E 659C75 4B7094 849173",
        "976050 63865B 8D7C5F", "555A58 444847 474D4C", "small"),
    "yellow_collared": zones("4D5147 484D46 626256 EEE8D6 B7B6A1 B98354",
        "88AA5C 87A75C 769C55 738F60 7D9C59", "789F59 6E9559 517995 9AA36A",
        "9E6756 567DA0 968369", "595953 454746 CCB79C", "small", "yellow_collar"),
    "red_bellied": zones("648E9C 70936D 8B9C78 E9C761 C5A657 8C7357",
        "8DA37A 966450 6F955E 708966 829C64", "799860 75955D 547783 B7AB75",
        "778760 678166 B0A774", "535651 3D4340 424845", "broad", "scalloped_belly"),
    "red_shouldered": zones("638AA6 629478 7DA55C EEE9D5 C9C7AF C28D53",
        "95B45D 91B15D 759C55 75A15F 86A755", "CA6552 7CA55B 4C7E81 CD6553",
        "789962 5F8B66 9E9F68", "535653 414542 444644", "small", "plain"),
    "hyacinth": zones("5968B7 5364B2 4F5FAC EDC65B EDC65B 745E51",
        "5363BC 4C5CB5 4755A0 4E5DAB 4B5EAE", "495BA7 4859A5 384884 3E4783",
        "4758A4 3D4B8B 39437E", "414349 33373C 41434A", "crescent"),
    "lear": zones("547E87 537B8D 557489 EAC963 EAC963 765F4F",
        "516B9F 4C639E 4B598C 505D95 4F6396", "4C608F 4A5A90 3B476E 3E4C73",
        "4E5D8C 404B76 3C466C", "45494A 353A3C 454649", "teardrop"),
    "spix": zones("9EACB2 A0AFB7 9CAFB9 8A959A 88969B D0B470",
        "82AAC1 7CA5C0 638EAC 6C95AF 7DA2B7", "739BB2 688FAC 4F7191 657F96",
        "6587A5 4E7196 627E94", "4A4E4D 383E41 3D4343", "grey_lores"),
}

GREYS = {
    "congo": zones("B3B7B4 A4AAA9 A4A9A7 EAECE2 CED3CC D9CB94",
        "A8ADAA A0A6A5 8C9496 959C9D A5AAA6", "959C9D 838B90 586066 777F82",
        "DE594A D64A43 C64E45", "4B4C50 37393E 383A3C", "grey_parrot"),
    "timneh": zones("89918F 818A8B 8B9290 DCE0D8 BBC3BD CFC194",
        "909795 838C8B 6B7477 777F80 89908F", "7C8384 717A7D 50585D 626C6F",
        "944C50 87434B 864A50", "C9B09F 434447 575256", "grey_parrot"),
}
COCKATOO = zones("FCFAEE F9F7ED F5F1D9 F4F0E5 EEDDA7 795D50",
    "F7F5EB EBECE4 E8EAE4 E9E8DC E6E9DF", "F2F1E8 E3E7E2 D0D8D4 EFE4BA",
    "E5E7DD D8DDD5 EDE2B0", "64666B 45484D 515359", "cockatoo")


def pick(grid, u, v):
    row = grid[min(len(grid) - 1, int(v * len(grid)))]
    return row[min(len(row) - 1, int(u * len(row)))]


CHEEK_SKIN = (
    "..........", "..........", "sss.......", "ssssss....", "sssssss...",
    "ssssssss..", "sssssss...", ".sssss....", "..........", "..........",
)
CHEEK_LINES = (
    "..........", "..........", "..........", "..........", ".....ll...",
    "lll....l..", "...lll....", ".ll.......", "..........", "..........",
)
COVERED_FACE = (
    "..........", "..........", "ss........", "sssss.....", "ssssss....",
    "sssss.....", ".sss......", "..........", "..........", "..........",
)
SCALLOPS = (
    "........", "..x...x.", ".x..xx..", "........", "....x...", "xx.x..xx",
    "........", "..x...x.", ".x..xx..", "........", "....x...", "xx.x..xx",
)


def neck_color(paint, face, u, v):
    if paint["pattern"] == "yellow_collar" and v > .74 and (face == "back" or u > .68):
        return color("EBD16A")
    return paint["crown"]


def head_color(paint, face, u, v, width, height):
    style, marks = paint["face"], paint["pattern"]
    if face == "back":
        return neck_color(paint, face, u, v)
    if face == "top":
        return paint["forehead"] if v > .62 else paint["crown"]
    if face == "bottom":
        return paint["throat"]
    if face == "front":
        if v < .33:
            return paint["forehead"]
        if v > .62:
            if style == "crescent" and v > .76 and .12 < u < .88:
                return paint["skin"]
            return paint["throat"]
        if style in {"broad", "small", "grey_parrot", "grey_lores"} and (u < .27 or u > .73):
            return paint["skin"]
        return paint["forehead"]

    x, y = int(u * width), int(v * height)
    ex, ey = int(width * .28), int(height * .40)
    eye = large_eye_color(paint, x-ex, y-ey)
    if eye is not None:
        return eye
    eye_rim = (abs(x - ex) + abs(y - ey) == 1)
    if style in {"crescent", "teardrop", "cockatoo", "grey_lores"} and eye_rim:
        return paint["skin"]
    if style == "crescent" and u < .38 and .59 < v < .73 + .18 * u:
        return paint["skin"]
    if style == "teardrop" and u < .28 and .55 < v < .83 - .43 * u:
        return paint["skin"]
    if style == "cockatoo":
        if .42 < u < .76 and .48 < v < .7:
            return mix(paint["throat"], paint["cheek_line"], .53)
        return paint["crown"] if v < .65 else paint["throat"]
    if style == "grey_lores" and u < .37 and .34 < v < .57:
        return paint["skin"]
    if style in {"broad", "small", "grey_parrot"}:
        grid = COVERED_FACE if style == "small" else CHEEK_SKIN
        if pick(grid, u, v) == "s":
            has_line = pick(CHEEK_LINES, u, v) == "l"
            if marks == "sparse":
                has_line = has_line and (u > .50 or v > .67)
            if marks == "bold" and .46 < u < .70 and .40 < v < .50:
                has_line = True
            if style == "grey_parrot" or marks in {"plain", "scalloped_belly"}:
                has_line = False
            return paint["cheek_line"] if has_line else paint["skin"]
        if style == "grey_parrot" and v > .54 and pick(SCALLOPS, u, v) == "x":
            return mix(paint["crown"], paint["skin"], .39)
    if marks == "ear_patch" and .55 < u < .83 and .48 < v < .70:
        return paint["forehead"]
    if v < .32 and u < .32:
        return paint["forehead"]
    if v > .70 and u < .68:
        return paint["throat"]
    return neck_color(paint, face, u, v)


def torso_color(paint, face, u, v):
    if face in {"top", "back"}:
        return paint["rump"] if face == "back" and v > .65 else paint["mantle"]
    if face == "bottom":
        return paint["belly"]
    lateral = face in SIDES
    if lateral and u > .80:
        return paint["mantle"]
    if v < .18 and u < (.62 if lateral else 1):
        return paint["throat"]
    marks = paint["pattern"]
    if marks in {"abdomen", "scalloped_belly"}:
        central = u < .45 if lateral else .18 < u < .82
        if central and .53 < v < .90:
            return paint["belly"]
    if marks == "scalloped_belly" or paint["face"] == "grey_parrot":
        if v < .70 and pick(SCALLOPS, u, v) == "x":
            return mix(paint["breast"], paint["skin"], .43)
    origin = paint["belly"] if v > .70 and marks not in {"abdomen", "scalloped_belly"} else paint["breast"]
    if v > .86 or lateral and u > .42:
        return mix(origin, paint["breast_shadow"], .61)
    return origin


def wing_color(paint, part, face, u, v):
    inner = face == ("east" if part.startswith("left") else "west")
    if inner or face == "bottom":
        return paint["underwing"] if "primary" not in part else mix(paint["underwing"], paint["flight"], .36)
    if "primary" in part:
        quill = (int(u * 6) + int(v * 2)) % 3 == 0
        return paint["flight_light"] if quill else paint["flight"]
    # Interlocking blocks run diagonally from the wrist to the tertials.
    boundary = .38 + .18 * u
    if v < boundary:
        return paint["shoulder"]
    if v > .83 + .09 * u:
        return paint["flight_light"]
    if pick(SCALLOPS, u, v) == "x":
        return paint["covert_light"]
    return paint["covert"]


def tail_color(paint, part, face, u, v):
    if face == "bottom":
        return paint["undertail"]
    if part == "tip":
        return paint["tail_tip"]
    if part == "centralTail" and v < .44:
        return paint["tail_tip"]
    if face == "top" and part == "tail" and v > .66:
        return paint["rump"]
    if face == "top" and .38 < u < .56:
        return paint["tail_shaft"]
    return paint["tail_base"]


def surface(variant, paint, part, face, u, v, width, height):
    if part == "head":
        return head_color(paint, face, u, v, width, height)
    if part == "crown":
        return paint["forehead"] if face == "front" or face == "top" and v > .65 else paint["crown"]
    if part == "body":
        return torso_color(paint, face, u, v)
    if part.startswith(("left_", "right_")):
        return wing_color(paint, part, face, u, v)
    if part in {"tail", "centralTail", "tip"}:
        return tail_color(paint, part, face, u, v)
    if part == "thigh":
        return paint["thigh"] if face in {"top", "front"} or v < .55 else mix(paint["thigh"], paint["mantle"], .19)
    if part in {"feet", "tarsus"}:
        return paint["foot_shadow"] if v > .68 or face == "bottom" else paint["foot"]
    if part == "lowerBill":
        return paint["lower_bill"]
    if part == "hook":
        return paint["bill_hook"]
    if part == "upperBill":
        if variant in {"scarlet", "red_and_green", "timneh"} and face in SIDES and u > .55 and v > .41:
            return paint["lower_bill"]
        if face == "bottom":
            return mix(paint["upper_bill"], paint["lower_bill"], .65)
        if face == "top":
            return mix(paint["upper_bill"], color("D6CBBA"), .14)
        return paint["upper_bill"]
    if part == "cere":
        if face == "front" and (u < .25 or u > .75):
            return paint["lower_bill"]
        return mix(paint["upper_bill"], paint["skin"], .37)
    raise KeyError(part)


# Blockbench box-UV origins shared by all seven silhouettes.
UV = {
    "body": (0, 0),
    "head": (60, 0),
    "crown": (74, 22),
    "upperBill": (0, 37),
    "lowerBill": (31, 37),
    "hook": (55, 37),
    "cere": (72, 37),
    "left_wing": (0, 52),
    "right_wing": (33, 52),
    "left_primary": (68, 52),
    "right_primary": (88, 52),
    "tail": (0, 82),
    "centralTail": (52, 82),
    "tip": (0, 110),
    "thigh": (112, 52),
    "tarsus": (112, 67),
    "feet": (108, 82),
    "crest": (78, 110),
}

# Left and right sides have the same cuboid dimensions and separate atlas islands.
SHAPES = {
    "sulfur_crested_cockatoo": {
        "body": (11, 16, 11),
        "head": (10, 10, 10),
        "crown": (9, 1, 9),
        "upperBill": (6, 4, 5),
        "lowerBill": (5, 3, 4),
        "hook": (4, 2, 2),
        "cere": (5, 1, 1),
        "left_wing": (2, 14, 8),
        "left_primary": (1, 10, 5),
        "tail": (8, 2, 11),
        "centralTail": (6, 2, 8),
        "tip": (4, 1, 2),
        "thigh": (3, 4, 3),
        "tarsus": (2, 4, 2),
    },
    "african_grey": {
        "body": (10, 13, 10),
        "head": (9, 8, 9),
        "crown": (8, 1, 8),
        "upperBill": (5, 3, 4),
        "lowerBill": (4, 2, 3),
        "hook": (3, 2, 2),
        "cere": (4, 1, 1),
        "left_wing": (2, 11, 7),
        "left_primary": (1, 8, 5),
        "tail": (8, 2, 7),
        "centralTail": (6, 2, 4),
        "tip": (4, 1, 1),
        "thigh": (3, 4, 3),
        "tarsus": (2, 4, 2),
    },
    "macaw_ara": {
        "body": (11, 16, 11),
        "head": (10, 10, 10),
        "crown": (9, 1, 9),
        "upperBill": (6, 4, 6),
        "lowerBill": (5, 3, 5),
        "hook": (4, 2, 2),
        "cere": (5, 1, 1),
        "left_wing": (2, 15, 9),
        "left_primary": (1, 12, 6),
        "tail": (9, 2, 15),
        "centralTail": (5, 2, 21),
        "tip": (2, 1, 10),
        "thigh": (3, 4, 3),
        "tarsus": (2, 4, 2),
    },
    "macaw_short_ara": {
        "body": (10, 14, 10),
        "head": (9, 9, 9),
        "crown": (8, 1, 8),
        "upperBill": (5, 4, 5),
        "lowerBill": (4, 3, 4),
        "hook": (3, 2, 2),
        "cere": (4, 1, 1),
        "left_wing": (2, 13, 8),
        "left_primary": (1, 10, 5),
        "tail": (8, 2, 12),
        "centralTail": (5, 2, 13),
        "tip": (2, 1, 7),
        "thigh": (3, 4, 3),
        "tarsus": (2, 4, 2),
    },
    "macaw_heavy": {
        "body": (12, 17, 12),
        "head": (11, 11, 10),
        "crown": (10, 1, 9),
        "upperBill": (7, 5, 6),
        "lowerBill": (6, 4, 5),
        "hook": (5, 2, 2),
        "cere": (6, 1, 1),
        "left_wing": (2, 15, 10),
        "left_primary": (1, 12, 7),
        "tail": (9, 2, 16),
        "centralTail": (6, 2, 20),
        "tip": (2, 1, 10),
        "thigh": (3, 4, 3),
        "tarsus": (2, 4, 2),
    },
    "macaw_slender": {
        "body": (9, 14, 9),
        "head": (8, 8, 9),
        "crown": (7, 1, 8),
        "upperBill": (5, 3, 4),
        "lowerBill": (4, 2, 3),
        "hook": (3, 2, 2),
        "cere": (4, 1, 1),
        "left_wing": (2, 13, 7),
        "left_primary": (1, 10, 5),
        "tail": (7, 2, 13),
        "centralTail": (4, 2, 15),
        "tip": (2, 1, 7),
        "thigh": (3, 4, 3),
        "tarsus": (2, 4, 2),
    },
    "macaw_mini": {
        "body": (8, 12, 8),
        "head": (7, 7, 8),
        "crown": (6, 1, 7),
        "upperBill": (4, 3, 4),
        "lowerBill": (3, 2, 3),
        "hook": (2, 2, 2),
        "cere": (3, 1, 1),
        "left_wing": (1, 11, 6),
        "left_primary": (1, 8, 4),
        "tail": (6, 1, 9),
        "centralTail": (4, 1, 10),
        "tip": (2, 1, 6),
        "thigh": (3, 4, 3),
        "tarsus": (2, 4, 2),
    },
}

VARIANTS = {
    "sulfur_crested_cockatoo": ('normal',),
    "african_grey": ('congo', 'timneh'),
    "macaw_ara": ('blue_and_yellow', 'blue_throated', 'military', 'great_green', 'scarlet', 'red_and_green'),
    "macaw_short_ara": ('chestnut_fronted', 'red_fronted'),
    "macaw_heavy": ('hyacinth', 'lear'),
    "macaw_slender": ('spix', 'red_bellied'),
    "macaw_mini": ('blue_headed', 'blue_winged', 'yellow_collared', 'red_shouldered'),
}


def paint_bird(group, variant, palette):
    image = Image.new("RGBA", (128, 128))
    pixels = image.load()
    for part, (x, y) in UV.items():
        if part in {"feet", "crest"}:
            continue
        dimension_key = part.replace("right_", "left_")
        w, h, d = SHAPES[group][dimension_key]
        # East (+X) lies to the left of north in a Blockbench box-UV atlas.
        rectangles = (
            ("east", x, y+d, d, h), ("front", x+d, y+d, w, h),
            ("west", x+d+w, y+d, d, h), ("back", x+2*d+w, y+d, w, h),
            ("top", x+d, y, w, d), ("bottom", x+d+w, y, w, d),
        )
        for face, x0, y0, width, height in rectangles:
            for j in range(height):
                for i in range(width):
                    u, v = (i + .5) / width, (j + .5) / height
                    if face == "east":
                        u = 1 - u
                    if face == "bottom":
                        v = 1 - v
                    pixels[x0+i, y0+j] = surface(variant, palette, part, face, u, v, width, height)
    # Individual toes use fractional local UVs inside this shared foot patch.
    x0, y0 = UV["feet"]
    for y in range(y0, y0 + 13):
        for x in range(x0, 128):
            pixels[x, y] = palette["foot"] if y < y0+4 else palette["foot_shadow"]
    if group == "sulfur_crested_cockatoo":
        # The outer yellow feathers and shorter central filaments share their color here.
        x0, y0 = UV["crest"]
        for y in range(y0, 128):
            for x in range(x0, x0+20):
                warm = color("F3D750") if y > y0+6 else color("FBE66B")
                pixels[x, y] = mix(warm, color("F5EB95"), .19) if (x-x0) % 4 == 1 else warm
    directory = ASSETS / ("macaw" if group.startswith("macaw_") else group)
    directory.mkdir(parents=True, exist_ok=True)
    expanded = add_layers(image, SHAPES[group], UV)
    expanded.save(directory / (variant + ".png"), optimize=True)


def main():
    for group, variants in VARIANTS.items():
        for variant in variants:
            if group == "sulfur_crested_cockatoo":
                palette = COCKATOO
            elif group == "african_grey":
                palette = GREYS[variant]
            else:
                palette = SPECIES[variant]
            paint_bird(group, variant, palette)


if __name__ == "__main__":
    main()
