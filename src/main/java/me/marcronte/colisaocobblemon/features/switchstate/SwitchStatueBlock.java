package me.marcronte.colisaocobblemon.features.switchstate;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SwitchStatueBlock extends BaseEntityBlock implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<StatueType> TYPE = EnumProperty.create("type", StatueType.class);
    public static final EnumProperty<StatuePart> PART = EnumProperty.create("part", StatuePart.class);

    public static final MapCodec<SwitchStatueBlock> CODEC = simpleCodec(SwitchStatueBlock::new);

    private static final VoxelShape BLOCK_SHAPE = box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public SwitchStatueBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, StatueType.A)
                .setValue(PART, StatuePart.BASE));
    }

    public enum StatueType implements StringRepresentable {
        A, B;
        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public enum StatuePart implements StringRepresentable {
        BASE, MIDDLE, TOP;
        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BLOCK_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, PART);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == StatuePart.BASE ? new SwitchStatueEntity(pos, state) : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(TYPE, StatueType.A)
                .setValue(PART, StatuePart.BASE);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            level.setBlock(pos.above(), state.setValue(PART, StatuePart.MIDDLE), 3);
            level.setBlock(pos.above(2), state.setValue(PART, StatuePart.TOP), 3);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            StatuePart part = state.getValue(PART);
            BlockPos basePos = switch (part) {
                case BASE -> pos;
                case MIDDLE -> pos.below();
                case TOP -> pos.below(2);
            };

            for (int i = 0; i < 3; i++) {
                BlockPos targetPos = basePos.above(i);
                if (level.getBlockState(targetPos).is(this)) {
                    level.removeBlock(targetPos, false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        StatuePart part = state.getValue(PART);
        BlockPos basePos = switch (part) {
            case BASE -> pos;
            case MIDDLE -> pos.below();
            case TOP -> pos.below(2);
        };

        if (part != StatuePart.BASE) {
            BlockState baseState = level.getBlockState(basePos);
            if (baseState.is(this)) {
                return this.useWithoutItem(baseState, level, basePos, player, hitResult);
            }
        }

        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            SwitchStateManager.toggleState(serverPlayer);
            String newStateString = SwitchStateManager.getState(serverPlayer);

            level.playSound(null, basePos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
            if(!newStateString.equalsIgnoreCase("A")){
                player.displayClientMessage(Component.translatable("message.colisao-cobblemon.activate_statue").withStyle(ChatFormatting.GREEN), true);
            } else{
                player.displayClientMessage(Component.translatable("message.colisao-cobblemon.deactivate_statue").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}