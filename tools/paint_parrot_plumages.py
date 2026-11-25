"""Paint 64 × 64 lovebird and monk parakeet atlases. Run with Python and Pillow.

Each cuboid uses Blockbench box UVs. Small motifs below are pixel drawings,
not image filters: the cheeks, mantle, tail, primaries and breast have independent
pigments. The lovebird registry supplies the actual combinations to paint.
"""

import json
from pathlib import Path

from PIL import Image

from extend_existing_parrot_atlases import extend_atlas
from paint_medium_eyes import detail_eyes

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
TEXTURES = RESOURCES / "assets/parapet/textures/entity"
REGISTRY = RESOURCES / "data/parapet/plumage/lovebird.json"

# B/b body in light and shadow, W/w mantle, F/f primaries, R rump, T/t tail,
# H forehead, M/m face and chin, N nape, L collar, A belly band, S breast scales,
# O bare orbital ring, E eye, K/k bill, P/p toes, X clear feather, V mantle chevron.
BASES = {
    # body, mantle, flight, rump
    "green":      ("87C64D", "5DAB4E", "384D48", "58A0E3"),
    "dark_green": ("71A64F", "4E8B49", "364A42", "4B82AC"),
    "olive":      ("989A4E", "7A8249", "535446", "697984"),
    "aqua":       ("6CC0AD", "51A29D", "385B65", "6099CC"),
    "turquoise":  ("6BAFC6", "539EAC", "3F556C", "608DC9"),
    "aqua_turquoise": ("71BABA", "53A3AA", "3C5B68", "5B96CB"),
    "parblue":    ("65BFC8", "4FA6B6", "3D5869", "5D95CA"),
    "grey":       ("A8A7AB", "8C8B90", "5D5D66", "83899B"),
    "blue":       ("64B8E1", "4299CA", "3B5675", "527EC1"),
    "cobalt":     ("5D89C4", "486CAE", "3C496A", "5266AB"),
    "mauve":      ("9793A6", "7C788B", "544F65", "847A9C"),
    "violet":     ("927FCA", "7766B5", "49456C", "7460BE"),
}
GREEN_SERIES = {"green", "dark_green", "olive"}
PARBLUE_SERIES = {"aqua", "aqua_turquoise", "turquoise", "parblue"}


def rgb(value):
    return tuple(bytes.fromhex(value))


def mix(first, second, amount):
    return tuple(round(a + (b - a) * amount) for a, b in zip(first, second))


def tint(palette, keys, target, amount):
    for key in keys:
        palette[key] = mix(palette[key], target, amount)


