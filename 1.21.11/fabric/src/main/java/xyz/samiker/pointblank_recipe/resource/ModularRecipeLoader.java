package xyz.samiker.pointblank_recipe.resource;

import com.google.gson.JsonParser;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.modular.ModularRecipe;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ModularRecipeLoader implements ResourceManagerReloadListener {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "modular_recipe_loader");

    private static List<ModularRecipe> recipes = List.of();

    public static List<ModularRecipe> recipes() {
        return recipes;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        List<ModularRecipe> loaded = new ArrayList<>();
        int disabled = 0;
        int missingItems = 0;

        Map<Identifier, Resource> resources = manager.listResources("modular_recipe", path -> path.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (var reader = new InputStreamReader(entry.getValue().open())) {
                ModularRecipe recipe = ModularRecipe.parse(entry.getKey().toString(), JsonParser.parseReader(reader).getAsJsonObject());
                if (!recipe.isEnabled(ModuleLoader::isModuleEnabled)) {
                    disabled++;
                } else if (!recipe.itemIds().stream().allMatch(id -> BuiltInRegistries.ITEM.containsKey(Identifier.tryParse(id)))) {
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
