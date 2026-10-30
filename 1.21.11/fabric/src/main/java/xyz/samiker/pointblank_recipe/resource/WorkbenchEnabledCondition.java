package xyz.samiker.pointblank_recipe.resource;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

/** Drops the recipe of the modular workbench when the workbench is turned off in the config. */
public record WorkbenchEnabledCondition() implements ResourceCondition {
    public static final MapCodec<WorkbenchEnabledCondition> CODEC = MapCodec.unit(new WorkbenchEnabledCondition());

    public static final ResourceConditionType<@NotNull WorkbenchEnabledCondition> TYPE = ResourceConditionType.create(
            Identifier.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "workbench_enabled"),
            CODEC
    );

    @Override
    public boolean test(@Nullable RegistryOps.RegistryInfoLookup registryInfoGetter) {
        return PointblankRecipe.config.modularWorkbench;
    }

    @Override
    public @NotNull ResourceConditionType<?> getType() {
        return TYPE;
    }
}
