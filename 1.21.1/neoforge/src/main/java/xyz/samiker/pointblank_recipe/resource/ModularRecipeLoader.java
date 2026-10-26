package xyz.samiker.pointblank_recipe.resource;

import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.modular.ModularRecipe;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModularRecipeLoader implements ResourceManagerReloadListener {
    private static List<ModularRecipe> recipes = List.of();

    // Reload listeners have no guaranteed order here, so module states are checked on use, not on load.
    public static List<ModularRecipe> recipes() {
        return recipes.stream().filter(recipe -> recipe.isEnabled(ModuleLoader::isModuleEnabled)).toList();
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        List<ModularRecipe> loaded = new ArrayList<>();
        int missingItems = 0;

        Map<ResourceLocation, Resource> resources = manager.listResources("modular_recipe", path -> path.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try (var reader = new InputStreamReader(entry.getValue().open())) {
                ModularRecipe recipe = ModularRecipe.parse(entry.getKey().toString(), JsonParser.parseReader(reader).getAsJsonObject());
                if (!recipe.itemIds().stream().allMatch(id -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.tryParse(id)))) {
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
        PointblankRecipe.LOGGER.info("Loaded {} modular recipes ({} with missing items)", loaded.size(), missingItems);
    }
}
