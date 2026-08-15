package me.marcronte.colisaocobblemon.features.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PokeLurerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape LOWER_BASE = Block.box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0);
    private static final VoxelShape LOWER_PILLAR = Block.box(4.0, 2.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape LOWER_CORE = Shapes.or(LOWER_BASE, LOWER_PILLAR);

    private static final VoxelShape LOWER_NORTH = Shapes.or(LOWER_CORE, Block.box(5.0, 5.0, 2.0, 11.0, 8.0, 4.0));
    private static final VoxelShape LOWER_EAST = Shapes.or(LOWER_CORE, Block.box(12.0, 5.0, 5.0, 14.0, 8.0, 11.0));
    private static final VoxelShape LOWER_SOUTH = Shapes.or(LOWER_CORE, Block.box(5.0, 5.0, 12.0, 11.0, 8.0, 14.0));
    private static final VoxelShape LOWER_WEST = Shapes.or(LOWER_CORE, Block.box(2.0, 5.0, 5.0, 4.0, 8.0, 11.0));

    private static final VoxelShape UPPER_PILLAR = Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0);
    private static final VoxelShape UPPER_TOP = Block.box(3.0, 14.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape UPPER_SHAPE = Shapes.or(UPPER_PILLAR, UPPER_TOP);

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            return neighborState.is(this) && neighborState.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return UPPER_SHAPE;
        }
        return switch (state.getValue(FACING)) {
            case EAST -> LOWER_EAST;
            case SOUTH -> LOWER_SOUTH;
            case WEST -> LOWER_WEST;
            default -> LOWER_NORTH;
        };
    }

    public PokeLurerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return new PokeLurerBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(HALF) == DoubleBlockHalf.UPPER) return null;

        return (lvl, pos, st, be) -> {
            if (be instanceof PokeLurerBlockEntity lurer) {
                lurer.tick(lvl, pos, st);
            }
        };
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockPos targetPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();

            BlockEntity be = level.getBlockEntity(targetPos);
            if (be instanceof PokeLurerBlockEntity lurer) {
                serverPlayer.openMenu(lurer);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockPos targetPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();

            BlockEntity be = level.getBlockEntity(targetPos);
            if (be instanceof PokeLurerBlockEntity lurer) {
                serverPlayer.openMenu(lurer);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return RenderShape.INVISIBLE;
        }
        return super.getRenderShape(state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PokeLurerBlockEntity lurer) {
                    net.minecraft.world.Containers.dropContents(level, pos, lurer);
                    level.updateNeighbourForOutputSignal(pos, this);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}