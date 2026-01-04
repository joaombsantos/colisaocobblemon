package me.marcronte.colisaocobblemon.features.fadeblock;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FadeBlock extends BaseEntityBlock {

    public static final MapCodec<FadeBlock> CODEC = simpleCodec(FadeBlock::new);
    public static BlockEntityType<FadeBlockEntity> ENTITY_TYPE;

    public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");

    public FadeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VISIBLE, true));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VISIBLE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FadeBlockEntity(pos, state); }

    // --- FISIC---
    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player) {
            if (level instanceof ServerLevel serverLevel) {
                if (FadeBlockData.get(serverLevel).isUnlocked(pos, player.getUUID())) {
                    return Shapes.empty();
                }
            }
        }
        return Shapes.block();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return true; }

    // --- INTERACTION ---
    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;


        if (FadeBlockData.get(serverLevel).isUnlocked(pos, player.getUUID())) {
            serverLevel.getServer().execute(() -> resyncChain(serverLevel, pos, serverPlayer));
            return ItemInteractionResult.CONSUME;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FadeBlockEntity fadeBe)) return ItemInteractionResult.FAIL;

        ItemStack requiredKey = fadeBe.getKeyItem();
        if (requiredKey.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        boolean sameItem = stack.is(requiredKey.getItem());
        boolean sameName = stack.getHoverName().getString().equals(requiredKey.getHoverName().getString());

        if (sameItem && sameName) {
            unlockChainReaction(serverLevel, pos, serverPlayer);
            player.displayClientMessage(Component.translatable("message.colisao-cobblemon.unlocked_path").withStyle(ChatFormatting.GREEN), true);
            return ItemInteractionResult.SUCCESS;
        } else {
            if (!stack.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.colisao-cobblemon.wrong_key").withStyle(ChatFormatting.RED), true);
            }
            return ItemInteractionResult.FAIL;
        }
    }

    // --- RESYNCHRONIZATION ---
    // Iteration on neighbours
    private void resyncChain(ServerLevel level, BlockPos startPos, ServerPlayer player) {
        FadeBlockData data = FadeBlockData.get(level);
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();

            if (data.isUnlocked(currentPos, player.getUUID())) {
                ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(currentPos, Blocks.AIR.defaultBlockState());
                player.connection.send(packet);
            }

            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(dir);
                if (visited.contains(neighborPos)) continue;

                if (level.getBlockState(neighborPos).getBlock() instanceof FadeBlock) {
                    visited.add(neighborPos);
                    queue.add(neighborPos);
                }
            }
        }
    }

    // --- LIBERAÇÃO INICIAL (COM SOM E EFEITOS) ---
    private void unlockChainReaction(ServerLevel level, BlockPos startPos, ServerPlayer player) {
        FadeBlockData data = FadeBlockData.get(level);

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> newlyUnlocked = new LinkedList<>(); // Lista para enviar ao cliente

        queue.add(startPos);
        visited.add(startPos);

        level.playSound(null, startPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);

        level.getServer().execute(() -> {
            while (!queue.isEmpty()) {
                BlockPos currentPos = queue.poll();

                // 1. Save on server
                data.unlock(currentPos, player.getUUID());

                // 2. Add on list to send to client
                newlyUnlocked.add(currentPos);

                // 3. Particles
                level.sendParticles(player, ParticleTypes.POOF, true,
                        currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5,
                        5, 0.3, 0.3, 0.3, 0.1);

                // 4. Neighbours
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = currentPos.relative(dir);
                    if (visited.contains(neighborPos)) continue;
                    if (level.getBlockState(neighborPos).getBlock() instanceof FadeBlock) {
                        visited.add(neighborPos);
                        queue.add(neighborPos);
                    }
                }
            }

            // 5. Send the list to client
            if (!newlyUnlocked.isEmpty()) {
                FadeNetwork.sendSync(player, newlyUnlocked);
            }
        });
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;

        if (player.isCreative()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FadeBlockEntity fadeBe) {
                serverPlayer.openMenu(fadeBe);
                return InteractionResult.CONSUME;
            }
        }

        if (FadeBlockData.get(serverLevel).isUnlocked(pos, player.getUUID())) {
            resyncChain(serverLevel, pos, serverPlayer);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}