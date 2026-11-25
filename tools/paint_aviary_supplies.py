"""Paint native 16-pixel seed, sack, ceramic and toy textures, plus the repeating bath water strip."""

import json
import math
from pathlib import Path
from random import Random

from PIL import Image, ImageDraw

ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/parapet"
MATERIALS = ASSETS / "textures/block/aviary"
ITEMS = ASSETS / "textures/item"


def save(image, folder, name):
    folder.mkdir(parents=True, exist_ok=True)
    image.save(folder / (name + ".png"), optimize=True)


def kernel(draw, x, y, kind):
    patterns = {
        "millet": ((" .B", ".HL", "LS "), {"B":"#c99242", ".":"#e5b65e", "H":"#ffda7e", "L":"#c58a3e", "S":"#805432"}),
        "oat": (("  h", " hl", "hlb", "lb "), {"h":"#f5dba1", "l":"#d2ae72", "b":"#966b42"}),
        "green": ((" hh", "hms", "ms "), {"h":"#adc278", "m":"#80965a", "s":"#566944"}),
        "berry": ((" mm", "hmm", " sd"), {"h":"#f5a076", "m":"#ca675b", "s":"#a64e4b", "d":"#7e4141"}),
        "sunflower": (("  l", " lm", "lms", "sd "), {"l":"#d4c5a0", "m":"#60656a", "s":"#45474a", "d":"#333d3c"}),
    }
    rows, palette = patterns[kind]
    for dy, row in enumerate(rows):
        for dx, ink in enumerate(row):
            if ink in palette:
                draw.point((x + dx, y + dy), fill=palette[ink])


def loose_feed(premium):
    image = Image.new("RGBA", (16, 16))
    draw = ImageDraw.Draw(image)
    outline = "#75583a" if not premium else "#654837"
    draw.polygon([(1,11),(2,9),(4,8),(5,7),(8,6),(10,6),(11,7),
                  (14,8),(14,10),(15,11),(13,13),(9,14),(6,14),(3,13),(1,13)],fill=outline)
    draw.polygon([(2,11),(4,9),(7,7),(10,7),(13,9),(14,11),(11,13),(7,13),(4,12)],fill="#b28c51")
    grains = [(5,5,"oat"),(8,5,"millet"),(3,7,"millet"),(11,7,"oat"),
              (5,8,"oat"),(8,8,"green"),(2,10,"oat"),(5,11,"millet"),
              (10,10,"millet"),(12,9,"green"),(8,11,"oat")]
    if premium:
        grains = [(6,4,"sunflower"),(9,5,"green"),(4,6,"millet"),(11,7,"berry"),
                  (6,7,"oat"),(2,8,"green"),(4,9,"berry"),(8,7,"sunflower"),
                  (12,9,"millet"),(9,10,"berry"),(6,11,"millet")]
    for x,y,kind in grains:
        kernel(draw,x,y,kind)
    kernel(draw,1,4,"sunflower" if premium else "millet")
    draw.point((13,5),fill="#d98c6e" if premium else "#d6b978")
    return image


def bag(premium):
    img = Image.new("RGBA",(16,16))
    d = ImageDraw.Draw(img)
    fabric = ("#273d35","#385948","#527762","#71937b") if premium else ("#62492f","#967346","#b2915e","#d4b582")
    shadow, dark, mid, light = fabric
    d.polygon([(4,0),(7,1),(9,0),(12,1),(11,4),(10,5),(12,6),(13,7),(14,10),(14,13),
               (13,14),(11,15),(4,15),(2,14),(1,12),(2,8),(4,6),(5,5)],fill=shadow)
    d.polygon([(4,1),(7,2),(9,1),(11,1),(10,4),(5,4)],fill=mid)
    d.line((6,2,6,3),fill=light);d.line((9,1,9,3),fill=dark)
    d.polygon([(5,6),(10,6),(12,7),(13,10),(13,13),(11,14),(4,14),(2,12),(3,8)],fill=mid)
    d.line([(3,9),(4,7),(6,6)],fill=light);d.line([(3,10),(3,12),(5,13)],fill=light)
    d.line([(12,8),(12,12),(11,13),(5,14)],fill=dark)
    d.rectangle((5,4,10,5),fill="#88613a");d.line((5,4,10,4),fill="#edce86")
    d.line([(10,5),(12,5),(13,6)],fill="#b68b54")
    d.rectangle((5,8,10,12),fill="#725938");d.rectangle((5,8,10,11),fill="#eed7a1")
    if premium:
        d.point((8,8),fill="#5b7d4c");d.rectangle((7,9,9,10),fill="#ad5344");d.point((8,11),fill="#a35141")
    else:
        d.line((6,11,9,8),fill="#916638");d.point((7,9),fill="#bc8b43");d.point((9,10),fill="#bc8b43")
    return img


