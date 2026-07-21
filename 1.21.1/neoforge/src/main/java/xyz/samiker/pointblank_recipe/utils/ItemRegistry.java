package xyz.samiker.pointblank_recipe.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.RegisterEvent;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

public final class ItemRegistry {
    public static RegisterEvent.RegisterHelper<Item> helper;

    private ItemRegistry() {}

    public static Item simple(String id) {
        Item item = new Item(new Item.Properties());
        if (helper != null) {
            helper.register(ResourceLocation.tryBuild(PointblankRecipe.MOD_ID, id), item);
        }
        return item;
    }

    public static Item[] simpleAll(String... ids) {
        Item[] items = new Item[ids.length];
        for (int i = 0; i < ids.length; i++) {
            items[i] = simple(ids[i]);
        }
        return items;
    }
}