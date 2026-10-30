package xyz.samiker.pointblank_recipe.resource;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

/** Drops the recipe of the modular workbench when the workbench is turned off in the config. */
public record WorkbenchEnabledCondition() implements ResourceCondition {
    public static final MapCodec<WorkbenchEnabledCondition> CODEC = MapCodec.unit(new WorkbenchEnabledCondition());

    public static final ResourceConditionType<WorkbenchEnabledCondition> TYPE = ResourceConditionType.create(
            ResourceLocation.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "workbench_enabled"),
            CODEC
    );

    @Override
    public boolean test(@Nullable HolderLookup.Provider registryLookup) {
        return PointblankRecipe.config.modularWorkbench;
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }
}
