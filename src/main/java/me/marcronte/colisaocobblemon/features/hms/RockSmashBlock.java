package me.marcronte.colisaocobblemon.features.hms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

public class RockSmashBlock extends Block {

    private static final Map<UUID, Long> PERMISSIONS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 24.0D, 16.0D);

    public RockSmashBlock(Properties settings) {
        super(settings);
    }

    public static InteractionResult handleInteract(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof PickaxeItem)) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof RockSmashBlock)) {
            return InteractionResult.PASS;
        }

        smashRockLine(world, pos, state.getBlock(), (ServerPlayer) player);

        // CORREÇÃO: Uso simplificado do hurtAndBreak (sem lambda)
        // Isso resolve o erro "Cannot resolve method broadcastBreakEvent"
        stack.hurtAndBreak(0, player, EquipmentSlot.MAINHAND);

        return InteractionResult.SUCCESS;
    }

    private static void smashRockLine(Level world, BlockPos centerPos, Block targetBlock, ServerPlayer player) {
        List<BlockPos> blocksToHide = new ArrayList<>();
        blocksToHide.add(centerPos);

        // 1. DETECÇÃO
        boolean hasX = isTarget(world, centerPos.east(), targetBlock) || isTarget(world, centerPos.west(), targetBlock);
        boolean hasZ = isTarget(world, centerPos.north(), targetBlock) || isTarget(world, centerPos.south(), targetBlock);

        Direction.Axis axisToBreak = null;

        if (hasX && hasZ) {
            axisToBreak = player.getDirection().getAxis();
        } else if (hasX) {
            axisToBreak = Direction.Axis.X;
        } else if (hasZ) {
            axisToBreak = Direction.Axis.Z;
        }

        // 2. COLETA
        if (axisToBreak != null) {
            if (axisToBreak == Direction.Axis.X) {
                collectOffsets(world, centerPos, targetBlock, Direction.EAST, blocksToHide);
                collectOffsets(world, centerPos, targetBlock, Direction.WEST, blocksToHide);
            } else {
                collectOffsets(world, centerPos, targetBlock, Direction.NORTH, blocksToHide);
                collectOffsets(world, centerPos, targetBlock, Direction.SOUTH, blocksToHide);
            }
        }

        // 3. AÇÃO
        long duration = 10;
        allowPlayer(player.getUUID(), duration);

        world.playSound(null, centerPos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);

        BlockState airState = Blocks.AIR.defaultBlockState();

        // Delay 100ms
        SCHEDULER.schedule(() -> {
            if (player.getServer() == null || player.hasDisconnected()) return;
            for (BlockPos pos : blocksToHide) {
                player.connection.send(new ClientboundBlockUpdatePacket(pos, airState));
            }
        }, 100, TimeUnit.MILLISECONDS);

        // Retorno após 10s
        SCHEDULER.schedule(() -> {
            if (player.getServer() != null) {
                player.getServer().execute(() -> {
                    if (player.hasDisconnected()) return;

                    for (BlockPos pos : blocksToHide) {
                        BlockState originalState = world.getBlockState(pos);
                        if (originalState.getBlock() instanceof RockSmashBlock) {
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

    // --- Colisão e Permissões ---
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