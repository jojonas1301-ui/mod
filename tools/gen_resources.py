#!/usr/bin/env python3
"""Gera os recursos (assets/data) do mod Vilas & Cambio."""
import json
import os
import unicodedata
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "villaeconomy")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "villaeconomy")
VANILLA = os.path.join(ROOT, "tools", "dump", "assets", "assets", "minecraft", "textures")

NOTES = [10, 20, 50, 100, 200]

NOTE_STYLE = {
    10: ((22, 92, 48), (58, 168, 96), (232, 226, 180)),
    20: ((20, 62, 118), (64, 136, 200), (222, 232, 244)),
    50: ((74, 32, 104), (140, 84, 190), (238, 224, 246)),
    100: ((122, 46, 24), (206, 108, 52), (250, 232, 208)),
    200: ((92, 66, 12), (196, 158, 40), (252, 244, 204)),
}

BLOCKS = {
    "balcao_banqueiro": ((28, 30, 42), (92, 74, 40), (204, 172, 78)),
    "mesa_mineracao": ((48, 48, 52), (110, 104, 96), (188, 188, 196)),
    "alambique": ((32, 40, 56), (110, 62, 148), (128, 224, 196)),
    "bancada_joalheiro": ((36, 30, 52), (208, 214, 228), (120, 224, 232)),
    "posto_guarda": ((44, 34, 26), (128, 96, 56), (198, 154, 74)),
    "podio_leilao": ((52, 40, 24), (168, 132, 72), (238, 210, 120)),
    "cofre_tesoureiro": ((34, 26, 18), (112, 84, 48), (226, 196, 96)),
    "balcao_nether": ((24, 18, 20), (92, 60, 64), (240, 148, 60)),
    "forja_ouro": ((28, 22, 18), (120, 92, 36), (252, 208, 72)),
    "totem_reliquias": ((22, 20, 30), (86, 66, 110), (196, 128, 236)),
}

PROFESSIONS = {
    # nome interno: (base vanilla, matiz rgb)
    "banqueiro": ("librarian.png", (220, 180, 60)),
    "mineiro": ("armorer.png", (140, 150, 170)),
    "alquimista": ("cleric.png", (140, 90, 200)),
    "joalheiro": ("cartographer.png", (90, 190, 200)),
    "guarda": ("weaponsmith.png", (120, 90, 60)),
    "leiloeiro": ("librarian.png", (200, 120, 60)),
    "tesoureiro": ("leatherworker.png", (230, 200, 90)),
    "banqueiro_nether": ("librarian.png", (240, 150, 70)),
    "ferreiro_ouro": ("armorer.png", (250, 200, 70)),
    "cacador_reliquias": ("cleric.png", (190, 110, 230)),
}

HEX = {}


def slug(text):
    base = unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode()
    return base


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(data, handle, ensure_ascii=False, indent=2)
        handle.write("\n")


