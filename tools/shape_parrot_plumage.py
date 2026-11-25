"""Extend a native 128 × 128 parrot atlas with UV islands for layered geometry.

The original artwork occupies the top half unchanged. Run this script to deck
out an existing base atlas, or run paint_macaws.py to repaint the complete set.
"""

from PIL import Image


def faces(origin, size):
    x, y = origin
    w, h, d = size
    return {
        "east": (x, y+d, d, h), "front": (x+d, y+d, w, h),
        "west": (x+d+w, y+d, d, h), "back": (x+2*d+w, y+d, w, h),
        "top": (x+d, y, w, d), "bottom": (x+d+w, y, w, d),
    }


def texel(coordinate, length):
    return min(length - 1, max(0, int(coordinate)))


def centered(index, target_length, source_length):
    # Keep each source texel one unit wide. A protrusion can duplicate its edge.
    return texel(index + .5 + (source_length - target_length)/2, source_length)


def cap_sample(face, column, row, width, depth, box_width, box_depth, outer_face=None):
    # Shelf colors meet the surrounding vertical plumage right at the seam.
    distance_from_front = depth-row-.5 if face == "top" else row+.5
    x = column+.5
    # A one-texel-wide wing tip is equidistant from its two broad sides.
    # Its exposed edge follows the plumage on the outside of that wing.
    border = outer_face or min(((distance_from_front, "front"), (depth-distance_from_front, "back"),
                                (x, "east"), (width-x, "west")))[1]
    if border in {"front", "back"}:
        i = centered(column, width, box_width)
        return border, box_width-1-i if border == "back" else i
    z = texel(distance_from_front + (box_depth-depth)/2, box_depth)
    return border, box_depth-1-z if border == "east" else z


def transfer(source, target, source_uv, source_size, target_uv, size, top, keel=False, outer_face=None):
    """Copy a 1:1 vertical slice. The keel takes all six faces from the breast."""
    sw, sh, sd = source_size
    _, cut_height, _ = size
    original = faces(source_uv, source_size)
    for face, (x, y, width, height) in faces(target_uv, size).items():
        for j in range(height):
            for i in range(width):
                is_cap = face in {"top", "bottom"}
                if keel:
                    sample_face = "front"
                    if face in {"east", "west"}:
                        # Each side wraps the closest edge of the projecting sternum.
                        margin = (sw-size[0])/2
                        col = texel(margin if face == "east" else sw-margin-1, sw)
                    else:
                        col = centered(i, width, sw)
                elif is_cap:
                    sample_face, col = cap_sample(face, i, j, width, height, sw, sd, outer_face)
                else:
                    sample_face = face
                    col = centered(i, width, original[face][2])
                level = (0 if face == "top" else cut_height-1) if is_cap else j
                row = texel(top + level + .5, sh)
                ox, oy, _, _ = original[sample_face]
                target[x+i, y+j] = source[ox+col, oy+row]


def add_layers(atlas, shape, uv):
    source = atlas.convert("RGBA").crop((0, 0, 128, 128))
    output = Image.new("RGBA", (128, 256))
    output.paste(source, (0, 0))
    src, dst = source.load(), output.load()
    w, h, d = shape["body"]
    sections = (
        ((0, 130), (w-2, round(h*.34), d-2), .00),
        ((52, 130), (w, round(h*.53), d+1), .24),
        ((0, 165), (w-1, round(h*.32), d-1), .68),
        ((52, 165), (w-4, round(h*.27), 2), .29),
    )
    for origin, size, bottom in sections:
        top = h * (1-bottom) - size[1]
        transfer(src, dst, uv["body"], shape["body"], origin, size, top, keel=origin==(52, 165))

    _, wing_height, wing_depth = shape["left_wing"]
    _, primary_height, primary_depth = shape["left_primary"]
    for side, shoulder_x, secondary_x in (("left", 0, 70), ("right", 33, 96)):
        outer_face = "west" if side == "left" else "east"
        shoulder_size = (1, round(wing_height*.49), wing_depth-2)
        transfer(src, dst, uv[side+"_wing"], shape["left_wing"],
                 (shoulder_x, 200), shoulder_size, 0, outer_face=outer_face)
        secondary_size = (1, round(primary_height*.63), primary_depth-1)
        transfer(src, dst, uv[side+"_primary"], shape["left_primary"],
                 (secondary_x, 200), secondary_size, primary_height*.20, outer_face=outer_face)
    return output


def main():
    from paint_macaws import ASSETS, SHAPES, UV, VARIANTS

    for group, variants in VARIANTS.items():
        directory = ASSETS / ("macaw" if group.startswith("macaw_") else group)
        for variant in variants:
            path = directory / (variant+".png")
            with Image.open(path) as atlas:
                expanded = add_layers(atlas, SHAPES[group], UV)
            expanded.save(path, optimize=True)


if __name__ == "__main__":
    main()
