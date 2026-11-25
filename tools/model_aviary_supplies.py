"""Author 16px aviary models. Run after paint_aviary_supplies.py, then export_aviary_furniture.py.

Existing sculpted furniture is edited in place; enrichment parts and all supply meshes
have stable names and IDs. Embedded textures make the Blockbench projects portable.
"""
import base64
import json
import math
import struct
import uuid
from itertools import product
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art/furniture"
TILES = ROOT / "src/main/resources/assets/parapet/textures/block/aviary"
SIDES = ("north", "south", "east", "west", "up", "down")


def identifier(*parts):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "parapet/aviary/" + "/".join(parts)))


class Project:
    def __init__(self, name, preserve=False):
        self.name = name
        self.path = ART / (name + ".bbmodel")
        if preserve:
            self.data = json.loads(self.path.read_text())
        else:
            self.data = {
                "meta": {"format_version": "5.0", "model_format": "java_block", "box_uv": False},
                "name": "parapet_" + name, "parent": "block/block", "java_block_version": "26.3",
                "ambientocclusion": False, "resolution": {"width": 16, "height": 16},
                "elements": [], "outliner": [], "groups": [], "textures": [],
            }

    def remove(self, predicate):
        parts = self.data["elements"]
        removed = {p["uuid"] for p in parts if predicate(p["name"])}
        self.data["elements"] = [p for p in parts if p["uuid"] not in removed]
        for group in self.data["outliner"]:
            group["children"] = [i for i in group["children"] if i not in removed]

    def group(self, name):
        for group in self.data["groups"]:
            if group["name"] == name:
                return next(g for g in self.data["outliner"] if g["uuid"] == group["uuid"])
        key = identifier(self.name, "group", name)
        self.data["groups"].append({"name": name, "uuid": key, "origin": [0,0,0], "color": 0})
        out = {"uuid": key, "isOpen": False, "children": []}
        self.data["outliner"].append(out)
        return out

    def texture(self, name):
        textures = self.data["textures"]
        filename = name + ".png"
        for i, t in enumerate(textures):
            if t["name"] == filename:
                return i
        index = len(textures)
        textures.append({"name": filename, "id": str(index), "uuid": identifier(self.name, "tile", name)})
        return index

    def box(self, group, name, lo, hi, tile):
        x,y,z = lo
        X,Y,Z = hi
        # Each small face samples actual texels instead of shrinking a 16px sheet onto every grain.
        sizes = [(X-x,Y-y), (X-x,Y-y), (Z-z,Y-y), (Z-z,Y-y), (X-x,Z-z), (X-x,Z-z)]
        tile_index = self.texture(tile)
        faces = {}
        seed = sum((i+1)*ord(c) for i,c in enumerate(name))
        for i, (side,(w,h)) in enumerate(zip(SIDES,sizes)):
            u = (seed + i*3) % max(1,1+int(16-w))
            v = (seed//7 + i*5) % max(1,1+int(16-h))
            faces[side] = {"uv": [u,v,round(u+w,3),round(v+h,3)], "texture": tile_index}
        key = identifier(self.name, group, name)
        part = {"name": name, "uuid": key, "type": "cube", "box_uv": False,
                "from": list(lo), "to": list(hi), "autouv": 0, "origin": [0,0,0], "faces": faces}
        self.data["elements"].append(part)
        self.group(group)["children"].append(key)
        return part

    def save(self):
        textures = self.data["textures"]
        used = sorted({face["texture"] for e in self.data["elements"] for face in e["faces"].values()
                       if face.get("texture") is not None})
        index = {old: new for new,old in enumerate(used)}
        self.data["textures"] = [textures[i] for i in used]
        for e in self.data["elements"]:
            for face in e["faces"].values():
                if face.get("texture") is not None:
                    face["texture"] = index[face["texture"]]
        for i,t in enumerate(self.data["textures"]):
            png = (TILES/t["name"]).read_bytes()
            width,height = struct.unpack(">II",png[16:24])
            t.update(id=str(i), folder="block/aviary", namespace="parapet", path="", internal=True,
                     width=width,height=height,uv_width=16,uv_height=16, frame_time=4 if height>16 else 1,
                     source="data:image/png;base64,"+base64.b64encode(png).decode())
            t.pop("relative_path",None)
        self.path.write_text(json.dumps(self.data,ensure_ascii=False,separators=(",",":"))+"\n")


# Position, footprint and material. Seeds are scattered first, then overlap in stepped layers.
PORTIONS = [
    [(4.0,5.4,2.1,1.0,"oat"),(6.9,8.8,1.4,1.6,"millet"),(9.4,6.6,1.2,2.2,"dark"),
     (6.0,6.1,1.7,1.2,"millet"),(10.9,10.2,1.5,1.0,"green")],
    [(3.3,8.9,1.1,1.7,"millet"),(8.4,10.1,1.4,1.8,"oat"),(10.9,5,1.8,1.0,"fruit"),
     (7.1,4.4,1.9,1.0,"millet"),(5.1,10.4,1.8,1.5,"green")],
    [(2.2,6.8,1.4,1.1,"oat"),(7.9,12.0,1.8,1.0,"dark"),(11.3,7.9,2.0,1.1,"millet"),
     (4.2,3.7,1.4,1.7,"green"),(6.1,6.5,1.4,1.6,"fruit")],
    [(2.2,10.6,1.4,1.0,"dark"),(10.9,12,1.8,1.1,"oat"),(13.0,6.2,1.3,1.6,"fruit"),
     (9.1,3.1,1.4,1.4,"green"),(4.0,11.8,1.4,1.7,"millet")],
]
MOUNDS = [
    [],
    [(5.65,5.8,9.75,7.15,.70),(4.75,7.15,10.55,8.8,.70),(6.1,8.8,10,9.85,.70)],
    [(4.95,5.25,8.95,6.8,1.02),(4.48,6.8,10.8,8.45,1.02),
     (5.50,8.45,11.28,9.82,1.02),(6.84,9.82,9.65,10.48,1.02)],
    [(6.4,5.88,8.65,7.25,1.67),(5.85,7.25,10.05,8.62,1.67),(7.20,8.62,9.56,9.65,1.67)],
]


def grain_model(name, premium, held):
    model = Project(name)
    mix = "premium_seed_mix" if premium else "seed_mix"
    palette = {"millet":0, "oat":1, "green":2, "dark":3 if premium else 0, "fruit":4 if premium else 1}
    for tier,grains in enumerate(PORTIONS):
        group = "frame" if held else f"portion_{tier+1}"
        lift = 6 if held else 0
        dy = 1.35 if held else 1
        def box(label,x,y,z,X,Y,Z,tile):
            return model.box(group,label,(x,round(lift+y*dy,3),z),(X,round(lift+Y*dy,3),Z),tile)
        def kernel(label,x,y,z,w,h,d,kind):
            part=box(label,x,y,z,round(x+w,3),round(y+h,3),round(z+d,3),"seed_coats")
            swatch=palette[kind]
            u,v=(swatch%4)*4,(swatch//4)*8
            for face in part["faces"].values():
                face["uv"]=[u,v+4,u+3,v+5.8]
            part["faces"]["up"]["uv"]=[u,v,u+4,v+4]
        for i,(x,z,X,Z,top) in enumerate(MOUNDS[tier]):
            base = 1.01 if tier==3 else .04 if tier==2 else 0
            part=box(f"grain_bed_{tier}_{i}",x,base,z,X,top,Z,mix)
            part["faces"]["up"]["uv"]=[x,z,X,Z]
        for i,(x,z,w,d,material) in enumerate(grains):
            kernel(f"kernel_{tier}_{i}",x,0,z,w,(.5,.6,.57)[i%3],d,material)
        if MOUNDS[tier]:
            for i,(x,z,kind) in enumerate([(6.50,6.15,"oat"),(8.4,7.63,"dark"),(7.34,8.56,"fruit")]):
                kernel(f"top_kernel_{tier}_{i}",x+.25*(tier%2),MOUNDS[tier][0][4],z-.12*(tier%2),
                       1.21 if i==1 else 1.62,.40,1.50 if i==1 else .85,kind)
    model.save()


def sack(name,premium):
    model = Project(name)
    fabric = "feed_moss_canvas" if premium else "feed_jute"
    seal = "premium_feed_seal" if premium else "feed_seal"
    for label,lo,hi in [
        ("bottom_fold",(4.0,1.0,5.15),(12,2.35,10.85)), ("belly",(3.3,2.35,4.35),(12.7,8.65,11.65)),
        ("lower_gusset",(3.65,1.58,4.72),(12.35,2.35,11.28)),
        ("shoulders",(3.92,8.65,4.86),(12.08,10.12,11.14)),("top_fold",(4.9,10.12,5.7),(11.1,11.24,10.3)),
        ("gathered_neck",(6.0,11.24,6.45),(10,12.85,9.55)),("flared_cuff",(5.35,12.85,5.98),(10.65,14.16,10.02)),
        ("cuff_left",(5.3,14.16,6.44),(7.33,14.65,9.7)),("cuff_right",(9.0,14.16,6.1),(10.66,14.52,9.23)),
    ]: model.box("frame",label,lo,hi,fabric)
    for label,lo,hi in [
        ("tie",(5.77,11.67,6.17),(10.23,12.35,9.83)),("tie_knot",(9.96,11.53,6.12),(10.78,12.41,7.46)),
        ("tie_tail",(10.4,10.52,6.55),(10.95,11.56,7.12))]:
        model.box("frame",label,lo,hi,"twine")
    label = model.box("frame","woven_label",(5.05,3.55,4.22),(10.95,7.70,4.36),seal)
    label["faces"]["north"]["uv"] = [3,4,13,12]
    model.save()


def refinished_bath(name,large):
    model = Project(name,True)
    model.remove(lambda name:name.startswith("detail_"))
    tile_indices = {model.texture("red_clay"):model.texture("bath_terracotta"),
                    model.texture("glaze"):model.texture("bath_celadon")}
    water = model.texture("bath_ripples")
    for e in model.data["elements"]:
        if e["name"].startswith("water_"):
            x,_,z=e["from"]; X,_,Z=e["to"]
            ox=16*math.floor((x+X)/32); oz=16*math.floor((z+Z)/32)
            e["faces"]={side:{"uv":[0,0,0,0],"texture":None} for side in SIDES}
            e["faces"]["up"]={"uv":[round(x-ox,3),round(z-oz,3),round(X-ox,3),round(Z-oz,3)],"texture":water}
        else:
            for f in e["faces"].values():
                if f.get("texture") in tile_indices:
                    f["texture"]=tile_indices[f["texture"]]
    def detail(name,lo,hi,tile):
        return model.box("frame","detail_"+name,lo,hi,tile)
    if large:
        # The original submerged step remains. A smaller dry landing and inset nosing
        # sit on it. Split at each 16-unit cell before the multipart exporter runs.
        for i,(x,X) in enumerate([(-3.9,0),(0,16),(16,19.9)]):
            detail(f"upper_landing_{i}",(x,4.7,24.65),(X,5.94,28.35),"bath_terracotta")
            detail(f"landing_nose_{i}",(x,5.94,24.6),(X,6.18,25.46),"bath_celadon")
        for i,x in enumerate((-2.8,3.7,9.7,17.3)):
            detail(f"traction_{i}",(x,5.94,26.2),(x+1.45,6.14,27.7),"ivory")
    else:
        detail("low_step",(4.18,2.68,9.38),(11.82,3.81,13.47),"bath_terracotta")
        detail("perching_lip",(4.87,3.81,11.38),(11.13,5.27,13.47),"bath_celadon")
        for i,x in enumerate((5.5,7.67,9.84)):
            detail(f"grip_{i}",(x,5.27,11.9),(x+.65,5.44,12.98),"ivory")
    model.save()


def chew_toy():
    model=Project("chew_blocks",True)
    model.remove(lambda name:name.startswith(("moving_chew","detail_")))
    def part(name,lo,hi,tile):
        return model.box("moving","detail_"+name,lo,hi,tile)
    # Alternating profiles and bite notches read in silhouette while the
    # unchanged cord, knots and arm retain their original animation pivot.
    part("rose_back",(4.15,3.02,8.45),(8.15,5.05,10.94),"toy_rosewood")
    part("rose_front",(4.15,3.02,7.59),(6.72,5.05,8.45),"toy_rosewood")
    part("honey_middle",(3.95,6.27,7.94),(8.55,8.15,10.85),"toy_honeywood")
    part("honey_front",(4.7,6.27,7.15),(7.8,8.15,7.94),"toy_honeywood")
    part("honey_back",(4.7,6.27,10.85),(7.8,8.15,11.62),"toy_honeywood")
    part("teal_middle",(4.45,9.4,7.61),(7.73,11.35,10.7),"toy_tealwood")
    part("teal_wing_left",(3.76,9.79,8.03),(4.45,10.98,10.32),"toy_tealwood")
    part("teal_wing_right",(7.73,9.79,8.03),(8.42,10.98,10.32),"toy_tealwood")
    part("rope_fringe_left",(5.43,1.52,8.86),(5.95,2.45,9.38),"twine")
    part("rope_fringe_right",(6.1,1.67,9.29),(6.6,2.45,9.9),"twine")
    model.save()


def water_bowl():
    model = Project("water_bowl")
    model.data["display"] = {
        "gui":{"rotation":[35,32,0],"translation":[0,4.15,0],"scale":[1.12,1.12,1.12]},
        "fixed":{"rotation":[26,180,0],"translation":[0,3.25,0],"scale":[.88,.88,.88]},
        "thirdperson_righthand":{"rotation":[50,32,0],"translation":[0,3.6,0],"scale":[.50,.50,.50]},
        "firstperson_righthand":{"rotation":[14,38,0],"translation":[0,2.25,0],"scale":[.60,.60,.60]},
        "firstperson_lefthand":{"rotation":[14,218,0],"translation":[0,2.25,0],"scale":[.60,.60,.60]},
    }
    # Each row gives its outer and inner half-width. Mirroring about Z=8
    # produces a stepped oval with a real cavity and a continuous liquid surface.
    front = [(3.1,4.05,3.05,0), (4.05,4.9,4.28,2.70), (4.9,5.8,5.2,3.95)]
    rows = front + [(5.8,10.2,5.8,4.78)] + [
        (16-z1,16-z0,outer,inner) for z0,z1,outer,inner in reversed(front)]
    def layer(label, scale, bottom, top, tile, mode):
        def coord(v):
            return round(8+(v-8)*scale, 3)
        for i,(z0,z1,outer,inner) in enumerate(rows):
            if mode == "water":
                spans = [(-inner,inner)] if inner else []
            elif mode == "wall" and inner:
                spans = [(-outer,-inner),(inner,outer)]
            else:
                spans = [(-outer,outer)]
            for j,(a,b) in enumerate(spans):
                x,z,X,Z = coord(8+a),coord(z0),coord(8+b),coord(z1)
                cube = model.box("water" if mode=="water" else "frame",
                                 f"{label}_{i}_{j}",(x,bottom,z),(X,top,Z),tile)
                if mode == "water":
                    cube["faces"] = {side: {"uv":[0,0,0,0], "texture":None} for side in SIDES}
                    cube["faces"]["up"] = {"uv":[x,z,X,Z], "texture":model.texture(tile)}
    model.box("frame","raised_foot",(5.27,0,5.73),(10.73,.65,10.27),"bowl_blue_enamel")
    layer("porcelain_floor",.82,.65,1.25,"bowl_porcelain","solid")
    layer("lower_curve",.85,1.25,2.48,"bowl_porcelain","wall")
    layer("cup_wall",.96,2.48,3.56,"bowl_porcelain","wall")
    layer("rolled_blue_rim",1,3.56,4,"bowl_blue_enamel","wall")
    layer("drinking_water",.96,3.26,3.33,"bath_ripples","water")
    for i,x in enumerate((6.08,7.65,9.22)):
        model.box("frame",f"painted_fleck_{i}",(x,2.72,3.242),(x+.65,3.24,3.3),"bowl_blue_enamel")
    model.box("frame","hook_plate",(5.43,2.8,12.58),(10.57,3.5,13.08),"brass")
    for i,x in enumerate((5.73,9.58)):
        model.box("frame",f"hook_upright_{i}",(x,3.5,12.65),(x+.64,5.58,13.23),"brass")
        model.box("frame",f"hook_bridge_{i}",(x,5.58,12.65),(x+.64,6.05,14.06),"brass")
        model.box("frame",f"hook_tip_{i}",(x,5.08,13.6),(x+.64,5.58,14.06),"brass")
    model.save()


def cage_shelf(name, width, depth, height, perch, forward, spacing, scale):
    """Build the fixed wooden shelf below CageLayout's two rations and the removable water bowl."""
    cage=Project(name,True)
    cage.remove(lambda n:n.startswith(("frame_feeder_cup","frame_grain_surface","supply_")))
    x=.64*width+forward
    z=.5*depth
    top=perch/16 + .22*scale
    def board(label,lo,hi,material):
        axes=[]
        for a,b in zip(lo,hi):
            edges=[a]+[k for k in (1,2) if a < k < b]+[b]
            axes.append(list(zip(edges,edges[1:])))
        offsets=[16 if dimension>2 else 0 for dimension in (width,height,depth)]
        cubes=[]
        for i,cell in enumerate(product(*axes)):
            a=[round(pair[0]*16-off,4) for pair,off in zip(cell,offsets)]
            b=[round(pair[1]*16-off,4) for pair,off in zip(cell,offsets)]
            cubes.append(cage.box("frame",f"supply_{label}_{i}",a,b,material))
        return cubes
    board("shelf",(x-.45*scale,top-.095*scale,z-spacing-.43*scale),
          (x+.45*scale,top,z+spacing+.43*scale),"honey_oak")
    for i,zpost in enumerate((z-spacing*.76,z+spacing*.76)):
        board(f"leg_{i}",(x+.11*scale,2/16,zpost-.085*scale),
              (x+.28*scale,top-.095*scale,zpost+.085*scale),"walnut")
    for quality,zc,trim in (("seed",z-spacing,"toy_honeywood"),("premium",z+spacing,"toy_tealwood")):
        def dish(bit,a,b,mat):
            board(quality+"_"+bit,(x+a[0]*scale,top+a[1]*scale,zc+a[2]*scale),
                  (x+b[0]*scale,top+b[1]*scale,zc+b[2]*scale),mat)
        dish("floor",(-.435,0,-.42),(.435,.10,.42),"feed_jute")
        for i,k in enumerate((-.42,.36)):
            dish(f"rim_z{i}",(-.435,.10,k),(.435,.16,k+.06),trim)
        for i,k in enumerate((-.435,.375)):
            dish(f"rim_x{i}",(k,.10,-.36),(k+.06,.16,.36),trim)
    ahead,span,thickness={1:(.10,.16,.06),2:(.20,.22,.08),3:(.29,.35,.10)}[width]
    branch_x=.64*width+ahead
    branch_top=perch/16
    # The main perch occupies exactly 1.12 pixels in Z. Its unbroken top forms
    # the intersection; none of the branch's top faces can overlap that area.
    near=z-.56/16
    far=z+.56/16
    def branch(label,start,end,cap):
        lo=(branch_x-thickness/2,branch_top-thickness,start)
        hi=(branch_x+thickness/2,branch_top,end)
        for cube in board("feeding_branch_"+label, lo, hi, "perch_bark"):
            cube["faces"][cap]["texture"]=None
            for side in ("up","down"):
                uv=cube["faces"][side]["uv"]
                w,h=uv[2]-uv[0],uv[3]-uv[1]
                cube["faces"][side].update(uv=[1,5,round(1+h,4),round(5+w,4)],rotation=90)
    branch("front",z-span,near,"south")
    branch("back",far,z+span,"north")
    if thickness>1/16:
        lo=(branch_x-thickness/2,branch_top-thickness,near)
        hi=(branch_x+thickness/2,branch_top-1/16,far)
        for cube in board("feeding_branch_joint",lo,hi,"perch_bark"):
            for side in ("up","north","south"):
                cube["faces"][side]["texture"]=None
    cage.save()


CAGE_SHELVES=[
    ("parrot_cage",1,1,1,3,.184,.225,.245), ("large_parrot_cage",2,1,2,8,.40,.28,.32),
    ("deluxe_parrot_cage",3,2,3,12,.52,.47,.56), ("super_deluxe_parrot_cage",3,3,3,12,.52,.47,.56),
    ("giant_parrot_cage",3,3,3,12,.52,.47,.56),
]


if __name__ == "__main__":
    for prefix,premium in (("",False),("premium_",True)):
        grain_model(prefix+"bird_feed",premium,True)
        grain_model(prefix+"bird_feed_pile",premium,False)
        sack(prefix+"bird_feed_bag",premium)
    refinished_bath("parrot_bath",False)
    refinished_bath("large_parrot_bath",True)
    chew_toy()
    water_bowl()
    for layout in CAGE_SHELVES:
        cage_shelf(*layout)
