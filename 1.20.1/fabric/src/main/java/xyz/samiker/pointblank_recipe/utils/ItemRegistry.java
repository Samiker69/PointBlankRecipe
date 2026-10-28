package xyz.samiker.pointblank_recipe.utils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

public final class ItemRegistry {
    private ItemRegistry() {
    }

    public static Item simple(String id) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.tryBuild(PointblankRecipe.MOD_ID, id),
                new Item(new Item.Properties())
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