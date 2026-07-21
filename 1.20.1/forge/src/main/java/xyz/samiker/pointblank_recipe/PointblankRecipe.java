package xyz.samiker.pointblank_recipe;

import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import xyz.samiker.pointblank_recipe.config.ModConfig;
import xyz.samiker.pointblank_recipe.item.PlatformItems;
import xyz.samiker.pointblank_recipe.resource.ModuleEnabledCondition;
import xyz.samiker.pointblank_recipe.resource.ModuleLoader;
import xyz.samiker.pointblank_recipe.utils.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(PointblankRecipe.MOD_ID)
public class PointblankRecipe {
    public static final String MOD_ID = "pointblank_recipe";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig config;

    public PointblankRecipe(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        config = ModConfig.load();

        modEventBus.addListener(this::onRegister);
        Tab.register(modEventBus);

        CraftingHelper.register(ModuleEnabledCondition.Serializer.INSTANCE);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    private void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            event.register(Registries.ITEM, helper -> {
                ItemRegistry.helper = helper;
                PlatformItems.registerAll();
            });
        }
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ModuleLoader());
    }
}