def lovebird_palette(species, base, traits):
    body, mantle, flight, rump = map(rgb, BASES[base])
    green = base in GREEN_SERIES
    parblue = base in PARBLUE_SERIES
    clear = rgb("F9E665" if green else "F2E9AB" if parblue else "F5F3E7")
    p = {
        "B": body, "b": mix(body, mantle, .61), "W": mantle,
        "w": mix(mantle, flight, .28), "F": flight, "f": mix(flight, mantle, .55),
        "R": rump, "T": mix(body, flight, .41), "t": mix(mantle, flight, .60),
        "N": mix(mantle, body, .35), "X": clear, "V": mix(body, clear, .33),
        "E": rgb("191C20"), "O": rgb("F7F1DF"), "P": rgb("AD9B97"),
        "p": rgb("8D8283"), "A": mix(body, clear, .24), "S": mix(body, mantle, .34),
    }
    if species == "peach":
        # Rosy-faced has an unringed eye and a pale horn bill in all three series.
        if green:
            forehead, mask, throat = "EC6D58", "F38E76", "EFA582"
        elif base == "aqua":
            forehead, mask, throat = "E8A378", "E8C49B", "DBD3AB"
        elif base == "aqua_turquoise":
            forehead, mask, throat = "EDAD83", "E9D9BA", "DDE1C3"
        elif base == "turquoise":
            forehead, mask, throat = "E8BA92", "F0E6CA", "DEE4D5"
        else:
            forehead, mask, throat = "E5E2D6", "EFF1E7", "DCE3DF"
        p.update(H=rgb(forehead), M=rgb(mask), m=rgb(throat), L=body, K=rgb("D6B79A"))
    elif species == "fischer":
        if green:
            forehead, mask, throat, collar = "D96942", "ED8142", "F3AB49", "EDCB59"
        elif parblue:
            forehead, mask, throat, collar = "ADB49C", "E8D196", "F1DD9B", "ECE6B7"
        else:
            forehead, mask, throat, collar = "8F9C9C", "CDD5D0", "E6E9DC", "EFF0E3"
        p.update(H=rgb(forehead), M=rgb(mask), m=rgb(throat), L=rgb(collar),
                 N=mix(mantle, rgb("9C8965") if green else rgb("929CA5"), .50),
                 K=rgb("DF6655" if green else "D78B78" if parblue else "DFC0AA"))
    elif species == "masked":
        hood, cheek, throat = ("434438", "363A34", "41433B") if green else ("424852", "353B45", "464C54")
        p.update(H=rgb(hood), M=rgb(cheek), m=rgb(throat), N=mix(rgb(hood), mantle, .23),
                 L=rgb("F1D45B" if green else "ECE5AD" if parblue else "EFEEE1"),
                 K=rgb("DC5E51" if green else "CE8778" if parblue else "DCB5A1"))
    else:
        raise ValueError(f"Unknown lovebird species {species}")

    if "orange_face" in traits:
        p.update(H=rgb("EF9735"), M=rgb("F6AE48"), m=rgb("EDBE65"))
    if "cinnamon" in traits:
        tint(p, "BWbRwTt", rgb("C5A878"), .32)
        p["F"], p["f"] = rgb("806E60"), rgb("A28C73")
    if "pallid" in traits:
        tint(p, "BbWwRTtN", clear, .47)
        p["F"], p["f"] = rgb("A9ABA4"), mix(p["W"], rgb("B6B8AE"), .50)
    if "pastel" in traits:
        tint(p, "BbWwRTtNHMm", clear, .34)
        p["F"], p["f"] = rgb("909699"), mix(p["W"], rgb("ACAEA8"), .45)
    if "dilute" in traits:
        tint(p, "BbWwRTtN", clear, .67)
        p["F"], p["f"] = rgb("A7ADA8"), mix(p["W"], rgb("D0D0B9"), .50)
    if "slaty" in traits:
        tint(p, "BbWwTtN", rgb("74868F"), .47)
        p["R"] = mix(p["R"], rgb("82899E"), .40)
    if "misty" in traits:
        tint(p, "BbWwTtN", rgb("ABA276" if green else "9EA5A9"), .29)
    if "marbled" in traits or "edged" in traits:
        tint(p, "BbWwTt", clear, .34 if "marbled" in traits else .23)
        p["V"] = mix(p["W"], clear, .57 if "marbled" in traits else .41)
        p["F"] = mix(p["F"], rgb("B5B6A8"), .48)
    if {"bronze_fallow", "pale_fallow"} & traits:
        pale = "pale_fallow" in traits
        tint(p, "BbWwTtN", clear, .64 if pale else .37)
        p["W"] = mix(p["B"], clear, .24) if pale else mix(p["W"], rgb("AE9673"), .36)
        p["F"] = rgb("BBAF93" if pale else "826B54")
        p["f"] = mix(p["F"], p["W"], .37)
        p["E"] = rgb("A95760" if pale else "8A424D")
        if species == "masked":
            tint(p, "HMm", rgb("B7AA92" if pale else "9F8064"), .67)
    if "sable" in traits:
        p["N"] = mix(p["M"], p["L"], .34) if green else rgb("F0F0E4")
        p["H"] = mix(p["M"], p["N"], .62)
    if "recessive_pied" in traits:
        p["X"] = mix(clear, rgb("FFF8CE" if green else "FFFCF2"), .25)
    if "euwing" in traits:
        tint(p, "Bb", clear, .69)
        p["w"] = mix(p["W"], rgb("3B4541"), .43)
        p["V"] = mix(p["W"], clear, .52)
    if "opaline" in traits:
        p["R"] = mix(p["B"], p["W"], .20)
        p["V"] = mix(p["W"], clear, .36)
    if "ino" in traits or "dec" in traits:
        clear = rgb("FAE761" if green else "F5E7AB" if parblue else "F7F5E9")
        tint(p, "BbWwRTtNVAX", clear, .93)
        p["F"], p["f"] = rgb("E7E4D7"), mix(clear, rgb("F5F2E4"), .70)
        if "ino" in traits:
            p["E"], p["P"], p["p"] = rgb("823F48"), rgb("D0AA9F"), rgb("B28E87")
        else:
            p["W"] = mix(clear, rgb("FFF4C1" if green else "FDFAF0"), .35)
            p["F"] = mix(clear, rgb("FBF7E8"), .72)
        if species != "peach":
            ino_face = rgb("F49C56" if green else "F1D4A0" if parblue else "F6F1E7")
            p["H"] = mix(clear, ino_face, .78)
            p["M"] = mix(clear, ino_face, .94 if species == "fischer" else .53)
            p["m"] = mix(clear, ino_face, .66 if species == "fischer" else .36)
            p["L"] = mix(clear, rgb("FFF1D1" if green else "F8F4E7"), .28)
    p["k"] = mix(p["K"], rgb("74534A"), .37)
    return p


