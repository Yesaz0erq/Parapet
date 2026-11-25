"""Native 16px inventory sprites and materials for the mounted enrichment toys."""
from random import Random
from PIL import Image, ImageDraw
from paint_aviary_supplies import ITEMS, MATERIALS, painted_tile, save

# Hand-authored icon silhouettes. Each cell below is one original texture pixel.
SPRITES = {
    "beak_rubbing_perch": [
        "................","..........mmMM..",".........mHMmM..",".........mm.mm..",
        "........ootMm...",".......owwtm....","......owWWto....",".....otWTwto....",
        "....owwWTto.....","...otTwwto......","..oTWWtto.......",".otWWTto........",
        ".otTtto.........","..otto..........","...oo...........","................"],
    "mineral_chew_charm": [
        "......mMMm......","......m..M......","......mmMm......",".......tt.......",
        "......rRr.......",".......tt.......","....ppCCCpp.....","...pCPPcCCcp....",
        "..pCPCCcCcccp...",".pCCcCccPcccp...",".pCCCcccCccpp...",
        "..pCcpCCccpp....","...pcpcccpp.....","....pppppt......",".......tT.......","......t.t......."],
    "double_ring_swing": [
        ".......mMm......",".......m.m......","......ttMm......","....ttT.tt......",
        "...oTTTo.t......","..oT...Toht.....",".oT.....thHh....",".oT.....H..Hh...",
        ".ot....Th...hH..","..ot..Tth....Hh.","...otto.H....Hh.","........h....Hh.",
        "........hH..Hh..",".........hHHh...","..........hh....","................"],
    "large_chew_mobile": [
        ".......mMm......",".......m.m......",".......tt.......","......rRr.......",
        "...oWWtottTTo...","..oTttTTWWooo...","..t..t..t..t....","..t..t..t..t....",
        ".hHh.t.rRr.t....",".hHh.ttRRr.t....","..t.oTTo..hHh...","..r.tTTo..HHh...",
        "....tt....t.....","....rRr..rRr....","....rRr...r.....",".....t.........."],
}
PALETTE = {"m":"#675c50","M":"#bfa36e","H":"#83afa0","h":"#477a73","o":"#694e37","t":"#916941",
           "T":"#d8a763","w":"#a87547","W":"#ebc98b","r":"#9d5145","R":"#df8770","p":"#a98684",
           "P":"#f8efcf","C":"#e5d4c7","c":"#cfb6b2"}


def main():
    for name,rows in SPRITES.items():
        image=Image.new("RGBA",(16,16))
        for y,row in enumerate(rows):
            for x,letter in enumerate(row):
                if letter != ".": image.putpixel((x,y),tuple(bytes.fromhex(PALETTE[letter][1:]))+(255,))
        save(image,ITEMS,name)
    for tile,base,shadow,highlight,pattern in [
        ("worn_perch_wood","#bc9561","#a58150","#dcc291","grain"),
        ("toy_sisal","#bb965c","#a27a4b","#d9b677","weave"),
        ("toy_sea_rope","#67968c","#4f8077","#8fb4a1","weave"),
        ("toy_mineral","#decbc1","#ccb3b0","#f0dfd1",""),
    ]:
        im=painted_tile(tile,base,shadow,highlight,pattern)
        if tile=="toy_mineral":
            d=ImageDraw.Draw(im);r=Random(104)
            for _ in range(13):
                x,y=r.randrange(15),r.randrange(15)
                d.point((x,y),fill="#bb9fa0");d.point((x+1,y),fill="#f2ead8")
        save(im,MATERIALS,tile)


if __name__=="__main__":
    main()
