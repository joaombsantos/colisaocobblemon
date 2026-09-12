package me.marcronte.colisaocobblemon.features.blocks.machines;

import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.network.BreedingNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class PokemonWorkBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public PokemonWorkBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof PokemonWorkBlockEntity machine) {
                safelyReturnPokemon(level, pos, machine);

                net.minecraft.world.Containers.dropContents(level, pos, (net.minecraft.world.Container) machine);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof PokemonWorkBlockEntity machine) {
            if (machine.getPokemonData() != null) {
                return 0.0F;
            }
        }

        return super.getDestroyProgress(state, player, level, pos);
    }

    private void safelyReturnPokemon(Level level, BlockPos pos, PokemonWorkBlockEntity machine) {
        if (level.isClientSide || machine.getPokemonData() == null || machine.getOwnerUuid() == null) return;

        ServerPlayer player = Objects.requireNonNull(level.getServer()).getPlayerList().getPlayer(machine.getOwnerUuid());

        if (player != null) {
            Pokemon p = BreedingNetwork.reconstructPokemon(machine.getPokemonData(), level.registryAccess());
            if (p != null) {
                int targetFriendship = machine.getMachineFriendship();
                com.cobblemon.mod.common.api.pokemon.PokemonProperties.Companion.parse("friendship=" + targetFriendship).apply(p);
                BreedingNetwork.givePokemonOrToPC(player, p);
            }
        } else {
            net.minecraft.world.item.ItemStack safeMachine = new net.minecraft.world.item.ItemStack(this);
            CompoundTag tag = new CompoundTag();
            machine.saveAdditional(tag, level.registryAccess());

            net.minecraft.world.item.component.CustomData.set(
                    net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
                    safeMachine,
                    tag
            );

            net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), safeMachine);
        }

        machine.clearPokemon();
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);

        if (drops.isEmpty()) {
            drops = new ArrayList<>(drops);
            drops.add(new ItemStack(this.asItem()));
        }

        return drops;
    }
}