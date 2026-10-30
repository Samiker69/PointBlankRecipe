package xyz.samiker.pointblank_recipe.modular;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A recipe from data/<namespace>/modular_recipe/*.json: a template, parts by slot ({"item"|"tag", "count"}; a list
 * means any of these), loose materials, an optional "base" weapon for upgrades and a vanilla-style result.
 */
public record ModularRecipe(
        String id,
        String template,
        Map<String, List<Ingredient>> parts,
        List<Ingredient> materials,
        String resultItem,
        int resultCount,
        List<String> modules
) {
    /** One stack as the matcher sees it, so this class stays free of Minecraft types. */
    public interface Stack {
        boolean isEmpty();

        String itemId();

        boolean isIn(String tagId);

        int count();
    }

    public record Ingredient(String item, String tag, int count) {
        public boolean test(Stack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            return item != null ? item.equals(stack.itemId()) : stack.isIn(tag);
        }

        static Ingredient parse(JsonObject json) {
            int count = json.has("count") ? json.get("count").getAsInt() : 1;
            if (json.has("tag")) {
                return new Ingredient(null, json.get("tag").getAsString(), count);
            }
            return new Ingredient(json.get("item").getAsString(), null, count);
        }
    }

    public static ModularRecipe parse(String id, JsonObject json) {
        Map<String, List<Ingredient>> parts = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> slot : json.getAsJsonObject("parts").entrySet()) {
            parts.put(slot.getKey(), parseList(slot.getValue()));
        }
        if (json.has("base")) {
            parts.put(ModularTemplate.BASE_SLOT, parseList(json.get("base")));
        }

        JsonObject result = json.getAsJsonObject("result");
        String template = json.get("template").getAsString();
        return new ModularRecipe(
                id,
                template.substring(template.indexOf(':') + 1),
                parts,
                json.has("materials") ? parseList(json.get("materials")) : List.of(),
                result.get("id").getAsString(),
                result.has("count") ? result.get("count").getAsInt() : 1,
                parseModules(json)
        );
    }

    private static List<Ingredient> parseList(JsonElement element) {
        List<Ingredient> list = new ArrayList<>();
        if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                list.add(Ingredient.parse(entry.getAsJsonObject()));
            }
        } else {
            list.add(Ingredient.parse(element.getAsJsonObject()));
        }
        return list;
    }

    // The NeoForge/Forge builds rewrite fabric:load_conditions, so accept every spelling.
    private static List<String> parseModules(JsonObject json) {
        List<String> modules = new ArrayList<>();
        for (String key : List.of("fabric:load_conditions", "neoforge:conditions", "forge:conditions")) {
            if (!json.has(key)) {
                continue;
            }
            JsonArray conditions = json.getAsJsonArray(key);
            for (JsonElement condition : conditions) {
                JsonObject object = condition.getAsJsonObject();
                if (object.has("module")) {
                    modules.add(object.get("module").getAsString());
                }
            }
        }
        return modules;
    }

    public boolean isEnabled(Predicate<String> moduleEnabled) {
        return modules.stream().allMatch(moduleEnabled);
    }

    /** Whether the workbench shows this recipe: every module it needs is loaded. */
    public boolean isAvailable() {
        return modules.stream().allMatch(ModularModules::isLoaded);
    }

    /**
     * Recipes of a template the parts in the slots can still become, complete ones first, then by result. These are
     * what the workbench flips through, so an empty workbench lists every gun of the template.
     */
    public static List<ModularRecipe> candidates(List<ModularRecipe> recipes, String template, Map<String, Stack> slots) {
        return recipes.stream()
                .filter(recipe -> recipe.template.equals(template) && recipe.isAvailable() && recipe.fits(slots))
                .sorted(Comparator.comparing((ModularRecipe recipe) -> recipe.match(slots, List.of()) == null)
                        .thenComparing(ModularRecipe::resultItem))
                .toList();
    }

    /** Every filled slot holds one of the parts of this recipe; empty slots and short stacks are still fine. */
    public boolean fits(Map<String, Stack> slots) {
        for (Map.Entry<String, Stack> slot : slots.entrySet()) {
            Stack stack = slot.getValue();
            List<Ingredient> wanted = parts.get(slot.getKey());
            if (!stack.isEmpty() && (wanted == null || wanted.stream().noneMatch(ingredient -> ingredient.test(stack)))) {
                return false;
            }
        }
        return slots.keySet().containsAll(parts.keySet());
    }

    /** Every item id this recipe references, to drop recipes for add-ons that are not installed. */
    public List<String> itemIds() {
        List<String> ids = new ArrayList<>();
        ids.add(resultItem);
        parts.values().forEach(list -> list.forEach(ingredient -> {
            if (ingredient.item != null) ids.add(ingredient.item);
        }));
        materials.forEach(ingredient -> {
            if (ingredient.item != null) ids.add(ingredient.item);
        });
        return ids;
    }

    /**
     * Checks the workbench contents. Every filled part slot must be used by the recipe and every slot of the recipe
     * must be filled; materials only need to be present, extra materials stay in the grid.
     *
     * @param slots    the stack in each slot of the current template, by slot name
     * @param grid     the materials grid
     * @return how many items to take from each material stack, or null when the recipe does not match
     */
    public int[] match(Map<String, Stack> slots, List<Stack> grid) {
        for (Map.Entry<String, Stack> slot : slots.entrySet()) {
            List<Ingredient> wanted = parts.get(slot.getKey());
            Stack stack = slot.getValue();
            if (wanted == null) {
                if (!stack.isEmpty()) return null;
                continue;
            }
            // A slot holds one stack, so a list in one slot means "any of these".
            if (wanted.stream().noneMatch(ingredient -> ingredient.test(stack) && stack.count() >= ingredient.count)) {
                return null;
            }
        }
        for (String slot : parts.keySet()) {
            if (!slots.containsKey(slot)) return null;
        }

        int[] taken = new int[grid.size()];
        // Exact items first, so a tag does not eat the stack an item ingredient needs.
        List<Ingredient> ordered = new ArrayList<>(materials);
        ordered.sort((a, b) -> Boolean.compare(a.item == null, b.item == null));
        for (Ingredient ingredient : ordered) {
            int missing = ingredient.count;
            for (int i = 0; i < grid.size() && missing > 0; i++) {
                Stack stack = grid.get(i);
                if (ingredient.test(stack)) {
                    int take = Math.min(missing, stack.count() - taken[i]);
                    taken[i] += take;
                    missing -= take;
                }
            }
            if (missing > 0) return null;
        }
        return taken;
    }

    /** How many items the recipe takes from a part slot. */
    public int partCount(String slot, Stack stack) {
        for (Ingredient ingredient : parts.getOrDefault(slot, List.of())) {
            if (ingredient.test(stack)) return ingredient.count;
        }
        return 0;
    }
}
