#!/usr/bin/env python3
"""Prototype: convert every weapon and component recipe into the draft modular format and check nothing is lost.

Usage: python3 tools/recipe_index.py && python3 tools/modular_draft.py
Writes docs/modular-crafting/draft-preview.json and one file per weapon into
shared_resources/data/pointblank_recipe/modular_recipe/ (same file name as the source recipe).
Copper nugget recipes are skipped: they turn ingots into a raw material, not a weapon part.
"""
import json
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "docs/modular-crafting"
MODULAR_RECIPES = ROOT / "shared_resources/data/pointblank_recipe/modular_recipe"

FRAME_TEMPLATES = {
    "theframeofthegun": "pistol",
    "frameofashot": "pistol",
    "frameoftherifle": "rifle",
    "framem4a1type": "rifle",
    "frameofsniperifle": "sniper",
    "sniperriflebillettrauma": "sniper",
    "frameofshotgun": "shotgun",
    "framemktype": "machinegun",
    "frameofsubmachinegun": "smg",
    "framep90": "smg",
    "frameofat4": "launcher",
    "frameofm32mgl": "launcher",
    "framerpg": "launcher",
    "m79": "launcher",
    "framecyberpunkgun": "cyberpunk",
    "framecyberpunkenergygun": "cyberpunk",
}
# Frames made for a single gun (Metro 2033 etc.) and recipes without a frame.
NO_FRAME_TEMPLATES = {"pointblank:gatling": "unique", "pointblank:hellriegel": "unique", "pointblank:ventil": "unique"}


def convert(recipe, components):
    parts, materials, base = defaultdict(list), [], None
    for ingredient, count in recipe["ingredients"].items():
        # Components have no slots: everything they are made of, including other components, is a material.
        category = None if recipe["kind"] == "component" else components.get(ingredient, {}).get("category")
        entry = {"tag": ingredient[1:]} if ingredient.startswith("#") else {"item": ingredient}
        if count > 1:
            entry["count"] = count
        if ingredient.startswith("pointblank:"):
            base = entry
        elif category:
            parts[category].append(entry)
        else:
            materials.append(entry)

    if recipe["kind"] == "component":
        template = "component"
    elif base:
        template = "upgrade"
    elif "frame" in parts:
        frame = parts["frame"][0]["item"].split(":", 1)[1]
        template = FRAME_TEMPLATES.get(frame, "unique")
    else:
        template = NO_FRAME_TEMPLATES[recipe["result"]]

    draft = {
        "type": "pointblank_recipe:modular",
        "fabric:load_conditions": [
            {"condition": "pointblank_recipe:module_enabled", "module": m} for m in recipe["modules"]
        ],
        "template": "pointblank_recipe:" + template,
    }
    if base:
        draft["base"] = base
    # A workbench slot holds one stack; a list in "parts" means "any of these", so two different parts cannot share a slot.
    for category, entries in parts.items():
        if len(entries) > 1:
            raise SystemExit(f"{recipe['file']}: several different {category} parts, they do not fit one slot")
    draft["parts"] = {category: entries[0] for category, entries in parts.items()}
    draft["materials"] = materials
    draft["result"] = {"count": recipe["count"], "id": recipe["result"]}
    return draft


def flatten(draft):
    """Back to {ingredient: count}, to prove the conversion is lossless."""
    entries = list(draft["materials"]) + ([draft["base"]] if "base" in draft else [])
    for value in draft["parts"].values():
        entries += value if isinstance(value, list) else [value]
    return {("#" + e["tag"]) if "tag" in e else e["item"]: e.get("count", 1) for e in entries}


def main():
    index = json.loads((OUT / "recipes.json").read_text())
    components = dict(index["components"])
    # Used by four recipes but has no recipe of its own, so recipe_index.py does not list it.
    components["pointblank_recipe:rifleflamearrester"] = {"category": "muzzle_device"}

    weapons = [
        r for r in index["recipes"]
        if r["kind"] == "weapon" or (r["kind"] == "component" and components[r["result"]]["category"] != "material")
    ]
    drafts, lossy = {}, []
    slots = defaultdict(lambda: {"count": 0, "seen": Counter()})
    for recipe in weapons:
        draft = convert(recipe, components)
        if flatten(draft) != recipe["ingredients"]:
            lossy.append(recipe["file"])
        drafts[recipe["file"]] = draft
        stats = slots[draft["template"]]
        stats["count"] += 1
        stats["seen"].update(draft["parts"].keys())

    templates = {
        name: {
            "recipes": s["count"],
            "required": sorted(c for c, n in s["seen"].items() if n == s["count"]),
            "optional": sorted(c for c, n in s["seen"].items() if n < s["count"]),
        }
        for name, s in sorted(slots.items())
    }
    (OUT / "draft-preview.json").write_text(json.dumps({"templates": templates, "recipes": drafts}, indent=2) + "\n")
    MODULAR_RECIPES.mkdir(parents=True, exist_ok=True)
    for name, draft in drafts.items():
        (MODULAR_RECIPES / name).write_text(json.dumps(draft, indent=2) + "\n")
    print(f"converted {len(drafts)}/{len(weapons)} recipes, lossy: {lossy or 'none'}")
    for name, t in templates.items():
        print(f"  {name:<28} {t['recipes']:>3}  required={t['required']}  optional={t['optional']}")


if __name__ == "__main__":
    main()
