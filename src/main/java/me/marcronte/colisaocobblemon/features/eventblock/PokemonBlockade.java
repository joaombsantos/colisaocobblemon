package me.marcronte.colisaocobblemon.features.eventblock;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PokemonBlockade extends BaseEntityBlock {

    public static final MapCodec<PokemonBlockade> CODEC = simpleCodec(PokemonBlockade::new);
    public static BlockEntityType<PokemonBlockadeEntity> ENTITY_TYPE;

    public PokemonBlockade(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PokemonBlockadeEntity(pos, state); }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof PokemonBlockadeEntity be) {
            int size = be.getHitboxSize();
            double offset = (size - 1) / 2.0;
            return Shapes.create(new AABB(0.0 - offset, 0.0, 0.0 - offset, 1.0 + offset, size, 1.0 + offset));
        }
        return Shapes.block();
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player) {
            if (level instanceof ServerLevel serverLevel) {
                if (player.isCreative() && player.isCrouching()) {
                    return Shapes.empty();
                }

                if (level.getBlockEntity(pos) instanceof PokemonBlockadeEntity be) {
                    if (EventBlockData.get(serverLevel).isCompleted(player.getUUID(), be.getEventId())) {
                        return Shapes.empty();
                    }
                }
            }
        }
        return getShape(state, level, pos, context);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PokemonBlockadeEntity blockade)) return ItemInteractionResult.FAIL;

        if (player.isCreative() && player.isCrouching()) {
            serverPlayer.openMenu(blockade);
            return ItemInteractionResult.SUCCESS;
        }

        if (EventBlockData.get(serverLevel).isCompleted(serverPlayer.getUUID(), blockade.getEventId())) {
            serverPlayer.connection.send(new ClientboundBlockUpdatePacket(pos, Blocks.AIR.defaultBlockState()));
            return ItemInteractionResult.CONSUME;
        }

        ItemStack requiredKey = blockade.getRequiredKeyItem();

        if (requiredKey.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean sameItem = stack.is(requiredKey.getItem());
        if (sameItem) {
            blockade.activateBattle(serverPlayer);
            return ItemInteractionResult.SUCCESS;
        } else {
            player.displayClientMessage(Component.literal(blockade.getCheckMessage()).withStyle(ChatFormatting.YELLOW), true);
        }
        return ItemInteractionResult.FAIL;
    }


    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PokemonBlockadeEntity blockade) {

            if (player.isCreative() && player.isCrouching()) {
                player.openMenu(blockade);
                return InteractionResult.CONSUME;
            }

            if (EventBlockData.get(serverLevel).isCompleted(serverPlayer.getUUID(), blockade.getEventId())) {
                serverPlayer.connection.send(new ClientboundBlockUpdatePacket(pos, Blocks.AIR.defaultBlockState()));
                return InteractionResult.CONSUME;
            }

            ItemStack requiredKey = blockade.getRequiredKeyItem();

            if (requiredKey.isEmpty()) {
                blockade.activateBattle(serverPlayer);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal(blockade.getCheckMessage()).withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return createTickerHelper(type, EventBlockRegistry.POKEMON_BLOCKADE_ENTITY, PokemonBlockadeEntity::clientTick);
        }
        return createTickerHelper(type, EventBlockRegistry.POKEMON_BLOCKADE_ENTITY, PokemonBlockadeEntity::serverTick);
    }
}