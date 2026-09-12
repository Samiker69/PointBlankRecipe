package xyz.samiker.pointblank_recipe.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

public record ModuleEnabledCondition(String moduleName) implements ICondition {
    public static final MapCodec<ModuleEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("module").forGetter(ModuleEnabledCondition::moduleName)
            ).apply(instance, ModuleEnabledCondition::new)
    );

    @Override
    public boolean test(IContext context) {
        return ModuleLoader.isModuleEnabled(moduleName);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}