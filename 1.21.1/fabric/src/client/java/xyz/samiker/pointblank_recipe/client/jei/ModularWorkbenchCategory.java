package xyz.samiker.pointblank_recipe.client.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.block.ModBlocks;
import xyz.samiker.pointblank_recipe.modular.ModularLayout;
import xyz.samiker.pointblank_recipe.modular.ModularRecipe;
import xyz.samiker.pointblank_recipe.modular.ModularTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Shows a modular recipe the way the workbench does: parts on the gun silhouette, the gun on the right. */
public class ModularWorkbenchCategory implements IRecipeCategory<ModularRecipe> {
    public static final RecipeType<ModularRecipe> TYPE = RecipeType.create(PointblankRecipe.MOD_ID, "modular_workbench", ModularRecipe.class);

    // Only the gun area of the workbench screen is shown; the template name takes the first 9 pixels.
    private static final int TOP = 8;
    private static final int TEXT = 0xFF404040;

    private final IDrawable icon;

    public ModularWorkbenchCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemLike(ModBlocks.MODULAR_WORKBENCH);
    }

    @Override
    public RecipeType<ModularRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("container.pointblank_recipe.modular_workbench");
    }

    @Override
    public int getWidth() {
        return ModularLayout.WIDTH;
    }

    @Override
    public int getHeight() {
        return 94 - TOP;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ModularRecipe recipe, IFocusGroup focuses) {
        for (Map.Entry<String, List<ModularRecipe.Ingredient>> part : recipe.parts().entrySet()) {
            int[] position = ModularLayout.slotPosition(part.getKey());
            builder.addSlot(RecipeIngredientRole.INPUT, position[0], position[1] - TOP)
                    .setStandardSlotBackground()
                    .addItemStacks(stacks(part.getValue()));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, ModularLayout.RESULT_X, ModularLayout.RESULT_Y - TOP)
                .setOutputSlotBackground()
                .addItemStack(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(recipe.resultItem())), recipe.resultCount()));
    }

    @Override
    public void draw(ModularRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        ModularTemplate template = ModularTemplate.byIndex(ModularTemplate.indexOf(recipe.template()));
        for (ModularLayout.Rect rect : ModularLayout.silhouette(template)) {
            graphics.fill(rect.x1() - 1, rect.y1() - 1 - TOP, rect.x2() + 1, rect.y2() + 1 - TOP, ModularLayout.SILHOUETTE_OUTLINE);
        }
        for (ModularLayout.Rect rect : ModularLayout.silhouette(template)) {
            graphics.fill(rect.x1(), rect.y1() - TOP, rect.x2(), rect.y2() - TOP, ModularLayout.SILHOUETTE_FILL);
        }
        Component name = Component.translatable("template.pointblank_recipe." + template.name());
        graphics.drawString(Minecraft.getInstance().font, name, 0, 0, TEXT, false);
    }

    private static List<ItemStack> stacks(List<ModularRecipe.Ingredient> ingredients) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ModularRecipe.Ingredient ingredient : ingredients) {
            if (ingredient.item() != null) {
                stacks.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ingredient.item())), ingredient.count()));
            } else {
                BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, ResourceLocation.parse(ingredient.tag())))
                        .forEach(holder -> stacks.add(new ItemStack(holder, ingredient.count())));
            }
        }
        return stacks;
    }
}
