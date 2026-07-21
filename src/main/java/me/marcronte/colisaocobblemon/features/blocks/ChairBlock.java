package me.marcronte.colisaocobblemon.features.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChairBlock extends DecorativeBlock {

    private static final VoxelShape SEAT = Block.box(3.0, 0.0, 3.0, 13.0, 9.0, 13.0);

    private static final VoxelShape SHAPE_NORTH = Shapes.or(SEAT, Block.box(3.0, 9.0, 12.0, 13.0, 19.0, 13.0));

    private static final VoxelShape SHAPE_SOUTH = Shapes.or(SEAT, Block.box(3.0, 9.0, 3.0, 13.0, 19, 4.0));

    private static final VoxelShape SHAPE_WEST  = Shapes.or(SEAT, Block.box(12.0, 9.0, 3.0, 13.0, 19.0, 13.0));
    private static final VoxelShape SHAPE_EAST  = Shapes.or(SEAT, Block.box(3.0, 9.0, 3.0, 4.0, 19.0, 13.0));

    public ChairBlock(Properties properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        switch (dir) {
            case NORTH: return SHAPE_NORTH;
            case SOUTH: return SHAPE_SOUTH;
            case WEST:  return SHAPE_WEST;
            case EAST:  return SHAPE_EAST;
            default:    return SHAPE_NORTH;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.525;
        double z = pos.getZ() + 0.5;

        List<ArmorStand> seats = level.getEntitiesOfClass(ArmorStand.class, new AABB(pos));
        ArmorStand seat = null;

        for (ArmorStand existingSeat : seats) {
            if (existingSeat.getTags().contains("chair_seat")) {
                seat = existingSeat;
                break;
            }
        }

        if (seat != null) {
            if (seat.isVehicle()) {
                return InteractionResult.CONSUME;
            }
        } else {
            seat = new ArmorStand(level, x, y, z);
            seat.setInvisible(true);
            seat.setNoGravity(true);
            seat.addTag("chair_seat");

            CompoundTag tag = new CompoundTag();
            seat.addAdditionalSaveData(tag);
            tag.putBoolean("Marker", true);
            seat.readAdditionalSaveData(tag);

            level.addFreshEntity(seat);
        }

        player.startRiding(seat);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            List<ArmorStand> seats = level.getEntitiesOfClass(ArmorStand.class, new AABB(pos));
            for (ArmorStand seat : seats) {
                if (seat.getTags().contains("chair_seat")) {
                    seat.discard();
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}