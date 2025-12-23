package me.marcronte.colisaocobblemon.features.hms;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
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

public class CutObstacleBlock extends Block {

    // --- Configurações do Cut ---
    private static final Map<UUID, Long> PERMISSIONS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 24.0D, 16.0D);

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public CutObstacleBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Métodos para permitir rotação por comandos/worldedit
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    // --- LÓGICA DE INTERAÇÃO (Cut) ---
    public static ActionResult handleInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.PASS;
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

        ItemStack stack = player.getMainHandStack();
        if (!stack.isOf(Items.SHEARS)) return ActionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof CutObstacleBlock)) {
            return ActionResult.PASS;
        }

        cutSmartLine(world, pos, state.getBlock(), (ServerPlayerEntity) player);
        stack.damage(1, player, EquipmentSlot.MAINHAND);

        return ActionResult.SUCCESS;
    }

    private static void cutSmartLine(World world, BlockPos centerPos, Block targetBlock, ServerPlayerEntity player) {
        List<BlockPos> blocksToHide = new ArrayList<>();
        blocksToHide.add(centerPos);

        // DETECÇÃO
        boolean hasX = isTarget(world, centerPos.east(), targetBlock) || isTarget(world, centerPos.west(), targetBlock);
        boolean hasZ = isTarget(world, centerPos.north(), targetBlock) || isTarget(world, centerPos.south(), targetBlock);

        Direction.Axis axisToCut = null;

        if (hasX && hasZ) {
            axisToCut = player.getHorizontalFacing().getAxis();
        } else if (hasX) {
            axisToCut = Direction.Axis.X;
        } else if (hasZ) {
            axisToCut = Direction.Axis.Z;
        }

        // COLETA
        if (axisToCut != null) {
            if (axisToCut == Direction.Axis.X) {
                collectOffsets(world, centerPos, targetBlock, Direction.EAST, blocksToHide);
                collectOffsets(world, centerPos, targetBlock, Direction.WEST, blocksToHide);
            } else {
                collectOffsets(world, centerPos, targetBlock, Direction.NORTH, blocksToHide);
                collectOffsets(world, centerPos, targetBlock, Direction.SOUTH, blocksToHide);
            }
        }

        // AÇÃO
        long duration = 10;
        allowPlayer(player.getUuid(), duration);

        BlockState airState = Blocks.AIR.getDefaultState(); // Agora funciona com o import correto

        // Envio Imediato do Pacote (Delay unificado de 100ms)
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
                        if (originalState.getBlock() instanceof CutObstacleBlock) {
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