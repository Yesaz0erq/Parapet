"""Paint the field guide, brass interaction panel, control glyphs and inventory book at native GUI resolution."""
from pathlib import Path
from random import Random
from PIL import Image, ImageDraw

ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/parapet"
GUI = ASSETS / "textures/gui"
GUI.mkdir(parents=True, exist_ok=True)
OUTLINE, IRON, CREAM = "#28282b", "#40535a", "#f2e2ad"
BRASS = ("#513e31", "#886538", "#ae864a", "#c6a060", "#e1bf7b", "#f1d89c")
LEATHER = ("#3f302a", "#624436", "#845840", "#a77850")
PAPER = ("#ad8856", "#bb9868", "#d4b88a", "#e0c79b", "#e7d1a6", "#ecdbb5", "#f0dfba")
INK = ("#19282b", "#233a3d", "#2b4448", "#345055", "#426160", "#627b72")


def new(size):
    im = Image.new("RGBA", size)
    return im, ImageDraw.Draw(im)


def corners(d, box, radius, fill):
    x, y, X, Y = box
    d.polygon([(x + radius,y),(X-radius,y),(X,y+radius),(X,Y-radius),(X-radius,Y),
               (x+radius,Y),(x,Y-radius),(x,y+radius)], fill=fill)


def bevel(d, box, light, dark):
    x, y, X, Y = box
    d.line([(x,Y-1),(x,y),(X-1,y)],fill=light)
    d.line([(X,y+1),(X,Y),(x+1,Y)],fill=dark)


def speckle(d, box, colors, count, seed, length=1):
    r=Random(seed)
    for _ in range(count):
        x=r.randint(box[0],box[2]); y=r.randint(box[1],box[3])
        d.line((x,y,min(x+r.randrange(length),box[2]),y),fill=r.choice(colors))


def rivet(d,x,y):
    d.rectangle((x,y,x+4,y+4),fill=BRASS[0]); d.rectangle((x+1,y,x+3,y+3),fill=BRASS[3])
    d.line((x+1,y+1,x+2,y+1),fill=BRASS[5]); d.point((x+2,y+2),fill=BRASS[0])


def stamps(d,x,y,pattern,palette,scale=1):
    for j,line in enumerate(pattern):
        for i,ch in enumerate(line):
            if ch not in palette: continue
            box=(x+i*scale,y+j*scale,x+(i+1)*scale-1,y+(j+1)*scale-1)
            d.rectangle(box,fill=palette[ch])


FEATHER=[".........22.",".......2332.","......2342..",".....23332..","....23232...","...232332...","...32332....",
         "..22322.....","..2332......","..232.......",".22.........","1..........."]


