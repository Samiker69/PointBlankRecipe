#!/usr/bin/env python3
"""Parse every recipe in shared_resources and build an index for modular crafting.

Usage: python3 tools/recipe_index.py
Writes docs/modular-crafting/recipes.json (every recipe and the category of every component) and prints checks.
"""
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RECIPES = ROOT / "shared_resources/data/pointblank_recipe/recipe"
OUT = ROOT / "docs/modular-crafting"

# First matching rule wins, so order matters (e.g. "framegunmagazine" is a magazine part, not a frame).
CATEGORIES = [
    ("magazine", r"magazine"),
    ("frame", r"frame|billet|^m79$"),
    ("barrel", r"barrel|muzzle|gunpoint"),
    ("stock", r"butt"),
    ("grip", r"handle"),
    ("sight", r"scope"),
    ("muzzle_device", r"silencer|flamearrester"),
    ("mechanism", r"mechanism|reloader|guidance"),
    ("special", r"energycube|basisfortherocket"),
    ("material", r"nugget|ingot"),
]


def component_category(name):
    for category, pattern in CATEGORIES:
        if re.search(pattern, name):
            return category
    return "unknown"


def result_kind(item_id):
    namespace, name = item_id.split(":", 1)
    if namespace == "pointblank_recipe":
        return "component"
    # Anything outside vanilla/Create is a Point Blank item; this also keeps typos like "pointbank:rpk" in the index.
    if namespace not in ("minecraft", "create"):
        # S.T.A.L.K.E.R. cartridges are named by caliber (s_545x39, s_9x39sp5), s_gausa is the gauss battery.
        if re.search(r"ammo|rocket|^grenade|^s_\d|^s_gausa$", name):
            return "ammo"
        return "weapon"
    return "material"


def ingredient_id(ingredient):
    return "#" + ingredient["tag"] if "tag" in ingredient else ingredient["item"]


def parse(path):
    data = json.loads(path.read_text())
    if data["type"] == "minecraft:crafting_shaped":
        counts = Counter(ch for row in data["pattern"] for ch in row if ch != " ")
        ingredients = {ingredient_id(data["key"][ch]): n for ch, n in counts.items()}
    else:
        ingredients = dict(Counter(ingredient_id(i) for i in data["ingredients"]))
    result = data["result"]["id"]
    return {
        "file": path.name,
        "type": data["type"].split(":")[1],
        "modules": [c["module"] for c in data.get("fabric:load_conditions", [])],
        "result": result,
        "count": data["result"].get("count", 1),
        "kind": result_kind(result),
        "pattern": data.get("pattern"),
        "ingredients": ingredients,
    }


def main():
    recipes = [parse(p) for p in sorted(RECIPES.glob("*.json"))]

    used_in = defaultdict(list)
    for r in recipes:
        for ing in r["ingredients"]:
            if ing.startswith("pointblank_recipe:"):
                used_in[ing].append(r["result"])

    components = {}
    for r in recipes:
        if r["kind"] != "component":
            continue
        name = r["result"].split(":", 1)[1]
        components[r["result"]] = {
            "category": component_category(name),
            "modules": r["modules"],
            "recipe": r["file"],
            "used_in": sorted(set(used_in[r["result"]])),
        }

    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "recipes.json").write_text(
        json.dumps({"recipes": recipes, "components": components}, indent=2) + "\n"
    )

    kinds = Counter(r["kind"] for r in recipes)
    print(f"recipes: {len(recipes)} (" + ", ".join(f"{k}: {n}" for k, n in sorted(kinds.items())) + ")")
    print("components by category:", dict(Counter(info["category"] for info in components.values())))

    unused = sorted(c for c, info in components.items() if not info["used_in"])
    no_components = sorted(
        r["result"] for r in recipes
        if r["kind"] == "weapon" and not any(i.startswith("pointblank_recipe:") for i in r["ingredients"])
    )
    uncraftable = sorted(set(used_in) - set(components))
    print("components used in recipes but with no recipe of their own:", ", ".join(uncraftable) or "none")
    print("components not used in any recipe:", ", ".join(unused) or "none")
    print(f"weapons crafted without any component ({len(no_components)}):", ", ".join(no_components) or "none")

if __name__ == "__main__":
    main()
