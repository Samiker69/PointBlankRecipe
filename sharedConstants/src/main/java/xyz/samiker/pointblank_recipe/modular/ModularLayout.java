package xyz.samiker.pointblank_recipe.modular;

import java.util.ArrayList;
import java.util.List;

/**
 * Where everything sits in the modular workbench screen, shared by all platforms. Each part slot has a fixed place on
 * a gun pointing left (muzzle on the left, stock on the right, magazine and grip below), and a template only shows
 * its own slots on top of a silhouette built from those same parts.
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

    public record Rect(int x1, int y1, int x2, int y2) {
    }

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

    /** Colors of the gun silhouette: every rect is drawn 1px larger in the outline color first, then filled. */
    public static final int SILHOUETTE_OUTLINE = 0xFF2B2B2B;
    public static final int SILHOUETTE_FILL = 0xFF4E4E4E;

    /** The gun shape drawn behind the slots of a template. */
    public static List<Rect> silhouette(ModularTemplate template) {
        List<Rect> shape = new ArrayList<>();
        switch (template.name()) {
            case "launcher" -> shape.add(new Rect(4, 42, 132, 66));
            case "shotgun" -> shape.add(new Rect(24, 58, 52, 66));
            case "sniper" -> shape.add(new Rect(2, 51, 26, 57));
            default -> {
            }
        }
        for (String slot : template.slots()) {
            switch (slot) {
                case "muzzle_device" -> shape.add(new Rect(4, 48, 26, 60));
                case "barrel" -> shape.add(new Rect(22, 50, 52, 58));
                case "frame", ModularTemplate.BASE_SLOT -> shape.add(new Rect(48, 40, 98, 64));
                case "stock" -> {
                    shape.add(new Rect(94, 44, 128, 58));
                    shape.add(new Rect(116, 42, 132, 70));
                }
                case "sight" -> {
                    shape.add(new Rect(50, 36, 96, 40));
                    shape.add(new Rect(48, 24, 86, 36));
                }
                case "special" -> shape.add(new Rect(80, 20, 102, 40));
                case "mechanism" -> shape.add(new Rect(30, 60, 58, 84));
                case "magazine" -> shape.add(new Rect(56, 62, 78, 92));
                case "grip" -> shape.add(new Rect(78, 62, 98, 92));
                default -> {
                }
            }
        }
        return shape;
    }
}