# Native-pixel graphics for the eyes, face partition and collar. U runs from
# the beak towards the nape on both sides. Dots denote the underlying feathers.
LOVE_CHEEK = {
    "peach": ("HHHNNN", "HHHHNN", "MMMMNN", "MMMMMN", "mmmmNN", "mmmNBN"),
    "fischer": ("HHHNNN", "HHHHNN", "MMMMHN", "MMMMMN", "mmmLLN", "mLLLLL"),
    "masked": ("HHHHNN", "HHHHHN", "MMMMMH", "MMMMMN", "mmmmNN", "mmLLLL"),
}
LOVE_FRONT = {
    "peach": ("HHHHHH", "HHHHHH", "MMMMMM", "MMMMMM", "mmmmmm", "BmmmmB"),
    "fischer": ("HHHHHH", "HHHHHH", "MMMMMM", "MMmmMM", "mmmmmm", "LLmmLL"),
    "masked": ("HHHHHH", "HHHHHH", "MMMMMM", "MmmmmM", "mmmmmm", "LmmmmL"),
}
LOVE_WING = ("WWWWWW", "WWWwWW", "WWwWwW", "wWWWWw", "WWwWww", "wwwWWW", "wwfwff")
LOVE_BODY = ("BBBBBBB", "BBBBBBB", "BBBBBbB", "BBBBBBB", "BBBBBbb", "bBBBBbb", "bbBBbbb")
PIED_WING = ("..XX..", "..XXX.", "...XX.", "......", "X.....", "XX....", "XX....")
PIED_SIDE = ("......", "..XX..", ".XXXX.", ".XXX..", "..X...", "....XX", "...XXX")
V_MANTLE = ("VwwwwV", "VVwwVV", "wVwwVw", "wVVVVw", "wwVVww", "wwwwww")
EDGE_FEATHERS = ("WWVWWV", "WVWWVW", "VWWVWW", "WWVWWV", "WVWWVW", "VWWVWW")
SIDES = {"left", "right"}


def motif(rows, u, v):
    return rows[min(int(v * len(rows)), len(rows) - 1)][min(int(u * len(rows[0])), len(rows[0]) - 1)]


