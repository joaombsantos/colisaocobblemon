package me.marcronte.colisaocobblemon.features.hms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CutObstacleBlock extends Block {

    // --- Cut Configurations ---
    private static final Map<UUID, Long> PERMISSIONS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 24.0D, 16.0D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CutObstacleBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // --- ITERATION LOGIC (Cut) ---
    public static InteractionResult handleInteract(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(Items.SHEARS)) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof CutObstacleBlock)) {
            return InteractionResult.PASS;
        }

        cutSmartLine(world, pos, state.getBlock(), (ServerPlayer) player);

        stack.hurtAndBreak(0, player, EquipmentSlot.MAINHAND);

        return InteractionResult.SUCCESS;
    }

    private static void cutSmartLine(Level world, BlockPos centerPos, Block targetBlock, ServerPlayer player) {
        List<BlockPos> blocksToHide = new ArrayList<>();
        blocksToHide.add(centerPos);

        // DETECTION
        boolean hasX = isTarget(world, centerPos.east(), targetBlock) || isTarget(world, centerPos.west(), targetBlock);
        boolean hasZ = isTarget(world, centerPos.north(), targetBlock) || isTarget(world, centerPos.south(), targetBlock);

        Direction.Axis axisToCut = null;

        if (hasX && hasZ) {
            axisToCut = player.getDirection().getAxis();
        } else if (hasX) {
            axisToCut = Direction.Axis.X;
        } else if (hasZ) {
            axisToCut = Direction.Axis.Z;
        }

        // COLLECT
        if (axisToCut != null) {
            if (axisToCut == Direction.Axis.X) {
                collectOffsets(world, centerPos, targetBlock, Direction.EAST, blocksToHide);
                collectOffsets(world, centerPos, targetBlock, Direction.WEST, blocksToHide);
            } else {
                collectOffsets(world, centerPos, targetBlock, Direction.NORTH, blocksToHide);
                collectOffsets(world, centerPos, targetBlock, Direction.SOUTH, blocksToHide);
            }
        }

        // ACTION
        long duration = 10;
        allowPlayer(player.getUUID(), duration);

        BlockState airState = Blocks.AIR.defaultBlockState();

        // 100ms DELAY
        SCHEDULER.schedule(() -> {
            if (player.getServer() == null || player.hasDisconnected()) return;
            for (BlockPos pos : blocksToHide) {
                player.connection.send(new ClientboundBlockUpdatePacket(pos, airState));
            }
        }, 100, TimeUnit.MILLISECONDS);

        // RETURN AFTER 10 SECONDS
        SCHEDULER.schedule(() -> {
            if (player.getServer() != null) {
                player.getServer().execute(() -> {
                    if (player.hasDisconnected()) return;

                    for (BlockPos pos : blocksToHide) {
                        BlockState originalState = world.getBlockState(pos);
                        if (originalState.getBlock() instanceof CutObstacleBlock) {
                            player.connection.send(new ClientboundBlockUpdatePacket(pos, originalState));
                        }
                    }
                });
            }
        }, duration, TimeUnit.SECONDS);
    }

    private static void collectOffsets(Level world, BlockPos startPos, Block targetBlock, Direction dir, List<BlockPos> list) {
        for (int i = 1; i <= 15; i++) {
            BlockPos checkPos = startPos.relative(dir, i);
            if (isTarget(world, checkPos, targetBlock)) {
                list.add(checkPos);
            } else {
                break;
            }
        }
    }

    private static boolean isTarget(Level world, BlockPos pos, Block target) {
        return world.getBlockState(pos).is(target);
    }

    // --- COLLISIONS AND PERMISSIONS ---
    public static void allowPlayer(UUID playerUuid, long seconds) {
        long expireTime = System.currentTimeMillis() + (seconds * 1000);
        PERMISSIONS.put(playerUuid, expireTime);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof Player player && hasPermission(player.getUUID())) {
                return Shapes.empty();
            }
        }
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    private static boolean hasPermission(UUID uuid) {
        if (!PERMISSIONS.containsKey(uuid)) return false;
        long expiration = PERMISSIONS.get(uuid);
        if (System.currentTimeMillis() > expiration) {
            PERMISSIONS.remove(uuid);
            return false;
        }
        return true;
    }
}