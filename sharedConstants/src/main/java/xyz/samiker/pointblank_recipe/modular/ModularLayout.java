package xyz.samiker.pointblank_recipe.modular;

import java.util.List;

/**
 * Where everything sits in the modular workbench screen, shared by all platforms. Each part slot has a fixed place on
 * a gun pointing left (muzzle on the left, stock on the right, magazine and grip below), and a template only shows
 * its own slots.
 */
public final class ModularLayout {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 186;
    public static final int INVENTORY_Y = 104;

    public static final int RESULT_X = 148;
    public static final int RESULT_Y = 46;

    /** Every slot the workbench has, in container order. */
    public static final List<String> SLOTS = List.of(
            "frame", "barrel", "magazine", "stock", "grip", "sight", "muzzle_device", "mechanism", "special",
            ModularTemplate.BASE_SLOT
    );

    private ModularLayout() {
    }

    /** Top-left corner of the item area of a slot, as {x, y}. */
    public static int[] slotPosition(String slot) {
        return switch (slot) {
            case "muzzle_device" -> new int[]{10, 46};
            case "barrel" -> new int[]{32, 46};
            case "frame", ModularTemplate.BASE_SLOT -> new int[]{58, 46};
            case "stock" -> new int[]{96, 46};
            case "sight" -> new int[]{58, 22};
            case "special" -> new int[]{80, 22};
            case "mechanism" -> new int[]{36, 70};
            case "magazine" -> new int[]{58, 70};
            case "grip" -> new int[]{80, 70};
            default -> throw new IllegalArgumentException("Unknown slot " + slot);
        };
    }
}
