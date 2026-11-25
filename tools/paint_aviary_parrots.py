"""Paint the species in the aviary expansion at the model's box-UV resolution."""

import json
from pathlib import Path

from PIL import Image

from paint_macaws import ASSETS, color, mix
from shape_parrot_plumage import add_layers, faces


def draw_atlas(shape, uv, plumage):
    base = Image.new("RGBA", (128, 128))
    pixels = base.load()
    for part, origin in uv.items():
        if part in {"feet", "crest"}:
            continue
        dimensions = shape[part.replace("right_", "left_")]
        for face, (x, y, width, height) in faces(origin, dimensions).items():
            for j in range(height):
                for i in range(width):
                    u = ((width-i-.5) if face == "east" else (i+.5)) / width
                    v = ((height-j-.5) if face == "bottom" else (j+.5)) / height
                    pixels[x+i, y+j] = surface(plumage, part, face, u, v, width, height)
    x, y = uv["feet"]
    for j in range(13):
        for i in range(128-x):
            pixels[x+i, y+j] = plumage["foot"] if j < 4 else plumage["toe_shadow"]
    if plumage["style"] in {"rainbow", "red_lory", "dusky"}:
        for face, (x, y, width, height) in faces((96, 110), (1, 1, 2)).items():
            for j in range(height):
                for i in range(width):
                    distal = (face == "front" or face == "east" and i == width-1
                              or face == "west" and i == 0 or face == "top" and j == height-1
                              or face == "bottom" and j == 0)
                    pixels[x+i, y+j] = color("A46170" if distal else "D89595")
    return add_layers(base, shape, uv)


def wing(p, part, face, u, v):
    inner = face == ("east" if part.startswith("left") else "west")
    if inner or face == "bottom":
        return p["underwing"] if "primary" not in part else mix(p["underwing"], p["flight"], .38)
    if "primary" in part:
        # Long and short vanes alternate down the primary fan.
        stripe = int(u*5+.64*v) % 3
        return mix(p["flight"], p["covert"], .22 if stripe == 1 else .04)
    if v < .37 + .18*u:
        return p["shoulder"]
    if v > .76+.13*u:
        return mix(p["flight"], p["covert"], .18)
    if (int(1.22*v*6)+int(u*6))%4 == 0:
        return mix(p["covert"], p["mantle"], .35)
    return p["covert"]


def tail(p, part, face, u, v):
    if face == "bottom":
        return p["undertail"]
    if part == "tip" or part == "centralTail" and face == "top" and v < .3:
        return p["tail_tip"]
    if part == "tail" and face == "top" and v > .70:
        return p["rump"]
    return mix(p["tail"], p["tail_tip"], .28) if .38 < u < .57 else p["tail"]


def eye(p, u, v, width, height):
    x, y = int(u*width), int(v*height)
    # Add the upper eyelid towards the crown, clearing rosy cheeks and neck rings below.
    dx, dy = x-int(width*.30), y-int(height*.40)
    if (dx, dy) == (0, 0):
        return p["pupil"]
    if (dx, dy) == (1, 0):
        return p["iris"]
    if p["style"] == "bourke":
        return None
    if p["orbital"] is not None:
        if (dx, dy) == (0, -1):
            return mix(p["pupil"], p["iris"], .12)
        if (dx, dy) == (1, -1):
            return mix(p["iris"], p["pupil"], .40)
        if (dx, dy) in {(-1, 0), (2, 0), (0, -2), (0, 1)}:
            return p["orbital"]
    else:
        if (dx, dy) == (0, -1):
            return mix(p["pupil"], p["iris"], .52)
        if width >= 8 and (dx, dy) == (1, -1):
            return mix(p["iris"], p["crown"], .40)
    return None