def paper(d, box, mirrored, seed, bare=False):
    x,y,X,Y=box
    d.rectangle(box,fill=PAPER[5])
    speckle(d,(x+3,y+3,X-3,Y-3),("#e8d4af","#eddbb7","#efe0bd","#ebd8b2"),(X-x)*(Y-y)//30,seed,3)
    # Vellum darkens where the leaves bend into the binding.
    for i,col in enumerate((PAPER[1],PAPER[2],PAPER[2],PAPER[3],PAPER[3],PAPER[4],PAPER[4],PAPER[4])):
        spine=x+i if mirrored else X-i
        d.line((spine,y,spine,Y),fill=col)
    edge=X if mirrored else x
    d.line((edge,y+2,edge,Y-2),fill=PAPER[3])
    if not bare:
        for dx,dy in ((1,1),(-1,1),(1,-1),(-1,-1)):
            ax=x+6 if dx>0 else X-6; ay=y+5 if dy>0 else Y-5
            d.line((ax,ay,ax+dx*8,ay),fill=PAPER[2]); d.line((ax,ay,ax,ay+dy*4),fill=PAPER[2])
            d.point((ax+dx*2,ay+dy*2),fill=PAPER[2])


def guide():
    im,d=new((320,200))
    corners(d,(0,4,319,196),3,OUTLINE)
    corners(d,(1,3,318,194),3,LEATHER[1])
    corners(d,(3,2,316,191),2,LEATHER[2])
    d.line((6,4,313,4),fill=LEATHER[3]);d.line((4,6,4,188),fill=LEATHER[3])
    for x in range(8,313,5): d.line((x,191,x+1,191),fill="#bc905d")
    for x1,x2 in [(5,156),(163,314)]:
        corners(d,(x1,7,x2,188),2,PAPER[0])
        for j,col in enumerate((PAPER[2],PAPER[1],PAPER[3],PAPER[2],PAPER[4])):
            d.line((x1+3,185-j,x2-2,185-j),fill=col)
    # Two woven markers protrude beyond the leaves.
    d.polygon([(172,177),(182,177),(182,199),(177,195),(172,199)],fill="#6e3d32")
    d.polygon([(174,179),(180,179),(180,196),(177,193),(174,196)],fill="#b36b46")
    d.line((175,184,175,193),fill="#d08b58")
    d.polygon([(135,181),(140,181),(140,196),(138,194),(135,196)],fill="#476d62")
    paper(d,(8,8,157,181),False,12);paper(d,(162,8,311,181),True,199)
    for x,col in [(158,PAPER[1]),(159,"#856a4e"),(160,"#957657"),(161,PAPER[1])]:
        d.line((x,10,x,181),fill=col)
    for y in (21,54,137,170):
        d.line((157,y,160,y+1),fill="#947955");d.line((159,y+2,162,y),fill=PAPER[3])
    for x,step in ((1,1),(318,-1)):
        d.line((x,11,x,5,x+3*step,2,x+11*step,2),fill=BRASS[4],width=2)
        d.line((x,185,x,190,x+4*step,194,x+11*step,194),fill=BRASS[2],width=2)
    im.save(GUI/"field_guide.png",optimize=True)
    leaf,ld=new((144,168));paper(ld,(0,0,143,167),True,71,True)
    ld.line((1,0,143,0),fill=PAPER[6]);ld.line((1,167,143,167),fill=PAPER[1])
    leaf.save(GUI/"page_leaf.png",optimize=True)


def panel():
    im,d=new((256,192))
    corners(d,(1,2,255,191),5,"#171c1f");corners(d,(0,0,254,189),5,OUTLINE)
    corners(d,(1,1,253,188),4,BRASS[2]);corners(d,(3,3,251,186),3,LEATHER[0])
    corners(d,(4,4,250,185),3,LEATHER[2])
    speckle(d,(5,5,249,184),(LEATHER[1],LEATHER[3]),730,93,5)
    corners(d,(8,7,247,182),3,BRASS[1]);corners(d,(9,8,246,181),2,INK[0]);d.rectangle((11,10,244,179),fill=INK[2])
    d.rectangle((12,11,243,75),fill=INK[1]);d.line((13,12,242,12),fill="#304c4c")
    speckle(d,(12,12,242,74),("#273f40","#263d3e","#293f3f"),128,30)
    d.line((12,79,243,79),fill=INK[0]);d.line((12,80,243,80),fill=BRASS[1]);d.line((12,81,243,81),fill=INK[4])
    d.rectangle((12,156,243,177),fill=INK[1]);d.line((13,156,242,156),fill=INK[0])
    # A stamped bird with a hooked upper bill on the motor-green header plate.
    seal=[".......BBBB.....","......BAAAAB....",".....BAABACCC...",".....BAAAB.C....","....BAAAAB......","...BAADDB.......",
          "..BAADDBB.......",".BAADDB.B.......",".BAADB..B.......","..BDB..BB.......","..BB...BB.......",".B...BBBB......."]
    stamps(d,187,23,seal,{"A":"#3a524f","B":"#314a45","C":"#526054","D":"#476157"},2)
    for x,y in ((3,4),(247,4),(3,180),(247,180)):rivet(d,x,y)
    for x in (20,62,187,229):
        d.line((x,185,x+7,185),fill=BRASS[2]);d.point((x,184),fill=BRASS[4])
    im.save(GUI/"interaction_panel.png",optimize=True)
    im,d=new((109,57))
    for i,(rim,face,hi,low,accent) in enumerate([
        (INK[0],"#45625d","#91a590","#293f3d",BRASS[3]),
        (BRASS[3],"#587970","#c4caa1","#334c42",BRASS[5]),
        ("#283537","#384446","#526061","#293439","#6d6b5b")]):
        y=i*19
        corners(d,(0,y,108,y+18),2,rim)
        d.rectangle((2,y+2,106,y+16),fill=face)
        bevel(d,(1,y+1,107,y+17),hi,low)
        d.line((2,y+2,2,y+15),fill=accent)
        d.line((105,y+3,105,y+14),fill=low)
    im.save(GUI/"interaction_buttons.png",optimize=True)


def icons():
    patterns=[
        ["....2.2.....","..2.3.3.2...","..3232323...","..3333333...","2.3333333...","233333333...","..3333332...",
         "...33332....","....222.....",".....11....."],
        ["..........2.","..22..22.2..",".23322332...",".23333332...","..233332....","...2332.....","....22......",
         "......344...",".2222344....",".344442....."],
        ["......222...",".....23232..",".....22222..","......22....","..222222....",".23333332...",
         ".2332222....","..332.......","..222......."],
        FEATHER,
        ["..2....2....",".232..232...","..2.22.2....","...2..2.....",".22....22...",".332..233...",
         ".33322333...","..332233....","...32.32....","...22.22...."],
        ["....2222....","..22333322..",".2332222332.",".232....232.",".22......32.","........232.",
         "...2...232..","..2322232...",".2333332....","..232.......","...2........"],
        ["....2222....","...23..32...","...23..32...","...22..22...","..22222222..","..23333332..",
         "..23322332..","..23322332..","..23333332..","..22222222.."]]
    im,d=new((84,12))
    for i,p in enumerate(patterns):
        stamps(d,i*12,0,p,{"1":"#719489","2":"#bd9859","3":"#f3d68e","4":"#8eb8a1"})
    im.save(GUI/"interaction_icons.png",optimize=True)


def item():
    im,d=new((16,16))
    d.polygon([(2,2),(10,0),(13,1),(15,12),(5,15),(2,13),(0,4)],fill=OUTLINE)
    d.polygon([(2,3),(11,1),(13,2),(14,11),(5,14),(2,13),(1,4)],fill=LEATHER[2])
    d.polygon([(3,10),(13,8),(14,11),(5,14),(3,13)],fill=PAPER[2]);d.line((5,12,13,10),fill=PAPER[4])
    d.polygon([(1,3),(10,1),(12,2),(13,9),(4,12),(1,11)],fill="#365f53")
    d.line((2,3,3,10,4,11),fill="#6c8b6c");d.line((4,11,13,9),fill="#244238")
    d.line((4,3,11,2),fill="#598064");d.line((3,4,5,10),fill="#9a7947")
    for x,y in [(7,5),(8,4),(9,4),(9,5),(8,5),(8,6),(7,6),(7,7),(6,8)]:d.point((x,y),fill="#e6bc67")
    d.point((8,5),fill="#f8df9b");d.point((7,6),fill="#b58c4f");d.line((10,12,10,14),fill="#ae5741")
    p=ASSETS/"textures/item/parrot_field_guide.png";p.parent.mkdir(exist_ok=True);im.save(p,optimize=True)


if __name__ == "__main__":
    guide()
    panel()
    icons()
    item()
