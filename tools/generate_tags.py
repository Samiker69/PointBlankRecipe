#!/usr/bin/env python3
"""Generate one item tag per modular slot: pointblank_recipe:parts/<slot>.

Usage: python3 tools/generate_tags.py
Only items registered in ModItems.ALL_ITEMS are tagged, so a tag never references a missing item.
"""
import json
import re
from collections import defaultdict
from pathlib import Path

from recipe_index import CATEGORIES, component_category

ROOT = Path(__file__).resolve().parent.parent
MOD_ITEMS = ROOT / "sharedConstants/src/main/java/xyz/samiker/pointblank_recipe/item/ModItems.java"
TAGS = ROOT / "shared_resources/data/pointblank_recipe/tags/item/parts"
# Raw materials are not weapon parts; copper nuggets already have c:nuggets/copper.
SKIPPED = {"material"}


def registered_items():
    source = MOD_ITEMS.read_text()
    constants = dict(re.findall(r'String (\w+) = "(\w+)";', source))
    all_items = re.search(r"ALL_ITEMS = List\.of\((.*?)\);", source, re.S).group(1)
    return [constants[name] for name in re.findall(r"\w+", all_items)]


def main():
    by_slot = defaultdict(list)
    for item in registered_items():
        slot = component_category(item)
        if slot == "unknown":
            raise SystemExit(f"no slot for {item}, add a rule to CATEGORIES in recipe_index.py")
        if slot not in SKIPPED:
            by_slot[slot].append(f"pointblank_recipe:{item}")

    TAGS.mkdir(parents=True, exist_ok=True)
    for slot, _ in CATEGORIES:
        if slot in by_slot:
            tag = {"replace": False, "values": sorted(by_slot[slot])}
            (TAGS / f"{slot}.json").write_text(json.dumps(tag, indent=2) + "\n")
            print(f"{slot:<14} {len(by_slot[slot])}")


if __name__ == "__main__":
    main()