def palette(style, head, body, wings, tail_feathers, soft_parts):
    groups = (
        ("forehead crown cheek throat", head),
        ("breast belly mantle rump thigh", body),
        ("shoulder covert flight underwing", wings),
        ("tail tail_tip undertail", tail_feathers),
        ("bill lower_bill iris orbital foot", soft_parts),
    )
    p = {"style": style, "pupil": color("22222A")}
    for names, hex_codes in groups:
        for name, code in zip(names.split(), hex_codes.split(), strict=True):
            p[name] = None if code == "-" else color(code)
    p["toe_shadow"] = mix(p["foot"], color("544D5A"), .29)
    return p


def recolor(p, **regions):
    return {**p, **{region: color(value) for region, value in regions.items()}}


JARDINE = palette("jardine", "DD5D39 476C48 4D7345 65834F", "79A352 70A052 476A48 64A159 DC6241",
                  "DA6340 4E794C 343E40 486851", "4D6748 38433E 526947", "C9AC82 49494B D47E53 E0C5BA 85817D")
SENEGAL = palette("senegal", "77746F 6A6866 8D8980 8E8A7A", "79AA55 EFC54A 5F8E50 A2B75B 7EAC59",
                  "6C9C54 608A52 404D47 ECCC52", "577754 465D4F E6BF47", "878781 646963 E0C562 6E6D68 868582")
BOURKE = palette("bourke", "74A3CA 918C7A B9ADA2 C7A5A3", "C8A29D E49DAF 887F70 72A1CC D99AA5",
                 "928B76 7B7669 625D60 756A6E", "777874 615F65 B5C2CC", "A29E96 797778 614D42 - 9A918A")
RINGNECK = palette("ringneck", "8EC76F 8FC97C 8DC777 8EC677", "96C972 97C26B 7EB366 6EAC6B 99BD67",
                   "81B865 78AD61 44785F 8AAC69", "6FA99A 478C98 A2B36F", "D65448 623D3F E8CA84 - 958B8B")
RAINBOW = palette("rainbow", "4E62B2 4C62A3 5268A8 475797", "EF883D 4E548D 5B9D5C 5DA760 819D53",
                  "73AF60 619E5B 377B76 EDAD49", "598B5B 7EA65C 95A856", "EB8050 C96348 D46646 - 8C8584")
RED_LORY = palette("red_lory", "DC5149 D44A47 D74C46 D34743", "DD5245 D64C48 C94943 C64845 CD4947",
                   "DC5147 C24748 373741 CE4847", "963D41 803C43 5369A5", "ED8856 CE6542 CD674B - 8A8584")
DUSKY = palette("dusky", "867345 B79656 736249 71583F", "EB883F E88543 776C58 DACEAD 946246",
                "705D49 685C4E 4A4742 D89645", "8C8754 8F915A A6A465", "DC794B C76643 D56E51 - 89817A")
BLUE_FRONT = palette("blue_front", "62A2BA D7BF55 D9C45E B0B363", "80AB62 86AF62 639156 68A76A 86AC60",
                     "D6654A 68975A 4E708C BA7454", "6CA664 7DA56C 9FA967", "665F5E 4A4949 D6914D E4DCC7 96938B")
YELLOW_HEAD = palette("yellow_head", "F2CE54 ECCB59 EECF65 E7C665", "81A75C 8DB462 668D55 6E9F60 DCC864",
                      "D9784D 73A363 4E7487 C07C4D", "70A16B 80AA70 A8B774", "CEB88E B29E81 D08B4A EBE3CD 97938B")
ORANGE_WING = palette("orange_wing", "E5C765 E8C86A E0C668 98AF7A", "88AB67 8AAE66 749461 75A169 8BA568",
                      "7FA367 70995F 467C91 D58A48", "72A36A 94AF66 B4BA73", "C4A981 685C54 D08A4F D4D1BD 928F86")
EASTERN = palette("eastern", "D95446 CD4D42 F2EBDD C94541", "D64F41 9FB967 729153 8EBA67 9DB363",
                  "5484B3 4A6DA0 3F5979 596B8F", "6F8B72 6297AB CE5149", "D4CBB3 B9AE9D 695341 - 92908C")
