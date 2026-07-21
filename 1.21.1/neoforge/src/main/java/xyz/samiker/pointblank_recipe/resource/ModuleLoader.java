package xyz.samiker.pointblank_recipe.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ModuleLoader extends SimplePreparableReloadListener<Map<String, Boolean>> {
    private static final Map<String, Boolean> DATAPACK_MODULE_STATES = new HashMap<>();

    @Override
    protected @NotNull Map<String, Boolean> prepare(ResourceManager manager, @NotNull ProfilerFiller profiler) {
        Map<String, Boolean> states = new HashMap<>();
        Map<ResourceLocation, Resource> resources = manager.listResources(
                "pointblank_modules",
                path -> path.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            Resource resource = entry.getValue();

            String path = id.getPath();
            String moduleName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')).toLowerCase();

            try (var reader = new InputStreamReader(resource.open())) {
                JsonElement json = JsonParser.parseReader(reader);
                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();
                    if (obj.has("enabled")) {
                        boolean enabled = obj.get("enabled").getAsBoolean();
                        states.put(moduleName, enabled);
                    }
                }
            } catch (Exception e) {
                PointblankRecipe.LOGGER.error("Failed to read module datapack file: {} -> {}", id, e.getMessage());
            }
        }
        return states;
    }

    @Override
    protected void apply(@NotNull Map<String, Boolean> object, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        DATAPACK_MODULE_STATES.clear();
        DATAPACK_MODULE_STATES.putAll(object);
        PointblankRecipe.LOGGER.info("ModuleLoader reloaded on Forge/NeoForge!");
    }

    public static boolean isModuleEnabled(String moduleName) {
        String name = moduleName.toLowerCase();
        if (DATAPACK_MODULE_STATES.containsKey(name)) {
            return DATAPACK_MODULE_STATES.get(name);
        }
        if (PointblankRecipe.config != null) {
            for (String disabled : PointblankRecipe.config.disabledModules) {
                if (disabled.equalsIgnoreCase(name)) {
                    return false;
                }
            }
        }
        return true;
    }
}