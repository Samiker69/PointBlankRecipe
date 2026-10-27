package xyz.samiker.pointblank_recipe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.samiker.pointblank_recipe.block.ModBlocks;
import xyz.samiker.pointblank_recipe.config.ModConfig;
import xyz.samiker.pointblank_recipe.item.ModItems;
import xyz.samiker.pointblank_recipe.item.PlatformItems;
import xyz.samiker.pointblank_recipe.item.Tab;
import xyz.samiker.pointblank_recipe.resource.ModularRecipeLoader;
import xyz.samiker.pointblank_recipe.resource.ModuleEnabledCondition;
import xyz.samiker.pointblank_recipe.resource.ModuleLoader;

public class PointblankRecipe implements ModInitializer {
    public static final String MOD_ID = "pointblank_recipe";
    public static final Identifier loaderId = Identifier.of(MOD_ID, "module_loader");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig config;

    @Override
    public void onInitialize() {
        config = ModConfig.load();

        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(loaderId, new ModuleLoader());
        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(ModularRecipeLoader.ID, new ModularRecipeLoader());
        // Module states come from ModuleLoader, so it has to run first.
        ResourceLoader.get(ResourceType.SERVER_DATA).addReloaderOrdering(loaderId, ModularRecipeLoader.ID);
        ResourceConditions.register(ModuleEnabledCondition.TYPE);

        PlatformItems.registerAll();
        ModBlocks.registerAll();
        Tab.initialize();

        LOGGER.info("[PB:Recipe] All items initialized successfully.");
    }
}