CRIMSON = palette("crimson", "CB4244 C94344 5B83B7 BD3F40", "C54645 B94344 AC5150 B04E48 B54649",
                  "6283B7 466FA3 3A4D72 485577", "4573A1 88B6C3 B74E49", "CDC9BE A9A69E 655145 - 888586")

PLUMAGES = {
    "jardines_parrot": {
        "gulielmi": JARDINE,
        "fantiensis": recolor(JARDINE, forehead="F49542", shoulder="E7924A", thigh="EB9746", mantle="57784B"),
        "massaicus": recolor(JARDINE, forehead="D65D3D", crown="426D4E", mantle="507751")},
    "bourkes_parrot": {
        "wild": BOURKE,
        "opaline": recolor(BOURKE, forehead="D69AA6", crown="D8A0AA", cheek="E6B2B1", throat="DFA1A7",
                           breast="E6A3AC", belly="E998AD", mantle="B89087", rump="91A2BF",
                           shoulder="BA948D", covert="998C82", thigh="DF9EAD"),
        "lutino": recolor(BOURKE, forehead="F3DEA3", crown="EACC8D", cheek="F5E5BE", throat="E8C1A5",
                          breast="E8C0A5", belly="EFA7A6", mantle="E1C283", rump="E6D6B3",
                          shoulder="E7D097", covert="E2C794", flight="F1E0BF", underwing="F4E3C1",
                          tail="EACFA0", tail_tip="EDDEBD", undertail="F2DDBC", bill="D5B6A2",
                          lower_bill="BFA393", foot="C69D96", iris="A76370"),
        "rubino": recolor(BOURKE, forehead="EDA8AE", crown="ECA5AC", cheek="F5C2BD", throat="EBABAD",
                          breast="F1ACB0", belly="EF99AB", mantle="DFAA9D", rump="E4C0BB",
                          shoulder="EDB1AE", covert="E4C0A3", flight="F1E1C7", underwing="F3DBBA",
                          tail="EBC2AE", tail_tip="F2DAC1", undertail="F1C8BF", bill="D5AF9D",
                          lower_bill="C09D93", foot="C7A098", iris="AB6370")},
    "rainbow_lorikeet": {"normal": RAINBOW},
    "red_lory": {"normal": RED_LORY},
    "dusky_lory": {"orange_band": DUSKY, "yellow_band": recolor(DUSKY, breast="E5BD56", belly="DEC16A",
                       underwing="D8AE53", crown="C7AE63", thigh="A89351")},
    "blue_fronted_amazon": {"aestiva": BLUE_FRONT, "xanthopteryx": recolor(BLUE_FRONT, shoulder="E7C964",
                       cheek="EDD068", crown="ECCE61", throat="C5BC69")},
    "yellow_headed_amazon": {"normal": YELLOW_HEAD},
    "orange_winged_amazon": {"normal": ORANGE_WING},
    "eastern_rosella": {"normal": EASTERN, "golden_mantled": recolor(EASTERN, mantle="E2BC53", rump="74AA92",
                       tail="65948B")},
    "crimson_rosella": {"crimson": CRIMSON,
        "yellow": recolor(CRIMSON, forehead="D9794C", crown="E8C95E", throat="E8C75C", breast="EDD067",
                          belly="E8CA64", mantle="CDB75D", rump="ADB06A", thigh="D9B95D", undertail="CBB466"),
        "adelaide": recolor(CRIMSON, forehead="D45D42", crown="D8874F", throat="D88F58", breast="DD8E56",
                            belly="D9A05E", mantle="C68F60", rump="AF9D61", thigh="D38B56", undertail="CD8053")},
    "senegal_parrot": {"senegalus": SENEGAL, "mesotypus": recolor(SENEGAL, breast="84AD62", belly="EDAB43",
                       rump="99AB58"), "versteri": recolor(SENEGAL, breast="669149", belly="E88743", mantle="517D49")},
}

