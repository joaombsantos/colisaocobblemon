package me.marcronte.colisaocobblemon.features.hms;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

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

    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 24.0D, 16.0D);

    public RockSmashBlock(Settings settings) {
        super(settings);
    }

    public static ActionResult handleInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.PASS;
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

        ItemStack stack = player.getMainHandStack();

        if (!(stack.getItem() instanceof PickaxeItem)) return ActionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof RockSmashBlock)) {
            return ActionResult.PASS;
        }

        smashRockLine(world, pos, state.getBlock(), (ServerPlayerEntity) player);
        stack.damage(0, player, EquipmentSlot.MAINHAND);

        return ActionResult.SUCCESS;
    }

    private static void smashRockLine(World world, BlockPos centerPos, Block targetBlock, ServerPlayerEntity player) {
        List<BlockPos> blocksToHide = new ArrayList<>();
        blocksToHide.add(centerPos);

        // 1. DETECÇÃO (Usa a direção do JOGADOR, funciona independente da rotação da pedra)
        boolean hasX = isTarget(world, centerPos.east(), targetBlock) || isTarget(world, centerPos.west(), targetBlock);
        boolean hasZ = isTarget(world, centerPos.north(), targetBlock) || isTarget(world, centerPos.south(), targetBlock);

        Direction.Axis axisToBreak = null;

        if (hasX && hasZ) {
            axisToBreak = player.getHorizontalFacing().getAxis();
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
        allowPlayer(player.getUuid(), duration);

        world.playSound(null, centerPos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);

        BlockState airState = Blocks.AIR.getDefaultState();

        // Delay 100ms
        SCHEDULER.schedule(() -> {
            if (player.getServer() == null || player.isDisconnected()) return;
            for (BlockPos pos : blocksToHide) {
                player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, airState));
            }
        }, 100, TimeUnit.MILLISECONDS);

        // Retorno após 10s
        SCHEDULER.schedule(() -> {
            if (player.getServer() != null) {
                player.getServer().execute(() -> {
                    if (player.isDisconnected()) return;

                    for (BlockPos pos : blocksToHide) {
                        BlockState originalState = world.getBlockState(pos);
                        if (originalState.getBlock() instanceof RockSmashBlock) {
                            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, originalState));
                        }
                    }
                });
            }
        }, duration, TimeUnit.SECONDS);
    }

    private static void collectOffsets(World world, BlockPos startPos, Block targetBlock, Direction dir, List<BlockPos> list) {
        for (int i = 1; i <= 15; i++) {
            BlockPos checkPos = startPos.offset(dir, i);
            if (isTarget(world, checkPos, targetBlock)) {
                list.add(checkPos);
            } else {
                break;
            }
        }
    }

    private static boolean isTarget(World world, BlockPos pos, Block target) {
        return world.getBlockState(pos).isOf(target);
    }

    // --- Colisão e Permissões ---
    public static void allowPlayer(UUID playerUuid, long seconds) {
        long expireTime = System.currentTimeMillis() + (seconds * 1000);
        PERMISSIONS.put(playerUuid, expireTime);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof PlayerEntity player && hasPermission(player.getUuid())) {
                return VoxelShapes.empty();
            }
        }
        return SHAPE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
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