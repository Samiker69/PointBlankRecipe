package xyz.samiker.pointblank_recipe.modular;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The modular recipes shipped inside the mod jar. A client connected to a dedicated server never loads server data,
 * so recipe viewers read these instead; tools/modular_draft.py writes the index of file names.
 */
public final class BundledModularRecipes {
    private static final String DIRECTORY = "/data/pointblank_recipe/modular_recipe/";
    private static final String INDEX = "/assets/pointblank_recipe/modular_recipe_index.json";

    private BundledModularRecipes() {
    }

    public static List<ModularRecipe> load() {
        List<ModularRecipe> recipes = new ArrayList<>();
        JsonArray files = read(INDEX).getAsJsonArray();
        for (JsonElement file : files) {
            String name = file.getAsString();
            String id = "pointblank_recipe:modular_recipe/" + name;
            recipes.add(ModularRecipe.parse(id, read(DIRECTORY + name).getAsJsonObject()));
        }
        return recipes;
    }

    private static JsonElement read(String path) {
        try (InputStream stream = BundledModularRecipes.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing " + path + " in the mod jar");
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }
}
