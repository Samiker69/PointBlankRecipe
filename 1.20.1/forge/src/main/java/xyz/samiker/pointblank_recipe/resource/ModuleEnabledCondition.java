package xyz.samiker.pointblank_recipe.resource;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import xyz.samiker.pointblank_recipe.PointblankRecipe;

public class ModuleEnabledCondition implements ICondition {
    private final String moduleName;

    public ModuleEnabledCondition(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public ResourceLocation getID() {
        return Serializer.INSTANCE.getID();
    }

    @Override
    public boolean test(ICondition.IContext context) {
        return ModuleLoader.isModuleEnabled(moduleName);
    }

    public static class Serializer implements IConditionSerializer<ModuleEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();
        @SuppressWarnings("removal")
        private static final ResourceLocation ID = new ResourceLocation(PointblankRecipe.MOD_ID, "module_enabled");

        @Override
        public void write(JsonObject json, ModuleEnabledCondition value) {
            json.addProperty("module", value.moduleName);
        }

        @Override
        public ModuleEnabledCondition read(JsonObject json) {
            return new ModuleEnabledCondition(json.get("module").getAsString());
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}