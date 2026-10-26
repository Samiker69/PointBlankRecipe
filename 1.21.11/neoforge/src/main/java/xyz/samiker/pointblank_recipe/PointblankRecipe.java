package xyz.samiker.pointblank_recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import xyz.samiker.pointblank_recipe.block.ModBlocks;
import xyz.samiker.pointblank_recipe.client.ClientSetup;
import xyz.samiker.pointblank_recipe.config.ModConfig;
import xyz.samiker.pointblank_recipe.item.ModItems;
import xyz.samiker.pointblank_recipe.item.PlatformItems;
import xyz.samiker.pointblank_recipe.resource.ModularRecipeLoader;
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

    public PointblankRecipe(IEventBus modEventBus, ModContainer modContainer) {
        config = ModConfig.load();

        modEventBus.addListener(this::onRegister);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modEventBus.addListener(ClientSetup::registerScreens);
        }
    }

    private void onRegister(RegisterEvent event) {
        ModBlocks.register(event);
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            event.register(Registries.ITEM, helper -> {
                ItemRegistry.helper = helper;
                PlatformItems.registerAll();
            });
        } else if (event.getRegistryKey().equals(NeoForgeRegistries.Keys.CONDITION_CODECS)) {
            event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, helper -> {
                helper.register(Identifier.fromNamespaceAndPath(MOD_ID, "module_enabled"), ModuleEnabledCondition.CODEC);
            });
        } else if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
            event.register(Registries.CREATIVE_MODE_TAB, helper -> {
                helper.register(Identifier.fromNamespaceAndPath(MOD_ID, "item_group"), CreativeModeTab.builder()
                        .icon(() -> new ItemStack(PlatformItems.get(ModItems.FRAME_M4_TYPE)))
                        .title(Component.translatable("item_group.pointblank_recipe"))
                        .displayItems((parameters, output) -> {
                            output.accept(ModBlocks.MODULAR_WORKBENCH_ITEM);
                            for (var item : PlatformItems.getItems()) {
                                output.accept(item);
                            }
                        }).build()
                );
            });
        }
    }

    private void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(ModuleLoader.ID, new ModuleLoader());
        event.addListener(ModularRecipeLoader.ID, new ModularRecipeLoader());
        //event.addDependency(Identifier.withDefaultNamespace("recipes"), ModuleLoader.ID);
    }
}
