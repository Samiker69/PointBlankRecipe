package xyz.samiker.pointblank_recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xyz.samiker.pointblank_recipe.item.ModItems;
import xyz.samiker.pointblank_recipe.item.PlatformItems;

import static xyz.samiker.pointblank_recipe.PointblankRecipe.MOD_ID;

public class Tab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> POINTBLANK_RECIPE_TAB =
            CREATIVE_MODE_TABS.register("item_group", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(PlatformItems.get(ModItems.FRAME_M4_TYPE)))
                    .title(Component.translatable("item_group.pointblank_recipe"))
                    .displayItems((parameters, output) -> {
                        for (var item : PlatformItems.getItems()) {
                            output.accept(item);
                        }
                    })
                    .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}