def write_text(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        handle.write(text)


def scaled(color, factor):
    return tuple(max(0, min(255, int(component * factor))) for component in color)


def shade(color, factor):
    return scaled(color, factor)


# --------------------------------------------------------------------------- texturas
def make_note(value):
    dark, mid, light = NOTE_STYLE[value]
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.rectangle([0, 1, 15, 14], fill=mid, outline=dark)
    draw.rectangle([1, 2, 14, 13], outline=light)
    # faixa central
    draw.rectangle([3, 4, 12, 11], fill=shade(mid, 1.15), outline=dark)
    # selo
    draw.ellipse([6, 6, 9, 9], fill=light, outline=dark)
    # fitas laterais
    draw.line([1, 3, 1, 12], fill=dark)
    draw.line([14, 3, 14, 12], fill=dark)
    # marcas do valor
    marks = {10: 1, 20: 2, 50: 2, 100: 3, 200: 4}[value]
    for index in range(marks):
        draw.point((3 + index * 2, 13), fill=light)
        draw.point((3 + index * 2, 2), fill=light)
    path = os.path.join(ASSETS, "textures", "item", f"nota{value}.png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    image.save(path)


def make_block_texture(name):
    dark, mid, light = BLOCKS[name]
    image = Image.new("RGBA", (16, 16), mid)
    draw = ImageDraw.Draw(image)
    draw.rectangle([0, 0, 15, 15], outline=dark)
    # padrão de tábuas / pedra
    for y in range(0, 16, 4):
        draw.line([0, y, 15, y], fill=shade(mid, 0.82))
    for x in range(2, 16, 5):
        draw.line([x, 0, x, 15], fill=shade(mid, 0.9))
    # detalhe brilhante no centro
    draw.rectangle([5, 5, 10, 10], fill=shade(mid, 1.12), outline=light)
    draw.point((7, 7), fill=light)
    draw.point((8, 8), fill=light)
    draw.rectangle([1, 1, 3, 3], fill=shade(mid, 1.05))
    draw.rectangle([12, 12, 14, 14], fill=shade(mid, 0.9))
    path = os.path.join(ASSETS, "textures", "block", f"{name}.png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    image.save(path)


def tint_texture(source, target, tint, strength=0.62):
    with Image.open(source) as image:
        image = image.convert("RGBA")
        pixels = image.load()

        for y in range(image.height):
            for x in range(image.width):
                r, g, b, a = pixels[x, y]

                if a == 0:
                    continue

                gray = (r + g + b) / 3.0
                nr = int(gray * (1 - strength) + tint[0] * strength)
                ng = int(gray * (1 - strength) + tint[1] * strength)
                nb = int(gray * (1 - strength) + tint[2] * strength)
                pixels[x, y] = (min(255, nr), min(255, ng), min(255, nb), a)

        os.makedirs(os.path.dirname(target), exist_ok=True)
        image.save(target)


def make_profession_textures():
    for name, (base, tint) in PROFESSIONS.items():
        source = os.path.join(VANILLA, "entity", "villager", "profession", base)

        if not os.path.exists(source):
            continue

        for namespace in ("villaeconomy", "minecraft"):
            target = os.path.join(ROOT, "src", "main", "resources", "assets", namespace, "textures",
                                  "entity", "villager", "profession", f"{name}.png")
            tint_texture(source, target, tint)


def make_nether_type_texture():
    source = os.path.join(VANILLA, "entity", "villager", "type", "plains.png")
    target = os.path.join(ASSETS, "textures", "entity", "villager", "type", "nether.png")

    if os.path.exists(source):
        tint_texture(source, target, (226, 128, 116), 0.7)


def make_icon():
    image = Image.new("RGBA", (128, 128), (24, 24, 32, 255))
    draw = ImageDraw.Draw(image)
    draw.rectangle([16, 32, 111, 95], fill=(58, 168, 96), outline=(20, 84, 44), width=4)
    draw.rectangle([26, 42, 101, 85], outline=(232, 226, 180), width=3)
    draw.ellipse([54, 50, 73, 69], fill=(240, 224, 120), outline=(120, 96, 32), width=3)
    draw.text((30, 100), "V&C", fill=(250, 220, 120))
    image.save(os.path.join(ASSETS, "icon.png"))


# --------------------------------------------------------------------------- modelos
def generate_item_models():
    for value in NOTES:
        write_json(os.path.join(ASSETS, "items", f"nota{value}.json"),
                   {"model": {"type": "minecraft:model", "model": f"villaeconomy:item/nota{value}"}})
        write_json(os.path.join(ASSETS, "models", "item", f"nota{value}.json"),
                   {"parent": "minecraft:item/generated",
                    "textures": {"layer0": f"villaeconomy:item/nota{value}"}})

    write_json(os.path.join(ASSETS, "items", "nether_villager_spawn_egg.json"),
               {"model": {"type": "minecraft:model", "model": "villaeconomy:item/nether_villager_spawn_egg"}})
    write_json(os.path.join(ASSETS, "models", "item", "nether_villager_spawn_egg.json"),
               {"parent": "minecraft:item/template_spawn_egg"})


def generate_block_resources():
    for name in BLOCKS:
        write_json(os.path.join(ASSETS, "blockstates", f"{name}.json"),
                   {"variants": {"": {"model": f"villaeconomy:block/{name}"}}})
        write_json(os.path.join(ASSETS, "models", "block", f"{name}.json"),
                   {"parent": "minecraft:block/cube_all", "textures": {"all": f"villaeconomy:block/{name}"}})
        write_json(os.path.join(ASSETS, "items", f"{name}.json"),
                   {"model": {"type": "minecraft:model", "model": f"villaeconomy:block/{name}"}})
        write_json(os.path.join(DATA, "loot_table", "blocks", f"{name}.json"), {
            "type": "minecraft:block",
            "random_sequence": f"villaeconomy:blocks/{name}",
            "pools": [{
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [{"type": "minecraft:item", "name": f"villaeconomy:{name}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        })


def generate_recipes():
    shaped = {
        "balcao_banqueiro": (["GGG", "GEG", "SSS"], {"G": "minecraft:gold_ingot", "E": "minecraft:emerald",
                                                     "S": "minecraft:smooth_stone"}),
        "mesa_mineracao": (["III", "C C", "SSS"], {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot",
                                                   "S": "minecraft:smooth_stone"}),
        "alambique": ([" B ", "GGG", "SSS"], {"B": "minecraft:glass_bottle", "G": "minecraft:gold_nugget",
                                              "S": "minecraft:smooth_stone"}),
        "bancada_joalheiro": (["DAD", "SSS"], {"D": "minecraft:diamond", "A": "minecraft:amethyst_shard",
                                               "S": "minecraft:smooth_stone"}),
        "posto_guarda": (["I I", "ISI", "SSS"], {"I": "minecraft:iron_ingot", "S": "minecraft:oak_planks"}),
        "podio_leilao": (["GGG", " B ", "SSS"], {"G": "minecraft:gold_ingot", "B": "minecraft:bookshelf",
                                                 "S": "minecraft:smooth_stone"}),
        "cofre_tesoureiro": (["IGI", "GBG", "IGI"], {"I": "minecraft:iron_ingot", "G": "minecraft:gold_ingot",
                                                     "B": "minecraft:barrel"}),
        "balcao_nether": (["GGG", "BBB", "BBB"], {"G": "minecraft:gold_ingot", "B": "minecraft:blackstone"}),
        "forja_ouro": (["GGG", "BBB", "BBB"], {"G": "minecraft:gold_block", "B": "minecraft:polished_blackstone"}),
        "totem_reliquias": ([" Q ", "QBQ", "BBB"], {"Q": "minecraft:quartz", "B": "minecraft:polished_blackstone"}),
    }

    for name, (pattern, key) in shaped.items():
        write_json(os.path.join(DATA, "recipe", f"{name}.json"), {
            "type": "minecraft:crafting_shaped",
            "pattern": pattern,
            "key": dict(key),
            "result": {"id": f"villaeconomy:{name}", "count": 1},
        })

    merges = {
        "nota20_de_nota10": ("villaeconomy:nota20", ["villaeconomy:nota10", "villaeconomy:nota10"]),
        "nota50_de_nota10": ("villaeconomy:nota50", ["villaeconomy:nota10"] * 5),
        "nota100_de_nota50": ("villaeconomy:nota100", ["villaeconomy:nota50", "villaeconomy:nota50"]),
        "nota200_de_nota100": ("villaeconomy:nota200", ["villaeconomy:nota100", "villaeconomy:nota100"]),
    }

    for name, (result, ingredients) in merges.items():
        write_json(os.path.join(DATA, "recipe", f"{name}.json"), {
            "type": "minecraft:crafting_shapeless",
            "ingredients": ingredients,
            "result": {"id": result, "count": 1},
        })


# --------------------------------------------------------------------------- trocas
M = lambda value: f"villaeconomy:nota{value}"  # noqa: E731

TRADES = {
    "banqueiro": {
        1: [(M(10), 5, M(50), 1, 12), (M(50), 1, M(10), 5, 12)],
        2: [(M(50), 2, M(100), 1, 12), (M(100), 2, M(200), 1, 10)],
        3: [(M(200), 1, M(100), 2, 10), (M(10), 10, M(100), 1, 8)],
        4: [(M(100), 3, "minecraft:diamond", 1, 6), (M(200), 1, "minecraft:gold_block", 1, 8)],
        5: [(M(200), 5, "minecraft:netherite_scrap", 1, 4), (M(200), 2, "minecraft:emerald", 6, 6)],
    },
    "mineiro": {
        1: [(M(10), 2, "minecraft:coal", 6, 16), (M(10), 3, "minecraft:raw_iron", 3, 16)],
        2: [(M(20), 4, "minecraft:iron_ingot", 2, 12), (M(10), 2, "minecraft:copper_ingot", 4, 16)],
        3: [(M(100), 1, "minecraft:iron_pickaxe", 1, 6), (M(50), 2, "minecraft:raw_gold", 3, 12)],
        4: [(M(200), 1, "minecraft:diamond", 2, 6), (M(100), 2, "minecraft:lapis_lazuli", 8, 10)],
        5: [(M(200), 3, "minecraft:diamond_pickaxe", 1, 4), (M(200), 2, "minecraft:ancient_debris", 1, 4)],
    },
    "alquimista": {
        1: [(M(10), 2, "minecraft:glass_bottle", 3, 16), (M(20), 1, "minecraft:glowstone_dust", 4, 16)],
        2: [(M(20), 2, "minecraft:blaze_powder", 2, 12), (M(50), 1, "minecraft:gunpowder", 6, 12)],
        3: [(M(50), 2, "minecraft:experience_bottle", 3, 8), (M(100), 1, "minecraft:magma_cream", 3, 8)],
        4: [(M(100), 2, "minecraft:ender_pearl", 2, 6), (M(200), 1, "minecraft:blaze_rod", 3, 6)],
        5: [(M(200), 2, "minecraft:ghast_tear", 2, 4), (M(200), 3, "minecraft:netherite_scrap", 1, 4)],
    },
    "joalheiro": {
        1: [(M(20), 2, "minecraft:amethyst_shard", 3, 16), (M(10), 1, "minecraft:lapis_lazuli", 4, 16)],
        2: [(M(100), 1, "minecraft:emerald", 2, 12), (M(50), 2, "minecraft:gold_ingot", 3, 12)],
        3: [(M(200), 1, "minecraft:diamond", 1, 8), (M(100), 2, "minecraft:gold_block", 1, 8)],
        4: [(M(200), 2, "minecraft:diamond", 3, 6), (M(200), 3, "minecraft:emerald_block", 1, 6)],
        5: [(M(200), 4, "minecraft:diamond_block", 1, 4), (M(200), 5, "minecraft:netherite_ingot", 1, 3)],
    },
    "guarda": {
        1: [(M(10), 1, "minecraft:arrow", 12, 20), (M(20), 1, "minecraft:iron_sword", 1, 12)],
        2: [(M(50), 1, "minecraft:shield", 1, 10), (M(20), 2, "minecraft:iron_chestplate", 1, 8)],
        3: [(M(100), 1, "minecraft:iron_helmet", 1, 8), (M(50), 2, "minecraft:iron_block", 1, 8)],
        4: [(M(200), 1, "minecraft:iron_golem_spawn_egg", 1, 4),
            (M(100), 2, "minecraft:golden_apple", 2, 6)],
        5: [(M(200), 3, "minecraft:tnt", 8, 6), (M(200), 2, "minecraft:enchanted_golden_apple", 1, 3)],
    },
    "leiloeiro": {
        1: [(M(50), 1, "minecraft:experience_bottle", 2, 16), (M(20), 2, "minecraft:book", 3, 16)],
        2: [(M(100), 1, "minecraft:name_tag", 1, 10), (M(50), 2, "minecraft:saddle", 1, 8)],
        3: [(M(200), 1, "minecraft:golden_apple", 2, 8), (M(100), 3, "minecraft:echo_shard", 1, 6)],
        4: [(M(200), 2, "minecraft:enchanted_golden_apple", 1, 5), (M(200), 3, "minecraft:totem_of_undying", 1, 3)],
        5: [(M(200), 5, "minecraft:enchanted_golden_apple", 2, 4),
            (M(200), 6, "minecraft:netherite_ingot", 2, 3)],
    },
    "tesoureiro": {
        1: [(M(100), 2, "minecraft:diamond", 1, 16), (M(50), 1, "minecraft:gold_block", 1, 16)],
        2: [(M(200), 1, "minecraft:diamond_block", 1, 12), (M(100), 3, "minecraft:emerald_block", 1, 12)],
        3: [(M(200), 2, "minecraft:netherite_scrap", 2, 8), (M(200), 1, "minecraft:ancient_debris", 1, 8)],
        4: [(M(200), 4, "minecraft:netherite_ingot", 1, 6), (M(200), 3, "minecraft:enchanted_book", 1, 6)],
        5: [(M(200), 8, "minecraft:netherite_block", 1, 3), (M(200), 6, "minecraft:elytra", 1, 2)],
    },
    # ---- Nether: tudo em ouro ----
    "banqueiro_nether": {
        1: [("minecraft:gold_ingot", 1, M(10), 4, 16), (M(10), 5, "minecraft:gold_ingot", 1, 16)],
        2: [("minecraft:gold_ingot", 4, M(10), 16, 12), (M(20), 5, "minecraft:gold_nugget", 8, 12)],
        3: [("minecraft:gold_block", 1, M(100), 4, 8), (M(50), 4, "minecraft:gold_ingot", 1, 10)],
        4: [("minecraft:gold_block", 3, M(200), 2, 6), (M(200), 1, "minecraft:gold_block", 2, 6)],
        5: [("minecraft:gold_block", 8, M(200), 5, 4), (M(200), 4, "minecraft:gold_block", 3, 4)],
    },
    "ferreiro_ouro": {
        1: [("minecraft:gold_ingot", 4, "minecraft:golden_sword", 1, 16),
            ("minecraft:gold_nugget", 8, "minecraft:gold_ingot", 1, 16)],
        2: [("minecraft:gold_ingot", 6, "minecraft:golden_helmet", 1, 12),
            ("minecraft:gold_ingot", 5, "minecraft:golden_boots", 1, 12)],
        3: [("minecraft:gold_ingot", 8, "minecraft:golden_chestplate", 1, 8),
            ("minecraft:gold_block", 1, "minecraft:golden_apple", 2, 8)],
        4: [("minecraft:gold_block", 2, "minecraft:golden_pickaxe", 1, 6),
            ("minecraft:gold_ingot", 12, "minecraft:enchanted_golden_apple", 1, 5)],
        5: [("minecraft:gold_block", 4, "minecraft:netherite_scrap", 2, 4),
            ("minecraft:gold_block", 6, "minecraft:golden_apple", 4, 4)],
    },
    "cacador_reliquias": {
        1: [("minecraft:gold_ingot", 2, "minecraft:quartz", 4, 16),
            ("minecraft:gold_ingot", 3, "minecraft:blaze_powder", 2, 16)],
        2: [("minecraft:gold_ingot", 6, "minecraft:blaze_rod", 1, 12),
            ("minecraft:gold_ingot", 4, "minecraft:magma_cream", 2, 12)],
        3: [("minecraft:gold_ingot", 10, "minecraft:ender_pearl", 1, 8),
            ("minecraft:gold_block", 1, "minecraft:ghast_tear", 2, 8)],
        4: [("minecraft:gold_block", 2, "minecraft:ancient_debris", 1, 6),
            ("minecraft:gold_block", 1, "minecraft:obsidian", 8, 6)],
        5: [("minecraft:gold_block", 3, "minecraft:netherite_scrap", 2, 4),
            ("minecraft:gold_block", 5, "minecraft:nether_star", 1, 2)],
    },
}


def generate_trades():
    for profession, levels in TRADES.items():
        for level, offers in levels.items():
            values = []

            for index, (wants, wants_count, gives, gives_count, max_uses) in enumerate(offers):
                trade_id = f"villaeconomy:{profession}/{level}/{index}"
                values.append(trade_id)
                write_json(os.path.join(DATA, "villager_trade", profession, str(level), f"{index}.json"), {
                    "wants": {"id": wants, "count": wants_count},
                    "gives": {"id": gives, "count": gives_count},
                    "max_uses": max_uses,
                    "xp": level,
                    "reputation_discount": 0.05,
                })

            write_json(os.path.join(DATA, "tags", "villager_trade", profession, f"level_{level}.json"),
                       {"values": values})
            write_json(os.path.join(DATA, "trade_set", profession, f"level_{level}.json"), {
                "amount": min(3, max(1, len(values))),
                "random_sequence": f"villaeconomy:trade_set/{profession}/level_{level}",
                "trades": f"#villaeconomy:{profession}/level_{level}",
            })


# --------------------------------------------------------------------------- worldgen
def generate_worldgen():
    write_json(os.path.join(DATA, "worldgen", "feature", "nether_village.json"),
               {"type": "villaeconomy:nether_village"})
    write_json(os.path.join(DATA, "worldgen", "placed_feature", "nether_village.json"), {
        "feature": "villaeconomy:nether_village",
        "placement": [
            {"type": "minecraft:rarity_filter", "chance": 16},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {
                "type": "minecraft:uniform",
                "min_inclusive": {"absolute": 34},
                "max_inclusive": {"absolute": 90},
            }},
            {"type": "minecraft:biome"},
        ],
    })


# --------------------------------------------------------------------------- línguas
def generate_languages():
    professions_pt = {
        "banqueiro": "Banqueiro",
        "mineiro": "Mineiro",
        "alquimista": "Alquimista",
        "joalheiro": "Joalheiro",
        "guarda": "Guarda",
        "leiloeiro": "Leiloeiro",
        "tesoureiro": "Tesoureiro",
        "banqueiro_nether": "Banqueiro do Nether",
        "ferreiro_ouro": "Ferreiro do Ouro",
        "cacador_reliquias": "Caçador de Relíquias",
    }
    professions_en = {
        "banqueiro": "Banker",
        "mineiro": "Miner",
        "alquimista": "Alchemist",
        "joalheiro": "Jeweler",
        "guarda": "Guard",
        "leiloeiro": "Auctioneer",
        "tesoureiro": "Treasurer",
        "banqueiro_nether": "Nether Banker",
        "ferreiro_ouro": "Gold Smith",
        "cacador_reliquias": "Relic Hunter",
    }
    blocks_pt = {
        "balcao_banqueiro": "Balcão do Banqueiro",
        "mesa_mineracao": "Mesa de Mineração",
        "alambique": "Alambique",
        "bancada_joalheiro": "Bancada do Joalheiro",
        "posto_guarda": "Posto de Guarda",
        "podio_leilao": "Pódio de Leilão",
        "cofre_tesoureiro": "Cofre do Tesoureiro",
        "balcao_nether": "Balcão do Nether",
        "forja_ouro": "Forja de Ouro",
        "totem_reliquias": "Totem de Relíquias",
    }
    blocks_en = {
        "balcao_banqueiro": "Banker's Counter",
        "mesa_mineracao": "Mining Bench",
        "alambique": "Alembic",
        "bancada_joalheiro": "Jeweler's Bench",
        "posto_guarda": "Guard Post",
        "podio_leilao": "Auction Podium",
        "cofre_tesoureiro": "Treasurer's Vault",
        "balcao_nether": "Nether Counter",
        "forja_ouro": "Gold Forge",
        "totem_reliquias": "Relic Totem",
    }

    for lang, professions, prefix, mission_line, group in (
            ("pt_br", professions_pt, "Vila & Câmbio", "%s §f%s §7x§e%s §8(%s) §7→ %s", "Vilas & Câmbio"),
            ("en_us", professions_en, "Village & Currency", "%s §f%s §7x§e%s §8(%s) §7→ %s",
             "Villages & Currency")):
        data = {
            "itemGroup.villaeconomy": group,
            "entity.villaeconomy.nether_villager": "Aldeão do Nether" if lang == "pt_br" else "Nether Villager",
            "item.villaeconomy.nether_villager_spawn_egg":
                "Ovo de Aldeão do Nether" if lang == "pt_br" else "Nether Villager Spawn Egg",
            "villaeconomy.mission.line": mission_line,
            "villaeconomy.mission.type.matar": "Matar" if lang == "pt_br" else "Kill",
            "villaeconomy.mission.type.minerar": "Minerar" if lang == "pt_br" else "Mine",
            "villaeconomy.mission.type.entregar": "Entregar" if lang == "pt_br" else "Deliver",
        }

        for value in NOTES:
            data[f"item.villaeconomy.nota{value}"] = (f"Nota de ${value}" if lang == "pt_br"
                                                     else f"${value} Bill")

        for name, label in blocks_pt.items():
            data[f"block.villaeconomy.{name}"] = label if lang == "pt_br" else blocks_en[name]

        for name, label in professions.items():
            data[f"villaeconomy.profession.{name}"] = label

        write_json(os.path.join(ASSETS, "lang", f"{lang}.json"), data)


def main():
    make_icon()

    for value in NOTES:
        make_note(value)

    for name in BLOCKS:
        make_block_texture(name)

    make_profession_textures()
    make_nether_type_texture()

    generate_item_models()
    generate_block_resources()
    generate_recipes()
    generate_trades()
    generate_worldgen()
    generate_languages()

    print("recursos gerados")


if __name__ == "__main__":
    main()
