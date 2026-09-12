package xyz.samiker.pointblank_recipe.utils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

public final class ItemRegistry {
    private ItemRegistry() {
    }

    public static Item simple(String id) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(PointblankRecipe.MOD_ID, id)
        );
        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                new Item(new Item.Properties().setId(key))
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