package xyz.samiker.pointblank_recipe.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchScreenHandler;

public final class ModBlocks {
    public static final Identifier MODULAR_WORKBENCH_ID = Identifier.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "modular_workbench");

    public static Block MODULAR_WORKBENCH;
    public static Item MODULAR_WORKBENCH_ITEM;
    public static MenuType<ModularWorkbenchScreenHandler> MODULAR_WORKBENCH_SCREEN;

    private ModBlocks() {
    }

    public static void registerAll() {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, MODULAR_WORKBENCH_ID);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, MODULAR_WORKBENCH_ID);
        MODULAR_WORKBENCH = Registry.register(BuiltInRegistries.BLOCK, blockKey,
                new ModularWorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(blockKey)));
        MODULAR_WORKBENCH_ITEM = Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(MODULAR_WORKBENCH, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        MODULAR_WORKBENCH_SCREEN = Registry.register(BuiltInRegistries.MENU, MODULAR_WORKBENCH_ID,
                new MenuType<>(ModularWorkbenchScreenHandler::new, FeatureFlags.VANILLA_SET));
    }
}
