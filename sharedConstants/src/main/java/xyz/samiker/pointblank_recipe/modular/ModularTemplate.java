package xyz.samiker.pointblank_recipe.modular;

import java.util.List;

/**
 * A weapon type shown in the modular workbench. Its slots are laid out in this order in the parts grid.
 * Only the frame is required; a slot list here is every part recipes of this type use.
 */
public record ModularTemplate(String name, List<String> slots) {
    public static final String BASE_SLOT = "base";
    public static final int MAX_SLOTS = 9;

    public static final List<ModularTemplate> ALL = List.of(
            new ModularTemplate("pistol", List.of("frame", "barrel", "magazine", "mechanism", "sight", "special")),
            new ModularTemplate("rifle", List.of("frame", "barrel", "magazine", "stock", "grip", "sight", "muzzle_device", "mechanism", "special")),
            new ModularTemplate("sniper", List.of("frame", "barrel", "magazine", "stock", "sight", "muzzle_device", "mechanism", "special")),
            new ModularTemplate("shotgun", List.of("frame", "barrel", "stock", "sight", "mechanism")),
            new ModularTemplate("machinegun", List.of("frame", "barrel", "magazine", "stock", "grip", "sight")),
            new ModularTemplate("smg", List.of("frame", "barrel", "magazine", "stock", "grip", "muzzle_device", "special")),
            new ModularTemplate("launcher", List.of("frame", "stock", "grip", "sight", "mechanism")),
            new ModularTemplate("cyberpunk", List.of("frame", "sight", "special")),
            new ModularTemplate("unique", List.of("frame", "barrel", "magazine", "stock", "mechanism", "muzzle_device")),
            new ModularTemplate("upgrade", List.of(BASE_SLOT, "barrel")),
            new ModularTemplate("component", List.of())
    );

    public static ModularTemplate byIndex(int index) {
        return ALL.get(Math.floorMod(index, ALL.size()));
    }

    public static int indexOf(String name) {
        for (int i = 0; i < ALL.size(); i++) {
            if (ALL.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /** Slot name at a position of the parts grid, or null when the position is unused by this template. */
    public String slotAt(int position) {
        return position < slots.size() ? slots.get(position) : null;
    }
}
