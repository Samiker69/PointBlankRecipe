package xyz.samiker.pointblank_recipe.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

public record ModuleEnabledCondition(String moduleName) implements ResourceCondition {
    public static final MapCodec<ModuleEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("module").forGetter(ModuleEnabledCondition::moduleName)
            ).apply(instance, ModuleEnabledCondition::new)
    );

    public static final ResourceConditionType<ModuleEnabledCondition> TYPE = ResourceConditionType.create(
            Identifier.of(PointblankRecipe.MOD_ID, "module_enabled"),
            CODEC
    );

    @Override
    public boolean test(@Nullable RegistryWrapper.WrapperLookup registryLookup) {
        return ModuleLoader.isModuleEnabled(moduleName);
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }
}