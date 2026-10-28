package xyz.samiker.pointblank_recipe.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
    public static final ResourceLocation MODULAR_WORKBENCH_ID = ResourceLocation.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "modular_workbench");

    public static Block MODULAR_WORKBENCH;
    public static Item MODULAR_WORKBENCH_ITEM;
    public static MenuType<ModularWorkbenchScreenHandler> MODULAR_WORKBENCH_SCREEN;

    private ModBlocks() {
    }

    public static void registerAll() {
        MODULAR_WORKBENCH = Registry.register(BuiltInRegistries.BLOCK, MODULAR_WORKBENCH_ID,
                new ModularWorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)));
        MODULAR_WORKBENCH_ITEM = Registry.register(BuiltInRegistries.ITEM, MODULAR_WORKBENCH_ID,
                new BlockItem(MODULAR_WORKBENCH, new Item.Properties()));
        MODULAR_WORKBENCH_SCREEN = Registry.register(BuiltInRegistries.MENU, MODULAR_WORKBENCH_ID,
                new MenuType<>(ModularWorkbenchScreenHandler::new, FeatureFlags.VANILLA_SET));
    }
}