def love_head(species, traits, face, u, v):
    if face in SIDES:
        value = motif(LOVE_CHEEK[species], u, v)
        if "opaline" in traits and value == "N":
            value = "H" if v < .42 else "M"
        if "pied" in traits and value == "N" and v < .42 and u > .65:
            value = "X"
        eye = motif(("......", "..O...", ".OEO..", "..O...", "......", "......"), u, v)
        if eye == "E" or (eye == "O" and species != "peach"):
            value = eye
        return value
    if face == "front":
        return motif(LOVE_FRONT[species], u, v)
    if face == "back":
        if "opaline" in traits:
            return "M" if v > .40 else "H"
        if "pied" in traits and u > .50 and v < .41:
            return "X"
        return "L" if species != "peach" and v > .65 else "N"
    if face == "top":
        if "opaline" in traits:
            return "H"
        if "pied" in traits and .23 < u < .7 and v < .55:
            return "X"
        return "H" if v > .58 else "N"
    return "m"


def love_body(species, traits, face, u, v):
    if face in {"top", "back"}:
        if "euwing" in traits:
            return motif(V_MANTLE, u, v)
        if "pied" in traits and motif(PIED_SIDE, u, v) == "X":
            return "X"
        return "R" if face == "back" and v > .69 else "W"
    if face == "bottom":
        return "b"
    if species != "peach" and v < (.22 if face == "front" else .18):
        return "L"
    if "pied" in traits and motif(PIED_SIDE, 1-u if face == "left" else u, v) == "X":
        return "X"
    return motif(LOVE_BODY, u, v)


def love_wing(traits, face, u, v):
    if face not in SIDES:
        return "W" if face in {"top", "front"} else "w"
    if "pied" in traits and motif(PIED_WING, 1-u if face == "right" else u, v) == "X":
        return "X"
    if "euwing" in traits:
        return motif(("VWWw", "WVww", "wWVw", "wwWV", "wwww", "wwww"), u, v)
    if "opaline" in traits:
        return motif(EDGE_FEATHERS, u, v)
    return motif(LOVE_WING, u, v)

# body, mantle, primaries, rump/tail, forehead, cheek, breast scallop, abdomen.
MONK = {
    "green":     "89C756 5DAB54 426D99 559E70 ABBBB1 C9CEBF 8D9B91 C3D26B",
    "blue":      "73B9E0 509CCB 466C9B 527EAD B8C7CB D2DADB 96A6A8 A8D7DF",
    "turquoise": "75C7B0 4AA598 46789B 4E958F B0C3BA CFD8CB 91A69F A6DCC8",
    "lutino":    "FBE85D F0D84B E9DFAB E8D066 F8F0B8 FBF2C5 E5DCAA F8ED85",
    "albino":    "F9F7EF EBEDE8 DADFDA E6EAE8 F7F5EC FBF9EF E4E5DC F8F5EA",
    "cinnamon":  "A5BE70 88A568 9C927D 98AB77 C4C2AC DAD7BD ABA791 D2D98F",
    "pallid":    "C7D96F AFC55C ADAD98 B8CB72 D8DABF E1E5CA BEC3A9 E0E69A",
    "grey_green":"8F9B66 75845D 626B65 78866D A7AFA5 C7C9BB 8D998F B3BA80",
    "cobalt":    "648DC5 4E71A9 3E5580 536E9D B1BBC7 D2D4D5 929BAB 98AFCF",
    "violet":    "978ACE 7D70B3 5E5989 7969A5 BCB8CA D9D1DE 9C99AE BBAFDA",
}


