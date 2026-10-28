package xyz.samiker.pointblank_recipe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.samiker.pointblank_recipe.block.ModBlocks;
import xyz.samiker.pointblank_recipe.config.ModConfig;
import xyz.samiker.pointblank_recipe.item.PlatformItems;
import xyz.samiker.pointblank_recipe.item.Tab;
import xyz.samiker.pointblank_recipe.resource.ModularRecipeLoader;
import xyz.samiker.pointblank_recipe.resource.ModuleLoader;

public class PointblankRecipe implements ModInitializer {
    public static final String MOD_ID = "pointblank_recipe";
    public static final ResourceLocation CONDITION_ID = ResourceLocation.tryBuild(MOD_ID, "module_enabled");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig config;

    @Override
    public void onInitialize() {
        config = ModConfig.load();

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ModuleLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ModularRecipeLoader());

        ResourceConditions.register(CONDITION_ID, json -> {
            if (json.has("module")) {
                String module = json.get("module").getAsString();
                return ModuleLoader.isModuleEnabled(module);
            }
            return true;
        });

        PlatformItems.registerAll();
        ModBlocks.registerAll();
        Tab.initialize();

        LOGGER.info("[PB:Recipe] All items initialized successfully.");
    }
}