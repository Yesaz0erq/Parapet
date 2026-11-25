"""Export the Blockbench companion projects into Minecraft's model-part coordinates.

Run with Python 3 from any directory. The source projects use Blockbench 5's
Java entity format, including box UVs and the Minecraft 24-unit ground origin.
"""

import json
import math
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "art/companions"
TARGET = ROOT / "src/client/resources/assets/parapet/geometry/companions"
ZERO = (0, 0, 0)


def vector(values):
    return [round(v, 7) if v else 0 for v in values]


def bone(name, pivot, parent_pivot, rotation):
    x, y, z = (a - b for a, b in zip(pivot, parent_pivot))
    rx, ry, rz = (math.radians(angle) for angle in rotation)
    return dict(name=name, pivot=vector((-x, -y, z)), rotation=vector((-rx, -ry, rz)), cubes=[], children=[])


def cube(element, pivot):
    if not element.get("box_uv", True):
        raise ValueError(f"{element['name']} uses per-face UVs in a Java box-UV project")
    x, y, z = pivot
    x0, y0, z0 = element["from"]
    x1, y1, z1 = element["to"]
    uv = element.get("uv_offset", (0, 0))
    if any(int(n) != n for n in uv):
        raise ValueError(f"{element['name']} has a fractional UV origin, which Minecraft cannot encode")
    return dict(uv=list(map(int, uv)), origin=vector((x-x1, y-y1, z0-z)), size=vector((x1-x0, y1-y0, z1-z0)),
                inflate=element.get("inflate", 0), mirror=element.get("mirror_uv", False))


def convert(project):
    groups = {group["uuid"]: group for group in project["groups"]}
    elements = {element["uuid"]: element for element in project["elements"]}

    def read_group(node, parent_pivot):
        group = groups[node["uuid"]]
        pivot = group["origin"]
        out = bone(group["name"], pivot, parent_pivot, group.get("rotation", ZERO))
        for child in node["children"]:
            if isinstance(child, dict):
                if groups[child["uuid"]].get("export", True):
                    out["children"].append(read_group(child, pivot))
                continue
            element = elements[child]
            if not element.get("export", True):
                continue
            if element.get("type", "cube") != "cube":
                raise ValueError(f"{element['name']} is not a Java entity cube")
            rotation = element.get("rotation", ZERO)
            if any(rotation):
                # Java bakes rotation into a ModelPart. Preserve the cube's own hinge.
                hinge = element["origin"]
                child_bone = bone("cube_" + element["uuid"], hinge, pivot, rotation)
                child_bone["cubes"].append(cube(element, hinge))
                out["children"].append(child_bone)
            else:
                out["cubes"].append(cube(element, pivot))
        return out

    resolution = project["resolution"]
    return {
        "texture": [resolution["width"], resolution["height"]],
        "bones": [read_group(node, (0, 24, 0)) for node in project["outliner"] if groups[node["uuid"]].get("export", True)],
    }


def main():
    files = sorted(SOURCE.glob("*.bbmodel"))
    if not files:
        raise SystemExit(f"No Blockbench companion projects found in {SOURCE}")
    TARGET.mkdir(parents=True, exist_ok=True)
    for source in files:
        geometry = convert(json.loads(source.read_text(encoding="utf-8")))
        pretty = json.dumps(geometry, indent=2, ensure_ascii=False)
        pretty = re.sub(r'\[\s*(-?[\d.eE+\-]+(?:,\s*-?[\d.eE+\-]+)*)\s*\]',
                        lambda match: "[" + ", ".join(n.strip() for n in match[1].split(",")) + "]", pretty)
        (TARGET / source.with_suffix(".json").name).write_text(pretty + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