def monk_palette(color, swatches):
    body, mantle, flight, rump, forehead, cheek, scallop, belly = map(rgb, swatches.split())
    ino = color in {"lutino", "albino"}
    return {
        "B": body, "b": mix(body, mantle, .59), "W": mantle, "w": mix(mantle, flight, .31),
        "F": flight, "f": mix(flight, body, .36), "T": rump, "t": mix(rump, flight, .53),
        "R": rump, "H": forehead, "M": cheek, "m": mix(cheek, scallop, .31),
        "N": mix(forehead, mantle, .47), "S": scallop, "A": belly,
        "E": rgb("8E4D50" if ino else "231F22"), "K": rgb("E8B488"), "k": rgb("B78B70"),
        "P": rgb("C2A39A" if ino else "A8A1A1"), "p": rgb("AE9089" if ino else "8F898A"),
    }


def monk_head(face, u, v):
    if face in SIDES:
        return motif(("HHNNNNN", "HHHNNNN", "MHEHHNN", "MMMMHHN", "MMMmNNW", "MmmmNWW", "mmmNWwW"), u, v)
    if face == "front":
        return motif(("HHHHHHHH", "HHHHHHHH", "HHMMMMHH", "MMMMMMMM", "MMMMMMMM", "mmMMMMmm", "mmmmmmmm"), u, v)
    if face == "top":
        return "H" if v > .54 else "N"
    if face == "back":
        return "W" if v > .66 else "N"
    return "m"


def monk_body(face, u, v):
    if face == "front":
        return motif(("mmMMMMmm", "SmSmmSmS", "mMSmmSMm", "SmmSSmmS", "mmSmmSmm", "mSmMMSmS",
                      "bAAAAAAb", "BAAAAAAB", "BBBABBBB", "bBBBBBBb", "bbBBBBbb"), u, v)
    if face in SIDES:
        return motif(("mmSSNWWW", "MSmmSWwW", "SmmSNWWW", "mSmNWWWW", "mmSBBBBW", "SbBBBBBW",
                      "AAABbbbb", "AABBBbbb", "BBBBBbbb", "BBbbBbbb", "bbbbbbbb"), u, v)
    if face == "bottom":
        return "b"
    return "R" if face == "back" and v > .67 else "W"


def crown(face, u, v, species, traits):
    if species == "monk_parakeet":
        return "H" if (face == "front" or face == "top" and v > .6) else "N"
    if "opaline" in traits:
        return "H"
    if "recessive_pied" in traits or "pied" in traits and u > .57 and v < .50:
        return "X"
    return "H" if face == "front" or face == "top" and v > .57 else "N"


def pied_patch(face, u, v):
    # Recessive pied leaves only isolated island feathers in a mostly clear field.
    return face in {"bottom", "back"} and u < .31 and v > .69 or (
        face in SIDES and .35 < u < .75 and .37 < v < .70)


def love_surface(part, species, traits, face, u, v):
    if "recessive_pied" in traits and part in {"body", "wing", "primary"}:
        if not pied_patch(face, u, v):
            if part != "body" or species == "peach" or v > .20:
                return "X"
    if part == "head":
        return love_head(species, traits, face, u, v)
    if part == "body":
        return love_body(species, traits, face, u, v)
    if part == "wing":
        if {"marbled", "edged"} & traits and face in SIDES:
            pattern = (("VwVw", "wVVw", "VwwV", "VVwV", "wwVw") if "marbled" in traits
                       else ("WVVW", "VwwV", "WWVV", "VVwW", "wVVw"))
            return motif(pattern, u, v)
        return love_wing(traits, face, u, v)
    if part == "primary" and "pied" in traits and face in SIDES and u < .38:
        return "X"
    return None


def shared_surface(part, species, traits, face, u, v):
    if part == "crown":
        return crown(face, u, v, species, traits)
    if part == "primary":
        return motif(("WfF", "fFF", "fFf", "FFF", "fFF"), u, v) if face in SIDES else "F"
    if part in {"tail_fan", "tail_pair", "tip"}:
        if part == "tail_fan" and v > .55 and face == "top":
            return "R"
        if "opaline" in traits and part == "tail_fan" and face == "top" and u < .37:
            return "m"
        if face in {"top", "bottom"}:
            return "T" if .18 < u < .57 or (u > .76 and v > .57) else "t"
        return "T" if face == "front" else "t"
    if part in {"bill", "hook", "cere"}:
        if part == "hook":
            return "k"
        if part == "cere" and face == "front" and (u < .33 or u > .67):
            return "k"
        return "K" if face in {"front", "top"} or v < .57 else "k"
    if part == "leg":
        return "P" if face in {"front", "top"} and v < .70 else "p"
    raise ValueError(f"Unexpected part {part}")


