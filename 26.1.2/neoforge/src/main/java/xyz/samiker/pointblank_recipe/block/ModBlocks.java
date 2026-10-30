package xyz.samiker.pointblank_recipe.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.RegisterEvent;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchMenu;

public final class ModBlocks {
    public static final Identifier MODULAR_WORKBENCH_ID = Identifier.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "modular_workbench");

    public static Block MODULAR_WORKBENCH;
    public static Item MODULAR_WORKBENCH_ITEM;
    public static MenuType<ModularWorkbenchMenu> MODULAR_WORKBENCH_MENU;

    private ModBlocks() {
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, MODULAR_WORKBENCH_ID);
            MODULAR_WORKBENCH = new ModularWorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(key)
                    // The model is not a full cube, so it must not hide the faces of blocks next to it.
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK));
            helper.register(key, MODULAR_WORKBENCH);
        });
        event.register(Registries.ITEM, helper -> {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MODULAR_WORKBENCH_ID);
            MODULAR_WORKBENCH_ITEM = new BlockItem(MODULAR_WORKBENCH, new Item.Properties().setId(key).useBlockDescriptionPrefix());
            helper.register(key, MODULAR_WORKBENCH_ITEM);
        });
        event.register(Registries.MENU, helper -> {
            MODULAR_WORKBENCH_MENU = new MenuType<>(ModularWorkbenchMenu::new, FeatureFlags.VANILLA_SET);
            helper.register(MODULAR_WORKBENCH_ID, MODULAR_WORKBENCH_MENU);
        });
    }
}
