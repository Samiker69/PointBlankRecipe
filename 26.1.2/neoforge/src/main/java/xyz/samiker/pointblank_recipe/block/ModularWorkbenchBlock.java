package xyz.samiker.pointblank_recipe.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchMenu;

public class ModularWorkbenchBlock extends Block {
    public static final MapCodec<ModularWorkbenchBlock> CODEC = simpleCodec(ModularWorkbenchBlock::new);
    private static final Component TITLE = Component.translatable("container.pointblank_recipe.modular_workbench");

    public ModularWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            player.openMenu(state.getMenuProvider(level, pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inventory, player) -> new ModularWorkbenchMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)),
                TITLE
        );
    }
}