# The grey and violet birds depicted here belong to the blue series.
# Body, underside, wing coverts, primaries, shoulder, central rectrices, nape ring.
RING_COLORS = {
    "green":     "91C970 98C773 70AC63 48755C 83B967 51969B DF9E9D",
    "blue":      "6FB9D4 83C5D5 5BA0C0 486F88 65ACC8 5689B0 D7E9EA",
    "lutino":    "F1D759 F3DE74 EBD26B ECE6BE EFDB71 EBE2AC E8A4A1",
    "albino":    "F2EFE8 F5F1E9 E6E4DD E1DED9 EEEBE5 E9E6E0 F1EEE8",
    "grey":      "A2AAB0 AEB6BA 929BA1 616A74 9AA2A7 727E89 E0E5E4",
    "turquoise": "71C1BA 7EC9BF 6CAFA6 4F8285 7DB6A1 538E9F C6DCD3",
    "violet":    "8587BB 9699C4 797BAD 565C89 8689B7 666F9E DDDCEA",
}


def ring_palette(variant):
    body, underside, covert, prim, shoulder, tip, band = RING_COLORS[variant].split()
    p = recolor(RINGNECK, forehead=body, crown=body, cheek=body, throat=body,
                breast=body, belly=underside, mantle=covert, rump=shoulder, thigh=underside,
                shoulder=shoulder, covert=covert, flight=prim, underwing=underside,
                tail=body, tail_tip=tip, undertail=underside)
    p["collar"] = color(band)
    if variant in {"lutino", "albino"}:
        p = recolor(p, pupil="864653", iris="B9817C", foot="C8A9A0", lower_bill="C97763")
    return p


PLUMAGES["rose_ringed_parakeet"] = {v: ring_palette(v) for v in RING_COLORS}


def feather_edges(u, v, columns=6, rows=7):
    row = int(v*rows)
    column = int(u*columns*2)+(row%2)
    return (int(v*rows*2)%2 == 1 and column%4 in (1, 2)) or (column%4 == 3 and row%2 == 0)


