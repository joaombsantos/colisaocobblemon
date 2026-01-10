package me.marcronte.colisaocobblemon.features.teleportblock;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;


public class TeleportBlock extends BaseEntityBlock {
    public static final MapCodec<TeleportBlock> CODEC = simpleCodec(TeleportBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.1, 0.1, 0.1, 15.9, 15.9, 15.9);

    public TeleportBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TeleportBlockEntity(pos, state); }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TeleportBlockEntity be) {
            be.tryTeleport(entity);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TeleportBlockEntity be) {
            be.tryTeleport(entity);
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isCreative() && level.isClientSide) {

            me.marcronte.colisaocobblemon.client.ColisaoCobblemonClient.openTeleportScreen(pos);

            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static int propagateSettings(Level level, BlockPos currentPos, BlockPos destPos, float yaw, float pitch, Set<BlockPos> visited) {
        if (visited.contains(currentPos)) return 0;
        visited.add(currentPos);

        BlockEntity be = level.getBlockEntity(currentPos);
        if (be instanceof TeleportBlockEntity teleBe) {
            teleBe.setDestination(destPos, yaw, pitch, level);
            int updated = 1;
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(dir);
                if (level.getBlockState(neighborPos).getBlock() instanceof TeleportBlock) {
                    updated += propagateSettings(level, neighborPos, destPos, yaw, pitch, visited);
                }
            }
            return updated;
        }
        return 0;
    }
}