package xyz.samiker.pointblank_recipe.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ModuleLoader implements ResourceManagerReloadListener {
    private static final Map<String, Boolean> DATAPACK_MODULE_STATES = new HashMap<>();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        DATAPACK_MODULE_STATES.clear();

        Map<Identifier, Resource> resources = manager.listResources(
                "pointblank_modules",
                path -> path.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            Resource resource = entry.getValue();

            String path = id.getPath();
            String moduleName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')).toLowerCase();

            try (var reader = new InputStreamReader(resource.open())) {
                JsonElement json = JsonParser.parseReader(reader);
                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();
                    if (obj.has("enabled")) {
                        boolean enabled = obj.get("enabled").getAsBoolean();
                        DATAPACK_MODULE_STATES.put(moduleName, enabled);
                    }
                }
            } catch (Exception e) {
                PointblankRecipe.LOGGER.error("Failed to read module datapack file: {} -> {}", id, e.getMessage());
            }
        }
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