def head(p, face, u, v):
    style, variant = p["style"], p["variant"]
    side = face in {"east","west"}
    if style == "ringneck":
        # Chin stripe cuts up and back across the cheek; rose or pale nape starts behind it.
        if p["sex"] == "male" and variant != "albino":
            chin = color("F5E6B1") if variant == "lutino" else color("34363D")
            if side:
                line = .90 - .31*u
                if u < .65 and abs(v-line)<.092:
                    return chin
                if u >= .58 and abs(v-(.68+.09*u))<.092:
                    return p["collar"]
            if face == "front" and v > .81:
                return chin
            if face == "back" and .68 < v < .89:
                return p["collar"]
            if face == "bottom" and v < .40:
                return chin
        elif p["sex"]=="female" and variant != "albino" and face=="back" and .72<v<.86:
            return mix(p["crown"], p["belly"], .57)
        if side and u>.55 and v<.56 and variant in {"green","turquoise"}:
            return mix(p["crown"], p["tail_tip"], .19)
    if style in {"eastern","crimson"}:
        if side and .10<u<.75 and .45<v<(.90 if .23<u<.62 else .76):
            return p["cheek"]
        if face=="front" and v>.51 and (u<.17 or u>.83):
            return p["cheek"]
        if face=="front" and v<.26 or side and u<.22 and v<.27 or face=="top" and v>.8:
            return p["forehead"]
    if style=="jardine":
        limit = {"gulielmi":.46, "fantiensis":.53,"massaicus":.19}[variant]
        if face=="front" and v<.37 or side and u<limit and v<.29 or face=="top" and v>1-limit:
            return p["forehead"]
        if face=="back" or side and u>.54 and v<.5:
            if feather_edges(u,v,5,6):
                return mix(p["crown"],color("273B39"),.45)
    if style=="bourke":
        if p["sex"]=="male" and variant=="wild" and (face=="front" and .12<v<.40 or side and u<.45 and .16<v<.37 or face=="top" and v>.86):
            return p["forehead"]
        if side and .15<u<.7 and .30<v<.68 or face=="front" and v>.42:
            return p["cheek"]
    if style=="blue_front":
        if face=="front" and v<.68 or side and u<.32 and v<.53 or face=="top" and v>.72:
            return p["forehead"]
        if face=="back" and v>.29 or side and u>(.78 if variant=="xanthopteryx" else .60):
            return p["mantle"]
        if side or face=="front":
            return p["cheek"] if v<.75 else p["throat"]
    if style=="orange_wing":
        if face=="front" and .21<v<.53 or side and u<.39 and .30<v<.55:
            return color("637BA9")
        if face=="back" or side and u>.60:
            return p["mantle"]
    if style=="rainbow":
        if face=="back" and v>.69 or side and u>.69 and v>.71:
            return color("B5C258")
        if (side or face=="front") and (int(u*9+v*2)%3==0):
            return mix(p["crown"],color("5351A1"),.58)
    if style=="dusky":
        if face=="front" and v<.21 or side and v<.20 or face=="top":
            return p["crown"]
        if side and u<.23 and .47<v<.80:
            return mix(p["bill"],p["breast"],.48)
        return p["cheek"]
    if side and v>.64 and u<.78 or face=="front" and v>.55 or face=="bottom":
        return p["throat"]
    if side and .36<v<.68:
        return p["cheek"]
    return p["crown"]


def body(p, face, u, v):
    style = p["style"]
    side = face in {"east","west"}
    if face in {"back","top"} or side and u>.80:
        if (face=="back" or side) and v>.65:
            return p["rump"]
        if style in {"jardine","eastern","crimson"}:
            return p["mantle"] if feather_edges(u,v,5,7) else mix(p["mantle"],color("292C34"),.78)
        if style=="bourke" and feather_edges(u,v,4,6):
            return mix(p["mantle"],p["flight"],.50)
        return p["mantle"]
    if face=="bottom":
        return p["belly"]
    if style=="senegal":
        # Green bib ends in a point on the lower breast; orange/yellow flanks frame it.
        depth = .72 - .88*abs(u-.5) if face=="front" else .32-.33*u
        return p["breast"] if v<depth else p["belly"]
    if style=="rainbow":
        if side and u>.57:
            return p["mantle"]
        if v<.56:
            return p["breast"] if v>.16 or int(u*7)%3 else color("E8A248")
        return p["belly"]
    if style=="dusky":
        return p["breast"] if .08<v<.24 or .49<v<.66 else (p["belly"] if v>.69 else p["cheek"])
    if style=="eastern":
        if v < .31:
            return p["breast"]
        return color("EBC955") if v<.65 else p["belly"]
    if style=="bourke":
        if side and u>.64 and v>.64:
            return p["rump"]
        if v<.44 and feather_edges(u,v,5,7):
            return mix(p["breast"],p["mantle"],.34)
    if style in {"blue_front","yellow_head","orange_wing"} and v<.73 and feather_edges(u,v,5,7):
        return mix(p["breast"],p["mantle"],.53)
    return p["breast"] if v<.57 else p["belly"]


