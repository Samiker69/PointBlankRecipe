package xyz.samiker.pointblank_recipe.item;

import net.minecraft.world.item.Item;
import xyz.samiker.pointblank_recipe.utils.ItemRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collection;

public final class PlatformItems {
    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    public static void registerAll() {
        for (String id : ModItems.ALL_ITEMS) {
            ITEMS.put(id, ItemRegistry.simple(id));
        }
    }

    public static Item get(String id) {
        return ITEMS.get(id);
    }

    public static Collection<Item> getItems() {
        return ITEMS.values();
    }
}