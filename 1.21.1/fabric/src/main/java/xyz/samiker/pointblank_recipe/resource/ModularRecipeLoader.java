package xyz.samiker.pointblank_recipe.resource;

import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.modular.ModularRecipe;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ModularRecipeLoader implements SimpleSynchronousResourceReloadListener {
    private static List<ModularRecipe> recipes = List.of();

    public static List<ModularRecipe> recipes() {
        return recipes;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "modular_recipe_loader");
    }

    // Module states come from ModuleLoader, so it has to run first.
    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return List.of(ResourceLocation.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "module_loader"));
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        List<ModularRecipe> loaded = new ArrayList<>();
        int disabled = 0;
        int missingItems = 0;

        Map<ResourceLocation, Resource> resources = manager.listResources("modular_recipe", path -> path.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try (var reader = new InputStreamReader(entry.getValue().open())) {
                ModularRecipe recipe = ModularRecipe.parse(entry.getKey().toString(), JsonParser.parseReader(reader).getAsJsonObject());
                if (!recipe.isEnabled(ModuleLoader::isModuleEnabled)) {
                    disabled++;
                } else if (!recipe.itemIds().stream().allMatch(id -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.tryParse(id)))) {
                    // Recipes for weapon packs that are not installed.
                    missingItems++;
                } else {
                    loaded.add(recipe);
                }
            } catch (Exception e) {
                PointblankRecipe.LOGGER.error("Failed to read modular recipe {}: {}", entry.getKey(), e.getMessage());
            }
        }

        recipes = List.copyOf(loaded);
        PointblankRecipe.LOGGER.info("Loaded {} modular recipes ({} in disabled modules, {} with missing items)",
                loaded.size(), disabled, missingItems);
    }
}
