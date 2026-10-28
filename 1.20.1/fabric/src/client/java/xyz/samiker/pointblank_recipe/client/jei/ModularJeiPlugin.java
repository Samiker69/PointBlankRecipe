package xyz.samiker.pointblank_recipe.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.block.ModBlocks;
import xyz.samiker.pointblank_recipe.modular.BundledModularRecipes;
import xyz.samiker.pointblank_recipe.modular.ModularRecipe;
import xyz.samiker.pointblank_recipe.resource.ModularRecipeLoader;
import xyz.samiker.pointblank_recipe.resource.ModuleLoader;

import java.util.List;

public class ModularJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.tryBuild(PointblankRecipe.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ModularWorkbenchCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(ModularWorkbenchCategory.TYPE, recipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.MODULAR_WORKBENCH, ModularWorkbenchCategory.TYPE);
    }

    // In single player the integrated server has the real (datapack-aware) list; on a dedicated server the client
    // only has the recipes bundled in the jar.
    private static List<ModularRecipe> recipes() {
        List<ModularRecipe> recipes = Minecraft.getInstance().getSingleplayerServer() != null
                ? ModularRecipeLoader.recipes()
                : BundledModularRecipes.load().stream().filter(recipe -> recipe.isEnabled(ModuleLoader::isModuleEnabled)).toList();
        return recipes.stream()
                .filter(recipe -> recipe.itemIds().stream().allMatch(id -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.tryParse(id))))
                .toList();
    }
}
