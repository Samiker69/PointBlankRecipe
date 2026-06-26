package xyz.samiker.pointblank_recipe.utils;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

public final class ItemRegistry {
    private ItemRegistry() {
    }

    public static Item simple(String id) {
        return Registry.register(
                Registries.ITEM,
                Identifier.of(PointblankRecipe.MOD_ID, id),
                new Item(new Item.Settings())
        );
    }

    public static Item[] simpleAll(String... ids) {
        Item[] items = new Item[ids.length];
        for (int i = 0; i < ids.length; i++) {
            items[i] = simple(ids[i]);
        }
        return items;
    }
}