# part, atlas UV, lovebird dimensions, monk parakeet dimensions
LAYOUT = (
    ("body",      (0, 0),   (6, 7, 6), (8, 11, 8)),
    ("head",      (33, 0),  (6, 6, 6), (8, 7, 7)),
    ("crown",     (32, 34), (5, 1, 5), (7, 1, 6)),
    ("wing",      (0, 20),  (1, 5, 4), (1, 9, 6)),
    ("primary",   (16, 20), (1, 3, 3), (1, 6, 4)),
    ("tail_fan",  (0, 37),  (5, 1, 4), (5, 2, 8)),
    ("tail_pair", (0, 50),  (3, 1, 3), (3, 1, 10)),
    ("tip",       (32, 48), None,      (1, 1, 6)),
    ("bill",      (32, 19), (3, 2, 2), (4, 2, 3)),
    ("hook",      (47, 19), (2, 1, 1), (2, 2, 1)),
    ("cere",      (54, 19), (2, 1, 1), (3, 1, 1)),
    ("leg",       (46, 27), (1, 2, 1), (1, 3, 1)),
)


def draw_atlas(group, file_id, species, palette, traits):
    image = Image.new("RGBA", (64, 64))
    pixels = image.load()
    is_monk = group == "monk_parakeet"

    def feather(part, face, u, v):
        if is_monk:
            if part == "body":
                return monk_body(face, u, v)
            if part == "head":
                return monk_head(face, u, v)
            if part == "wing":
                return love_wing(set(), face, u, v)
        else:
            color = love_surface(part, species, traits, face, u, v)
            if color is not None:
                return color
        return shared_surface(part, species, traits, face, u, v)

    for part, (x, y), love_size, monk_size in LAYOUT:
        size = monk_size if is_monk else love_size
        if size is None:
            continue
        w, h, d = size
        # The side left of the nose unfolds in the opposite horizontal direction.
        rectangles = (
            ("left", x, y+d, d, h), ("front", x+d, y+d, w, h),
            ("right", x+d+w, y+d, d, h), ("back", x+2*d+w, y+d, w, h),
            ("top", x+d, y, w, d), ("bottom", x+d+w, y, w, d),
        )
        for face, x0, y0, width, height in rectangles:
            for row in range(height):
                for col in range(width):
                    u, v = (col + .5) / width, (row + .5) / height
                    if face == "left":
                        u = 1 - u
                    if face == "bottom":
                        v = 1 - v
                    pixels[x0+col, y0+row] = palette[feather(part, face, u, v)] + (255,)
    # Fractional toe cuboids share this padded UV island.
    for y in range(27, 34):
        for x in range(53, 64):
            pixels[x, y] = palette["P" if y < 30 else "p"] + (255,)
    if is_monk:
        detail_eyes(image, group, file_id)
        image = extend_atlas(image, group)
    target = TEXTURES / group
    target.mkdir(parents=True, exist_ok=True)
    image.save(target / (file_id + ".png"), optimize=True)


def main():
    registry = json.loads(REGISTRY.read_text(encoding="utf-8"))
    for entry in registry["variants"]:
        species, traits = entry["species"], set(entry["traits"])
        palette = lovebird_palette(species, entry["base"], traits)
        draw_atlas("lovebird", entry["id"], species, palette, traits)
    for name, swatches in MONK.items():
        draw_atlas("monk_parakeet", name, "monk_parakeet", monk_palette(name, swatches), set())


if __name__ == "__main__":
    main()