def painted_tile(name, base, shade, highlight, pattern):
    img = Image.new("RGB",(16,16),base)
    d = ImageDraw.Draw(img)
    rng=Random(name)
    for _ in range(30):
        x,y=rng.randrange(16),rng.randrange(16)
        d.line((x,y,min(15,x+rng.choice((0,0,1))),y), fill=rng.choice((shade,highlight)))
    if pattern=="weave":
        for y in range(0,16,4):
            for x in range((y//4)%2,16,4):
                d.line((x,y,x+1,y),fill=highlight)
        for x in (2,10):
            for y in (2,10):
                d.line((x,y,x,y+1),fill=shade)
    if pattern=="grain":
        for x,y,length in [(0,3,4),(6,4,2),(10,2,6),(2,11,6),(12,10,4),(6,14,3)]:
            for i in range(length):
                d.point(((x+i)%16,y),fill=shade if y%2 else highlight)
    return img


def label(premium):
    img=Image.new("RGB",(16,16),"#dbbe83" if premium else "#cfb17b");d=ImageDraw.Draw(img)
    d.rectangle((3,4,12,11),fill="#87643f")
    d.rectangle((4,5,11,10),fill="#eed8a5")
    if premium:
        d.line([(7,7),(8,6),(10,5)],fill="#577548")
        d.rectangle((7,7,9,8),fill="#b9544b");d.point((8,7),fill="#d87d65");d.point((7,9),fill="#a84c42")
    else:
        d.line((6,9,9,6),fill="#89623a");d.point((6,7),fill="#b88743");d.point((8,8),fill="#b88743")
    return img


def seed_coats():
    """Five small, opaque swatches for the tops and sides of individual 3D kernels."""
    im = Image.new("RGB", (16, 16), "#b38b52")
    d = ImageDraw.Draw(im)
    colors = [("#8c693e","#bf9651","#efcf81"),("#9a7951","#ceb388","#f5deae"),
              ("#536340","#90a16a","#bcbe87"),("#4a4243","#54565a","#c4b8a2"),
              ("#864645","#bf6255","#e59274")]
    for i, (shade,body,light) in enumerate(colors):
        x,y=(i%4)*4,(i//4)*8
        d.rectangle((x,y,x+3,y+3),fill=body)
        d.line((x+1,y,x+2,y),fill=light)
        d.point((x,y+1),fill=light)
        d.line((x+3,y+2,x+3,y+3),fill=shade)
        if i==3:
            d.line((x+1,y,x+1,y+3),fill=light)
        d.rectangle((x,y+4,x+3,y+7),fill=shade)
        d.line((x+1,y+4,x+2,y+4),fill=body)
    return im


def water():
    # Both wave phases wrap over the same 16 × 16 domain for every tile of the large bath.
    count=12
    strip=Image.new("RGB",(16,16*count))
    palette=["#386f85","#407e94","#488ba0","#5497aa","#68aaba","#87c3cb"]
    for frame in range(count):
        phase=math.tau*frame/count
        im=Image.new("RGB",(16,16))
        for y in range(16):
            for x in range(16):
                u,v=math.tau*x/16, math.tau*y/16
                wave=math.sin(2*v+0.60*math.sin(u-phase)+phase)
                glint=math.cos(u+0.6*math.cos(v+phase)-phase)
                tone=2 if wave>-.4 else 1
                if wave<-.84 and glint<.35: tone=0
                if wave>.28: tone=3
                if wave>.70 and glint>.16: tone=4
                if wave>.92 and glint>.64: tone=5
                im.putpixel((x,y),tuple(bytes.fromhex(palette[tone][1:])))
        strip.paste(im,(0,16*frame))
    return strip


def main():
    save(seed_coats(), MATERIALS, "seed_coats")
    for premium, prefix in ((False,""),(True,"premium_")):
        save(loose_feed(premium),ITEMS,prefix+"bird_feed")
        save(bag(premium),ITEMS,prefix+"bird_feed_bag")
        save(label(premium),MATERIALS,prefix+"feed_seal")
        name=prefix+"seed_mix"
        img=painted_tile(name,"#b58d49","#99713f","#d6ac63","")
        d=ImageDraw.Draw(img)
        for x,y,k in [(0,0,"oat"),(5,0,"millet"),(10,1,"green"),(13,5,"oat"),(2,6,"millet"),
                      (6,5,"oat"),(10,8,"millet"),(0,12,"green"),(5,11,"millet"),(13,12,"millet")]:
            kernel(d,x,y,k)
        if premium:
            for x,y,k in [(2,2,"berry"),(8,3,"sunflower"),(0,8,"sunflower"),(11,11,"berry"),(6,11,"green")]:
                kernel(d,x,y,k)
        save(img,MATERIALS,name)
    for args in [
        ("feed_jute","#ad8c5d","#9e7e51","#bc9f71","weave"),
        ("feed_moss_canvas","#537c65","#466b58","#648b73","weave"),
        ("bath_celadon","#72a698","#689c8d","#88b5a3",""),
        ("bath_terracotta","#ad7155","#9d6049","#bd8462",""),
        ("bowl_porcelain","#e3dcc7","#d3cbb6","#eee8d6",""),
        ("bowl_blue_enamel","#536e86","#49637a","#68899b",""),
        ("toy_rosewood","#af6552","#935242","#c37d65","grain"),
        ("toy_honeywood","#c69852","#ae7f42","#dfb16c","grain"),
        ("toy_tealwood","#578784","#447572","#72a29a","grain"),
    ]:
        save(painted_tile(*args),MATERIALS,args[0])
    save(water(),MATERIALS,"bath_ripples")
    (MATERIALS/"bath_ripples.png.mcmeta").write_text(json.dumps({
        "animation":{"width":16,"height":16,"frametime":4}
    },indent=2)+"\n",encoding="utf-8")


if __name__=="__main__":
    main()
