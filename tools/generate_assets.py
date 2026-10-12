#!/usr/bin/env python3
"""Генерирует текстуры, модели и lang-файлы мода. Запуск из корня проекта: python3 tools/generate_assets.py"""
import json, os, random
from PIL import Image, ImageDraw

ROOT = "src/main/resources/assets/bloxcraft"
for d in ("lang", "models/item", "textures/item", "textures/entity"):
    os.makedirs(f"{ROOT}/{d}", exist_ok=True)

# id: (EN, RU, основной цвет, акцентный цвет)
FRUITS = {
    "sand":   ("Sand Fruit", "Фрукт Песка", (222, 200, 130), (170, 140, 80)),
    "rubber": ("Rubber Fruit", "Резиновый фрукт", (230, 70, 110), (255, 180, 200)),
    "flame":  ("Flame Fruit", "Фрукт Пламени", (255, 106, 0), (255, 225, 60)),
    "ice":    ("Ice Fruit", "Фрукт Льда", (130, 210, 255), (255, 255, 255)),
    "light":  ("Light Fruit", "Фрукт Света", (255, 245, 150), (255, 255, 255)),
    "magma":  ("Magma Fruit", "Фрукт Магмы", (190, 40, 20), (255, 150, 0)),
    "quake":  ("Quake Fruit", "Фрукт Землетрясения", (190, 190, 215), (90, 90, 140)),
    "buddha": ("Buddha Fruit", "Фрукт Будды", (240, 190, 40), (255, 240, 150)),
    "dark":   ("Dark Fruit", "Фрукт Тьмы", (60, 30, 90), (170, 60, 230)),
    "dragon": ("Dragon Fruit", "Фрукт Дракона", (60, 150, 230), (255, 120, 40)),
}
# id: (EN, RU, цвет лезвия)
SWORDS = {
    "katana": ("Katana", "Катана", (210, 215, 225)),
    "cutlass": ("Cutlass", "Абордажная сабля", (190, 190, 200)),
    "bisento": ("Bisento", "Бисенто", (170, 200, 210)),
    "shisui": ("Shisui", "Шисуи", (120, 190, 255)),
    "saddi": ("Saddi", "Сэдди", (240, 230, 90)),
    "yama": ("Yama", "Яма", (255, 110, 50)),
    "dark_blade": ("Dark Blade (Yoru)", "Тёмный клинок (Ёру)", (40, 20, 70)),
    "cursed_dual_katana": ("Cursed Dual Katana", "Проклятые двойные катаны", (150, 20, 60)),
}
BOSSES = {  # id: (имя, кожа, волосы, рубашка, штаны, акцент)
    "bobby":         ("Bobby", (240, 200, 160), (130, 40, 20), (180, 30, 30), (60, 40, 30), (40, 40, 40)),
    "gorilla_king":  ("Gorilla King", (70, 70, 80), (35, 35, 40), (90, 90, 100), (60, 60, 70), (30, 30, 30)),
    "saber_expert":  ("Saber Expert", (235, 200, 170), (210, 210, 220), (30, 60, 120), (30, 30, 50), (200, 200, 60)),
    "ice_admiral":   ("Ice Admiral", (200, 230, 255), (240, 250, 255), (100, 170, 230), (60, 90, 150), (255, 255, 255)),
    "magma_admiral": ("Magma Admiral", (200, 120, 80), (255, 120, 0), (60, 20, 20), (40, 40, 40), (255, 80, 0)),
    "greybeard":     ("Greybeard", (225, 190, 160), (190, 190, 190), (50, 60, 70), (30, 35, 45), (200, 170, 60)),
    "darkbeard":     ("Darkbeard", (60, 50, 70), (10, 10, 10), (20, 10, 30), (15, 10, 25), (150, 0, 200)),
}
NPCS = {
    "npc_quest": ((235, 200, 170), (90, 60, 30), (40, 120, 60), (60, 50, 40), (200, 170, 60)),
    "npc_fruit": ((235, 200, 170), (60, 40, 30), (140, 60, 170), (50, 40, 70), (255, 200, 60)),
    "npc_sword": ((220, 180, 150), (50, 50, 55), (160, 40, 40), (50, 50, 55), (180, 180, 190)),
}

def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)

def fruit_tex(fid, main, acc):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse((2, 4, 13, 15), fill=shade(main, 0.6) + (255,))
    d.ellipse((3, 5, 12, 14), fill=main + (255,))
    rnd = random.Random(fid)
    for _ in range(9):
        x, y = rnd.randint(4, 11), rnd.randint(6, 13)
        if (x - 7.5) ** 2 + (y - 9.5) ** 2 < 17:
            im.putpixel((x, y), acc + (255,))
    for p in ((5, 7), (5, 8), (6, 7)):
        im.putpixel(p, shade(main, 1.35) + (255,))
    for p in ((8, 3), (8, 4)):
        im.putpixel(p, (110, 70, 30, 255))
    for p in ((9, 2), (10, 2), (10, 3), (11, 3)):
        im.putpixel(p, (60, 170, 60, 255))
    return im

def sword_tex(blade):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for i in range(9):
        im.putpixel((6 + i, 9 - i), blade + (255,))
        im.putpixel((7 + i, 9 - i), shade(blade, 1.25) + (255,))
        im.putpixel((5 + i, 9 - i), shade(blade, 0.6) + (255,))
    for p in ((3, 8), (4, 9), (5, 10), (6, 11), (7, 12)):
        im.putpixel(p, (200, 170, 50, 255))
    for p in ((4, 11), (3, 12), (2, 13)):
        im.putpixel(p, (90, 55, 25, 255))
    im.putpixel((1, 14), (200, 170, 50, 255))
    return im

