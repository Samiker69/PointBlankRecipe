package xyz.samiker.pointblank_recipe.modular;

/** Which modules ("modules" of a modular recipe) have their content loaded right now. */
public final class ModularModules {
    private ModularModules() {
    }

    // Stub: there is no way to tell yet, so every module counts as loaded.
    public static boolean isLoaded(String module) {
        return true;
    }
}