def wing_marks(p, part, face, u, v):
    style=p["style"]
    inner = face == ("east" if part.startswith("left") else "west")
    primary="primary" in part
    if inner and style=="bourke" and p["sex"]=="female" and .49<v<.64:
        return mix(p["underwing"],color("F4E6D4"),.77 if p["variant"]=="wild" else .24)
    if inner and style=="dusky" and (.19<v<.32 or .61<v<.78):
        return p["breast"]
    if not inner and face!="bottom":
        if style=="jardine" and not primary:
            if v<.23 and u<.57:
                return p["shoulder"]
            return p["covert"] if feather_edges(u,v,4,6) else mix(p["covert"],color("323839"),.72)
        if style in {"crimson","eastern"} and not primary:
            if u>.47 and v<.60:
                return p["mantle"] if feather_edges(u,v,4,6) else color("3F4048")
            return p["shoulder"] if v<.50 else p["covert"]
        if style=="bourke" and not primary:
            if feather_edges(u,v,4,6):
                edge = color("E1CEAB") if p["variant"] in {"wild","opaline"} else color("F4DCA6")
                return mix(p["covert"],edge,.70)
        if style=="red_lory" and ((not primary and u>.55 and v>.62) or primary and u>.66 and v<.27):
            return color("50659A")
        if style=="red_lory" and primary and .28<v<.59:
            return p["breast"]
        if style in {"yellow_head","blue_front","orange_wing"} and .54<v<.78 and u>.42 and not primary:
            return color("DE9045") if style=="orange_wing" else color("C75A45")
    return wing(p,part,face,u,v)


def surface(p,part,face,u,v,w,h):
    if part=="head":
        if face in {"east","west"}:
            eyelid=eye(p,u,v,w,h)
            if eyelid is not None:
                return eyelid
        return head(p,face,u,v)
    if part=="crown":
        if face=="top":
            return head(p,"top",u,v)
        return head(p,face,u,.10)
    if part=="body":
        return body(p,face,u,v)
    if part.startswith(("left_", "right_")):
        return wing_marks(p,part,face,u,v)
    if part in {"tail","centralTail","tip"}:
        if p["style"]=="red_lory" and part=="tail" and face=="bottom" and v<.46:
            return p["undertail"]
        return tail(p,part,face,u,v)
    if part=="thigh":
        return p["thigh"] if v<.67 else mix(p["thigh"],p["belly"],.15)
    if part=="tarsus":
        return p["toe_shadow"] if v>.67 or face=="bottom" else p["foot"]
    if part=="lowerBill":
        return p["lower_bill"]
    if part=="hook":
        return p["lower_bill"] if p["style"] in {"jardine","orange_wing"} else mix(p["bill"],p["lower_bill"],.42)
    if part=="upperBill":
        if p["style"] in {"jardine","orange_wing"} and (face=="front" and v>.46 or face in {"east","west"} and u<.3 and v>.38):
            return p["lower_bill"]
        return p["bill"] if face!="bottom" else mix(p["bill"],p["lower_bill"],.53)
    if part=="cere":
        if face=="front" and (u<.23 or u>.77):
            return mix(p["bill"],p["lower_bill"],.83)
        return mix(p["bill"],p["cheek"],.32)
    raise KeyError(part)


def main(species_names=None):
    spec=json.loads(Path(__file__).with_name("companion_shapes.json").read_text())
    for species, variants in spec["species"].items():
        if species_names is not None and species not in species_names:
            continue
        directory=ASSETS/species
        directory.mkdir(parents=True,exist_ok=True)
        for identifier in variants:
            if identifier.endswith(("_male","_female")):
                variant,sex=identifier.rsplit("_",1)
            else:
                variant,sex=identifier,None
            p=dict(PLUMAGES[species][variant], variant=variant, sex=sex)
            if species=="bourkes_parrot":
                if variant in {"lutino","rubino"}:
                    p["pupil"]=color("854551")
                if sex=="female" and variant=="wild":
                    for area in ("breast","belly","rump"):
                        p[area]=mix(p[area],color("CCC0B5"),.34)
            p["toe_shadow"]=mix(p["foot"],color("65535F"),.27)
            image=draw_atlas(spec["shapes"][species],spec["uv"],p)
            image.save(directory/(identifier+".png"),optimize=True)


if __name__=="__main__":
    main()