def logpose_tex():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse((1, 1, 14, 14), fill=(60, 50, 40, 255))
    d.ellipse((2, 2, 13, 13), fill=(230, 220, 190, 255))
    d.ellipse((5, 5, 10, 10), fill=(120, 190, 230, 255))
    for p in ((7, 3), (8, 4), (8, 5), (8, 6)):
        im.putpixel(p, (200, 30, 30, 255))
    for p in ((7, 9), (7, 10), (7, 11)):
        im.putpixel(p, (40, 40, 40, 255))
    return im

# раскладка скина 64x64: (u, v, ширина, высота, глубина)
PARTS = {"head": (0, 0, 8, 8, 8), "body": (16, 16, 8, 12, 4), "rarm": (40, 16, 4, 12, 4),
         "rleg": (0, 16, 4, 12, 4), "larm": (32, 48, 4, 12, 4), "lleg": (16, 48, 4, 12, 4)}

def skin_tex(skin, hair, shirt, pants, acc, scars=False, seed=0):
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    def rect(u, v, w, h, c):
        d.rectangle((u, v, u + w - 1, v + h - 1), fill=c + (255,))
    for name, (u, v, w, h, dp) in PARTS.items():
        fw, fh = 2 * dp + 2 * w, dp + h
        base = {"head": skin, "body": shirt, "rarm": shirt, "larm": shirt, "rleg": pants, "lleg": pants}[name]
        rect(u, v, fw, fh, base)
        if name == "head":
            rect(u, v, fw, dp, hair)                 # верх головы
            rect(u + dp, v + dp, w, 2, hair)          # чёлка (перед)
            rect(u, v + dp, dp, 3, hair)              # бока
            rect(u + dp + w, v + dp, dp, 3, hair)
            rect(u + 2 * dp + w, v + dp, w, h - 1, hair)  # затылок
            d.point((u + dp + 2, v + dp + 4), fill=(255, 255, 255, 255))
            d.point((u + dp + 5, v + dp + 4), fill=(255, 255, 255, 255))
            d.point((u + dp + 3, v + dp + 4), fill=(20, 20, 20, 255))
            d.point((u + dp + 6, v + dp + 4), fill=(20, 20, 20, 255))
            rect(u + dp + 2, v + dp + 6, 4, 1, shade(skin, 0.6))
        elif name == "body":
            rect(u, v + dp + h - 3, fw, 1, acc)       # пояс
        elif name in ("rarm", "larm"):
            rect(u, v + dp + h - 3, fw, 3, skin)      # кисти
        else:
            rect(u, v + dp + h - 3, fw, 3, acc)       # сапоги
    if scars:
        rnd = random.Random(seed)
        for _ in range(10):
            d.point((rnd.randint(16, 40), rnd.randint(20, 31)), fill=shade(shirt, 0.5) + (255,))
    return im

def main():
    lang_en = {"itemGroup.bloxcraft": "Blox Craft", "item.bloxcraft.log_pose": "Log Pose",
               "entity.bloxcraft.boss": "Boss", "entity.bloxcraft.quest_npc": "Villager NPC",
               "key.categories.bloxcraft": "Blox Craft",
               "key.bloxcraft.z": "Fruit ability 1", "key.bloxcraft.x": "Fruit ability 2", "key.bloxcraft.c": "Fruit ability 3"}
    lang_ru = {"itemGroup.bloxcraft": "Blox Craft", "item.bloxcraft.log_pose": "Лог-пос",
               "entity.bloxcraft.boss": "Босс", "entity.bloxcraft.quest_npc": "Житель-NPC",
               "key.categories.bloxcraft": "Blox Craft",
               "key.bloxcraft.z": "Способность фрукта 1", "key.bloxcraft.x": "Способность фрукта 2", "key.bloxcraft.c": "Способность фрукта 3"}

    def model(name, parent):
        with open(f"{ROOT}/models/item/{name}.json", "w") as f:
            json.dump({"parent": parent, "textures": {"layer0": f"bloxcraft:item/{name}"}}, f, indent=2)

    for fid, (en, ru, main, acc) in FRUITS.items():
        name = f"{fid}_fruit"
        fruit_tex(fid, main, acc).save(f"{ROOT}/textures/item/{name}.png")
        model(name, "minecraft:item/generated")
        lang_en[f"item.bloxcraft.{name}"], lang_ru[f"item.bloxcraft.{name}"] = en, ru
    for sid, (en, ru, blade) in SWORDS.items():
        sword_tex(blade).save(f"{ROOT}/textures/item/{sid}.png")
        model(sid, "minecraft:item/handheld")
        lang_en[f"item.bloxcraft.{sid}"], lang_ru[f"item.bloxcraft.{sid}"] = en, ru
    logpose_tex().save(f"{ROOT}/textures/item/log_pose.png")
    model("log_pose", "minecraft:item/generated")

    for bid, (_, sk, hr, sh, pn, ac) in BOSSES.items():
        skin_tex(sk, hr, sh, pn, ac, scars=True, seed=hash(bid) & 0xFFFF).save(f"{ROOT}/textures/entity/boss_{bid}.png")
    for nid, (sk, hr, sh, pn, ac) in NPCS.items():
        skin_tex(sk, hr, sh, pn, ac).save(f"{ROOT}/textures/entity/{nid}.png")

    for code, data in (("en_us", lang_en), ("ru_ru", lang_ru)):
        with open(f"{ROOT}/lang/{code}.json", "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
    print("assets generated")

main()
