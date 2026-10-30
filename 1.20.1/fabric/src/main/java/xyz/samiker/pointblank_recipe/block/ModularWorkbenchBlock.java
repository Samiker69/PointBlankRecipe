package xyz.samiker.pointblank_recipe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchScreenHandler;

import java.util.EnumMap;
import java.util.Map;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/**
 * A workbench three blocks wide. The center block renders the whole model; the side blocks are invisible parts that
 * give the model its collision and break together with the center.
 */
public class ModularWorkbenchBlock extends Block {
    private static final Component TITLE = Component.translatable("container.pointblank_recipe.modular_workbench");
    public static final DirectionProperty FACING = HORIZONTAL_FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    // The model facing north, in pixels: it spans x -16..32, the back panel sticks out above the table.
    private static final double[][] MODEL_BOXES = {
            {-16, 0, 0, 32, 16, 16},
            {-10, 16, 13, 26, 19, 16},
            {-4, 19, 13, 20, 24, 16},
            {-4, 21, 9, 20, 22, 13},
    };
    private static final Map<Part, Map<Direction, VoxelShape>> SHAPES = new EnumMap<>(Part.class);

    static {
        for (Part part : Part.values()) {
            Map<Direction, VoxelShape> byFacing = new EnumMap<>(Direction.class);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                byFacing.put(facing, shape(part, facing));
            }
            SHAPES.put(part, byFacing);
        }
    }

    public ModularWorkbenchBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, Part.CENTER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    // Only placed when both side blocks have room, so the model never cuts into other blocks.
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        Level level = ctx.getLevel();
        for (Part side : Part.SIDES) {
            BlockPos pos = ctx.getClickedPos().relative(side.direction(facing));
            if (!level.getBlockState(pos).canBeReplaced(ctx) || !level.getWorldBorder().isWithinBounds(pos)) {
                return null;
            }
        }
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            for (Part side : Part.SIDES) {
                level.setBlock(pos.relative(side.direction(state.getValue(FACING))), state.setValue(PART, side), Block.UPDATE_ALL);
            }
        }
    }

    // A part whose neighbor in the workbench is gone removes itself, so breaking any part breaks the whole workbench.
    // The center is placed before its sides, so it only does this once the other side shows the workbench was complete.
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Part part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        for (Part linked : part.linked()) {
            if (direction == part.towards(linked, facing) && !isPart(neighborState, linked, facing)) {
                Part other = linked == Part.LEFT ? Part.RIGHT : Part.LEFT;
                if (part != Part.CENTER || isPart(level.getBlockState(pos.relative(part.towards(other, facing))), other, facing)) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // Only the center drops the item, also when a side is broken and takes the center with it. In creative nothing
    // should drop, so the center goes first, quietly.
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && state.getValue(PART) != Part.CENTER) {
            level.setBlock(center(state, pos), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(PART)).get(state.getValue(FACING));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            if (PointblankRecipe.config.modularWorkbench) {
                player.openMenu(state.getMenuProvider(world, center(state, pos)));
            } else {
                player.displayClientMessage(Component.translatable("container.pointblank_recipe.modular_workbench.disabled"), true);
            }
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

    private boolean isPart(BlockState state, Part part, Direction facing) {
        return state.is(this) && state.getValue(PART) == part && state.getValue(FACING) == facing;
    }

    private static BlockPos center(BlockState state, BlockPos pos) {
        Part part = state.getValue(PART);
        return part == Part.CENTER ? pos : pos.relative(part.towards(Part.CENTER, state.getValue(FACING)));
    }

    // Cuts the model boxes down to one part and turns them like the blockstate turns the model.
    private static VoxelShape shape(Part part, Direction facing) {
        VoxelShape shape = Shapes.empty();
        double offset = part.offset * 16;
        for (double[] box : MODEL_BOXES) {
            double x1 = Math.max(box[0], offset) - offset;
            double x2 = Math.min(box[3], offset + 16) - offset;
            if (x1 >= x2) {
                continue;
            }
            double z1 = box[2];
            double z2 = box[5];
            double[] turned = switch (facing) {
                case EAST -> new double[]{16 - z2, x1, 16 - z1, x2};
                case SOUTH -> new double[]{16 - x2, 16 - z2, 16 - x1, 16 - z1};
                case WEST -> new double[]{z1, 16 - x2, z2, 16 - x1};
                default -> new double[]{x1, z1, x2, z2};
            };
            shape = Shapes.or(shape, Block.box(turned[0], box[1], turned[1], turned[2], box[4], turned[3]));
        }
        return shape;
    }

    /** Which third of the workbench a block is, from the left of the model facing north. */
    public enum Part implements StringRepresentable {
        LEFT(-1),
        CENTER(0),
        RIGHT(1);

        static final Part[] SIDES = {LEFT, RIGHT};

        private final int offset;

        Part(int offset) {
            this.offset = offset;
        }

        /** Where this side block sits from the center. */
        Direction direction(Direction facing) {
            return offset < 0 ? facing.getCounterClockWise() : facing.getClockWise();
        }

        Direction towards(Part other, Direction facing) {
            return other.offset < offset ? facing.getCounterClockWise() : facing.getClockWise();
        }

        Part[] linked() {
            return this == CENTER ? SIDES : new Part[]{CENTER};
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
