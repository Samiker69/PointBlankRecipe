package xyz.samiker.pointblank_recipe.client;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import xyz.samiker.pointblank_recipe.block.ModBlocks;

// Only registered on the client, so a dedicated server never loads screen classes.
public final class ClientSetup {
    private ClientSetup() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModBlocks.MODULAR_WORKBENCH_MENU, ModularWorkbenchScreen::new);
    }
}
