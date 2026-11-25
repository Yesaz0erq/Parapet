"""Draw the 16-pixel ivory hatching eggs and their species-coloured paper tags."""
import json
from pathlib import Path

from PIL import Image, ImageDraw


ASSETS = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/parapet'
# The print on each label takes its colours from the namesake bird.
TAGS = {
    'budgerigar': ('#83AB49', '#E8CB57'),
    'cockatiel': ('#90928A', '#DF7646'),
    'green_cheek_conure': ('#577D42', '#A53B3E'),
    'lovebird': ('#70A951', '#E68F72'),
    'monk_parakeet': ('#5D9D59', '#BBC3AF'),
    'caique': ('#D48C3C', '#464740'),
    'eclectus': ('#468F49', '#AA3E50'),
    'sun_conure': ('#F2B83D', '#DE7739'),
    'sulfur_crested_cockatoo': ('#E1C53D', '#777777'),
    'african_grey': ('#858D8B', '#B73E3C'),
    'blue_and_yellow_macaw': ('#3986AE', '#EDB646'),
    'blue_throated_macaw': ('#3D9E9B', '#DBAA49'),
    'military_macaw': ('#698641', '#B84639'),
    'great_green_macaw': ('#92AD53', '#DD7B45'),
    'scarlet_macaw': ('#CB4D43', '#E6BF3B'),
    'red_and_green_macaw': ('#A83E42', '#589253'),
    'red_fronted_macaw': ('#849547', '#D87140'),
    'chestnut_fronted_macaw': ('#6B934E', '#805643'),
    'hyacinth_macaw': ('#44539D', '#EEB947'),
    'lear_macaw': ('#526F9E', '#B78D4B'),
    'spix_macaw': ('#749BB8', '#B2C4CA'),
    'red_bellied_macaw': ('#56734A', '#955044'),
    'blue_headed_macaw': ('#597F9E', '#729052'),
    'blue_winged_macaw': ('#659950', '#465E99'),
    'yellow_collared_macaw': ('#65894B', '#DBC34B'),
    'red_shouldered_macaw': ('#619055', '#B64E4C'),
    'jardines_parrot': ('#496A48', '#C16A37'),
    'senegal_parrot': ('#687665', '#D4AB3F'),
    'bourkes_parrot': ('#BE8B96', '#789BAD'),
    'rainbow_lorikeet': ('#555CA3', '#DF9247'),
    'red_lory': ('#C34B49', '#4F5E89'),
    'dusky_lory': ('#72574B', '#E1984E'),
    'blue_fronted_amazon': ('#629F9D', '#DFCC5B'),
    'yellow_headed_amazon': ('#DDBD4D', '#659353'),
    'orange_winged_amazon': ('#839951', '#D48C4B'),
    'rose_ringed_parakeet': ('#87AB60', '#BC7D8A'),
    'eastern_rosella': ('#CB6250', '#D9DECD'),
    'crimson_rosella': ('#A9424D', '#688FB3'),
}


def egg(main, accent):
    image = Image.new('RGBA', (16, 16))
    draw = ImageDraw.Draw(image)
    rows = [(6, 7), (5, 8), (4, 9), (4, 9), (3, 10), (3, 10), (2, 11), (2, 11),
            (2, 12), (2, 12), (2, 12), (3, 11), (3, 11), (4, 10), (5, 9)]
    shell = {(x, y) for y, (first, last) in enumerate(rows) for x in range(first, last + 1)}
    for x, y in shell:
        if any((x + dx, y + dy) not in shell for dx, dy in ((0, 1), (-1, 0), (1, 0), (0, -1))):
            color = '#877865' if y < 10 else '#746659'
        elif y > 10 or x > 9:
            color = '#BEAC92'
        elif (x < 7 and y < 6) or (x < 5 and y < 10):
            color = '#FFF8DF'
        elif x + y < 16:
            color = '#F1E6CA'
        else:
            color = '#DACAAD'
        draw.point((x, y), fill=color)

    # The clipped corners, punched hole and short cord read distinctly from the shell.
    draw.polygon([(8, 8), (11, 8), (15, 12), (15, 13), (13, 15), (11, 15),
                  (7, 11), (7, 9)], fill='#77614C')
    draw.polygon([(9, 9), (11, 9), (14, 12), (14, 13), (12, 14),
                  (8, 11), (8, 10)], fill='#E8CDA0')
    draw.line([(7, 7), (8, 7), (9, 9)], fill='#9B744B')
    draw.point((9, 9), fill='#715A47')
    draw.line([(9, 11), (11, 10), (13, 12), (12, 13), (10, 12)], fill=main)
    draw.point((11, 11), fill=main)
    draw.line([(11, 12), (12, 12), (13, 13)], fill=accent)
    return image


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    directory = ASSETS / 'textures/item/hatching_eggs'
    directory.mkdir(parents=True, exist_ok=True)
    for species, palette in TAGS.items():
        item_id = species + '_hatching_egg'
        egg(*palette).save(directory / (species + '.png'))
        resource = 'parapet:item/hatching_eggs/' + species
        write_json(ASSETS / 'models/item/hatching_eggs' / (species + '.json'),
                   {'parent': 'minecraft:item/generated', 'textures': {'layer0': resource}})
        write_json(ASSETS / 'items' / (item_id + '.json'),
                   {'model': {'type': 'minecraft:model', 'model': resource}})


if __name__ == '__main__':
    main()
