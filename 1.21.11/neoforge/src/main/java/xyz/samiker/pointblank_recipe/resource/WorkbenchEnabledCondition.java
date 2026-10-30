package xyz.samiker.pointblank_recipe.resource;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

/** Drops the recipe of the modular workbench when the workbench is turned off in the config. */
public record WorkbenchEnabledCondition() implements ICondition {
    public static final MapCodec<WorkbenchEnabledCondition> CODEC = MapCodec.unit(new WorkbenchEnabledCondition());

    @Override
    public boolean test(IContext context) {
        return PointblankRecipe.config.modularWorkbench;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
