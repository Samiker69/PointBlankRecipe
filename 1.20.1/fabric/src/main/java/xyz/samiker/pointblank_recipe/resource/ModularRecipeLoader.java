package xyz.samiker.pointblank_recipe.resource;

import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
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
    public Identifier getFabricId() {
        return Identifier.of(PointblankRecipe.MOD_ID, "modular_recipe_loader");
    }

    // Module states come from ModuleLoader, so it has to run first.
    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(Identifier.of(PointblankRecipe.MOD_ID, "module_loader"));
    }

    @Override
    public void reload(ResourceManager manager) {
        List<ModularRecipe> loaded = new ArrayList<>();
        int disabled = 0;
        int missingItems = 0;

        Map<Identifier, Resource> resources = manager.findResources("modular_recipe", path -> path.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (var reader = new InputStreamReader(entry.getValue().getInputStream())) {
                ModularRecipe recipe = ModularRecipe.parse(entry.getKey().toString(), JsonParser.parseReader(reader).getAsJsonObject());
                if (!recipe.isEnabled(ModuleLoader::isModuleEnabled)) {
                    disabled++;
                } else if (!recipe.itemIds().stream().allMatch(id -> Registries.ITEM.containsId(Identifier.tryParse(id)))) {
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
