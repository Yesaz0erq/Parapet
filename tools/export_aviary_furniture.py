"""Export the square cage doors, multipart blocks and inventory models from art/furniture."""
import json
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/parapet"
# Width, depth, height in model units. Supersized projects are centred inside
# Blockbench's Java model bounds of -16 to 32 on each axis.
CAGES = {
    "parrot_cage": (16, 16, 16), "large_parrot_cage": (32, 16, 32),
    "deluxe_parrot_cage": (48, 32, 48), "super_deluxe_parrot_cage": (48, 48, 48),
    "giant_parrot_cage": (48, 48, 48),
}
FURNITURE = {"cage_workbench", "parrot_bath", "parrot_play_stand"}
LARGE_FIXTURES = {"large_parrot_play_stand": (48, 16, 32), "large_parrot_bath": (48, 48, 16)}
TOYS = {"foraging_ball", "chew_blocks", "rope_swing"}
FACINGS = ("north", "east", "south", "west")


def write(path, document):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(document, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def read_groups(project):
    cubes = {element["uuid"]: element for element in project["elements"]}
    groups = {group["uuid"]: group["name"] for group in project["groups"]}
    materials = [Path(t["name"]).stem for t in project["textures"]]
    result = {}
    for group in project["outliner"]:
        elements = []
        for key in group["children"]:
            cube = cubes[key]
            faces = {}
            for side, face in cube["faces"].items():
                if face.get("texture") is None:
                    continue
                material = materials[int(face["texture"])]
                faces[side] = {"uv": face["uv"], "texture": "#" + material}
                if face.get("rotation"):
                    faces[side]["rotation"] = face["rotation"]
            elements.append({"from": cube["from"], "to": cube["to"], "faces": faces})
        result[groups[group["uuid"]]] = elements
    return result


def save_model(name, elements, inventory=False):
    materials = sorted({face["texture"][1:] for part in elements for face in part["faces"].values()})
    textures = {material: "parapet:block/aviary/" + material for material in materials}
    textures["particle"] = textures[next((m for m in ("honey_oak","smoked_spruce","walnut","red_clay","linen") if m in textures), materials[0])]
    model = {"parent": "minecraft:block/block", "ambientocclusion": False, "textures": textures, "elements": elements}
    if inventory:
        model["display"] = {"gui": {"rotation": [25, 35, 0], "translation": [0, 0, 0], "scale": [.79, .79, .79]}}
    write(ASSETS / "models" / (name + ".json"), model)
    return "parapet:" + name


def transform_cube(cube, transform):
    a, b = transform(*cube["from"]), transform(*cube["to"])
    return {**cube, "from": [round(min(p,q), 4) for p,q in zip(a,b)],
                    "to": [round(max(p,q), 4) for p,q in zip(a,b)]}


def open_leaf(cube, right, width):
    hinge_x = width - 1.92 if right else 1.92
    sign = -1 if right else 1
    def rotate(x,y,z):
        return hinge_x - sign*(z-1.92), y, 1.92 + sign*(x-hinge_x)
    result = transform_cube(cube, rotate)
    faces = {}
    for direction, original in cube["faces"].items():
        face = original.copy()
        if direction in FACINGS:
            direction = FACINGS[(FACINGS.index(direction) + sign) % 4]
        else:
            face["rotation"] = (face.get("rotation", 0) + 90*sign*(1 if direction == "up" else -1)) % 360
        faces[direction] = face
    result["faces"] = faces
    return result


def part_name(x, y, z):
    labels = [("", "upper", "top")[y], ("", "back", "rear")[z], ("", "right", "far_right")[x]]
    return "_".join(label for label in labels if label) or "base"


def cell_for(cube):
    x,y,z = [int((a+b)/32) for a,b in zip(cube["from"], cube["to"])]
    return part_name(x,y,z), transform_cube(cube, lambda a,b,c: (a-16*x, b-16*y, c-16*z))


def model_ref(path):
    return {"type": "minecraft:model", "model": path}


def item_definition(name, model):
    write(ASSETS / "items" / (name + ".json"), {"model": model})


def export_large_fixture(name, groups):
    width, depth, height = LARGE_FIXTURES[name]
    cells = defaultdict(lambda: defaultdict(list))
    def absolute(x, y, z):
        return x + 16, y, z + (16 if depth == 48 else 0)
    for group, cubes in groups.items():
        for cube in cubes:
            part, element = cell_for(transform_cube(cube, absolute))
            cells[part][group].append(element)
    multipart = []
    for y in range(3):
        for x in range(3):
            for z in range(3):
                part = part_name(x, y, z)
                if part not in cells:
                    multipart.append({"when": {"part": part}, "apply": {"model": "parapet:block/aviary/empty"}})
                    continue
                for group, cubes in cells[part].items():
                    ref = save_model(f"block/aviary/{name}/{part}_{group}", cubes)
                    for i, facing in enumerate(FACINGS):
                        properties = {"part": part, "facing": facing}
                        if group == "water":
                            properties["filled"] = "true"
                        multipart.append({"when": properties, "apply": {"model": ref, "y": i * 90}})
    write(ASSETS / "blockstates" / (name + ".json"), {"multipart": multipart})
    def inventory(x, y, z):
        a, b, c = absolute(x, y, z)
        return (a-width/2)/3+8, (b-height/2)/3+8, (c-depth/2)/3+8
    ref = save_model("item/aviary/" + name, [transform_cube(c, inventory) for c in groups["frame"]], True)
    item_definition(name, model_ref(ref))


def export_toy(name, groups):
    fixed, moving = groups.get("frame", []), groups["moving"]
    model = save_model("block/aviary/" + name, fixed + moving)
    rotation = {"origin": [8, 4.6, 8], "axis": "z", "angle": 22.5} if name == "foraging_ball" else {
        "origin": [8, 14.5, 8], "axis": "x", "angle": 22.5}
    alternate = save_model("block/aviary/" + name + "_active", fixed + [dict(cube, rotation=rotation) for cube in moving])
    write(ASSETS / "blockstates" / (name + ".json"), {"variants": {
        "active=false": {"model": model}, "active=true": {"model": alternate}}})
    item_definition(name, model_ref(model))


def export_cage(name, groups):
    width, depth, height = CAGES[name]
    shift_x, shift_y, shift_z = [16 if size>32 else 0 for size in (width, height, depth)]
    def absolute(x,y,z):
        return x+shift_x, y+shift_y, z+shift_z
    groups = {group:[transform_cube(cube, absolute) for cube in cubes] for group,cubes in groups.items()}
    layers = defaultdict(lambda: defaultdict(list))
    for group, cubes in groups.items():
        for cube in cubes:
            if group.startswith("door_"):
                part, closed = cell_for(cube)
                layers[part]["closed"].append(closed)
                part, opened = cell_for(open_leaf(cube, group == "door_right", width))
                layers[part]["open"].append(opened)
            else:
                part, local = cell_for(cube)
                layers[part][group].append(local)

    multipart = []
    predicates = {"open":{"open":"true"}, "closed":{"open":"false"}, "frame":{},
                  "garland":{"garland":"true"}, "bell":{"bell":"true"}}
    for part, states in layers.items():
        for state, elements in states.items():
            model = save_model(f"block/aviary/{name}/{part}_{state}", elements)
            for i, facing in enumerate(FACINGS):
                variant = {"model":model}
                if i:
                    variant["y"] = 90*i
                multipart.append({"when":{"part":part, "facing":facing, **predicates[state]}, "apply":variant})
    # Empty grid cells in a large aviary still need a particle model for picking/breaking.
    all_parts = {part_name(x,y,z) for x in range(3) for y in range(3) for z in range(3)}
    without_mesh = sorted(all_parts - layers.keys())
    if without_mesh:
        multipart.append({"when":{"part":"|".join(without_mesh)}, "apply":{"model":"parapet:block/aviary/empty"}})
    write(ASSETS / "blockstates" / (name + ".json"), {"multipart":multipart})

    scale = 16 / max(width, height)
    def centred(x,y,z):
        return (x-width/2)*scale+8, (y-height/2)*scale+8, (z-depth/2)*scale+8
    body = groups["frame"] + groups["door_left"] + groups.get("door_right", [])
    layers = [model_ref(save_model("item/aviary/" + name, [transform_cube(c,centred) for c in body], True))]
    for group, component in (("garland","cage_garland"), ("bell","hanging_bell")):
        overlay = save_model(f"item/aviary/{name}_{group}", [transform_cube(c,centred) for c in groups[group]], True)
        layers.append({"type":"minecraft:condition", "property":"minecraft:component", "predicate":"minecraft:custom_data",
                       "value":{"parapet:"+component:True}, "on_true":model_ref(overlay), "on_false":{"type":"minecraft:empty"}})
    item_definition(name, {"type":"minecraft:composite", "models":layers})


def main():
    write(ASSETS / "models/block/aviary/empty.json",
          {"textures":{"particle":"parapet:block/aviary/iron"}, "elements":[]})
    for path in sorted((ROOT / "art/furniture").glob("*.bbmodel")):
        name = path.stem
        groups = read_groups(json.loads(path.read_text()))
        if name in CAGES:
            export_cage(name, groups)
            continue
        if name in LARGE_FIXTURES:
            export_large_fixture(name, groups)
            continue
        if name in TOYS:
            export_toy(name, groups)
            continue
        element_list = groups["frame"]
        if name not in FURNITURE:
            ref = save_model("item/aviary/" + name, element_list, True)
            item_definition(name, model_ref(ref))
            continue
        ref = save_model("block/aviary/" + name, element_list)
        variants = {"": {"model":ref}}
        if name == "parrot_bath":
            wet = save_model("block/aviary/parrot_bath_filled", element_list + groups["water"])
            variants = {"filled=false":{"model":ref}, "filled=true":{"model":wet}}
        elif name == "cage_workbench":
            variants = {"facing="+d:{"model":ref, "y":90*i} for i,d in enumerate(FACINGS)}
        write(ASSETS / "blockstates" / (name + ".json"), {"variants":variants})
        item_definition(name, model_ref(ref))


if __name__ == "__main__":
    main()
