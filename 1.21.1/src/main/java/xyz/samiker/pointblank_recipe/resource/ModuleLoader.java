package xyz.samiker.pointblank_recipe.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ModuleLoader implements IdentifiableResourceReloadListener {
    private static final Map<String, Boolean> DATAPACK_MODULE_STATES = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return Identifier.of(PointblankRecipe.MOD_ID, "module_loader");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            DATAPACK_MODULE_STATES.clear();

            Map<Identifier, Resource> resources = manager.findResources(
                    "pointblank_modules",
                    path -> path.getPath().endsWith(".json")
            );

            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                Identifier id = entry.getKey();
                Resource resource = entry.getValue();

                String path = id.getPath();
                String moduleName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')).toLowerCase();

                try (var reader = new InputStreamReader(resource.getInputStream())) {
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
            PointblankRecipe.LOGGER.info("ModuleLoader reloaded!");
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared);
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
