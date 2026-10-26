package xyz.samiker.pointblank_recipe.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchScreenHandler;

public final class ModBlocks {
    public static final Identifier MODULAR_WORKBENCH_ID = Identifier.of(PointblankRecipe.MOD_ID, "modular_workbench");

    public static Block MODULAR_WORKBENCH;
    public static Item MODULAR_WORKBENCH_ITEM;
    public static ScreenHandlerType<ModularWorkbenchScreenHandler> MODULAR_WORKBENCH_SCREEN;

    private ModBlocks() {
    }

    public static void registerAll() {
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, MODULAR_WORKBENCH_ID);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, MODULAR_WORKBENCH_ID);
        MODULAR_WORKBENCH = Registry.register(Registries.BLOCK, blockKey,
                new ModularWorkbenchBlock(AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE).registryKey(blockKey)));
        MODULAR_WORKBENCH_ITEM = Registry.register(Registries.ITEM, itemKey,
                new BlockItem(MODULAR_WORKBENCH, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey()));
        MODULAR_WORKBENCH_SCREEN = Registry.register(Registries.SCREEN_HANDLER, MODULAR_WORKBENCH_ID,
                new ScreenHandlerType<>(ModularWorkbenchScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    }
}
