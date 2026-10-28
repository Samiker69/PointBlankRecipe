package xyz.samiker.pointblank_recipe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchScreenHandler;

public class ModularWorkbenchBlock extends Block {
    private static final Component TITLE = Component.translatable("container.pointblank_recipe.modular_workbench");

    public ModularWorkbenchBlock(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            player.openMenu(state.getMenuProvider(world, pos));
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return new SimpleMenuProvider(
                (syncId, inventory, player) -> new ModularWorkbenchScreenHandler(syncId, inventory, ContainerLevelAccess.create(world, pos)),
                TITLE
        );
    }
}
