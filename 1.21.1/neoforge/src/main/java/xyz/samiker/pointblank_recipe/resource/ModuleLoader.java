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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleLoader extends SimplePreparableReloadListener<Map<String, Boolean>> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "module_loader");
    private static final Map<String, Boolean> DATAPACK_MODULE_STATES = new ConcurrentHashMap<>();

    @Override
    protected @NotNull Map<String, Boolean> prepare(ResourceManager manager, @NotNull ProfilerFiller profiler) {
        Map<String, Boolean> states = new ConcurrentHashMap<>();
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
                        states.put(moduleName, obj.get("enabled").getAsBoolean());
                    }
                }
            } catch (Exception e) {
                PointblankRecipe.LOGGER.error("Failed to read module datapack file: {} -> {}", id, e.getMessage());
            }
        }

        DATAPACK_MODULE_STATES.clear();
        DATAPACK_MODULE_STATES.putAll(states);

        return states;
    }

    @Override
    protected void apply(@NotNull Map<String, Boolean> object, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        PointblankRecipe.LOGGER.info("ModuleLoader reloaded on NeoForge!");
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