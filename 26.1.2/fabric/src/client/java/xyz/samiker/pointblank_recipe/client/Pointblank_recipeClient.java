package xyz.samiker.pointblank_recipe.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import xyz.samiker.pointblank_recipe.block.ModBlocks;

public class Pointblank_recipeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModBlocks.MODULAR_WORKBENCH_MENU, ModularWorkbenchScreen::new);
    }
}
