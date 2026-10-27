package xyz.samiker.pointblank_recipe.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import xyz.samiker.pointblank_recipe.block.ModBlocks;

public class Pointblank_recipeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModBlocks.MODULAR_WORKBENCH_SCREEN, ModularWorkbenchScreen::new);
    }
}
