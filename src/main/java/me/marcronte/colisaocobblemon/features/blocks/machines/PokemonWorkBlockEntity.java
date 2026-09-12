package me.marcronte.colisaocobblemon.features.blocks.machines;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.network.BreedingNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class PokemonWorkBlockEntity extends BlockEntity {

    protected CompoundTag pokemonData = null;
    protected UUID ownerUuid = null;

    protected int progressTicks = 0;

    public PokemonWorkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public enum WorkPower {
        WEAK,    // BST < 300
        MEDIUM,  // BST 300 - 499
        STRONG   // BST >= 500
    }

    // --- ABSTRACTS ---

    public abstract boolean isValidPokemon(Pokemon pokemon);

    protected abstract boolean canWork(Level level, BlockPos pos, Pokemon pokemon, WorkPower power);

    protected abstract void performWork(Level level, BlockPos pos, Pokemon pokemon, WorkPower power);

    protected abstract int calculateWorkTime(WorkPower power);

    protected abstract int getFriendshipConsumption();

    // -----------------------------------------------------------

    public void tick(Level level, BlockPos pos) {
        if (level.isClientSide() || pokemonData == null) return;

        Pokemon pokemon = BreedingNetwork.reconstructPokemon(pokemonData, level.registryAccess());
        if (pokemon == null) return;

        if (getMachineFriendship() < getFriendshipConsumption()) {
            this.progressTicks = 0;
            return;
        }

        WorkPower power = calculatePower(pokemon);

        if (canWork(level, pos, pokemon, power)) {
            this.progressTicks++;
            int maxTicks = calculateWorkTime(power);

            if (this.progressTicks >= maxTicks) {
                performWork(level, pos, pokemon, power);

                this.progressTicks = 0;
                this.setChanged();
            }
        } else {
            if (this.progressTicks > 0) {
                this.progressTicks = 0;
                this.setChanged();
            }
        }
    }

    protected WorkPower calculatePower(Pokemon pokemon) {
        int bst = 0;
        bst += pokemon.getSpecies().getBaseStats().getOrDefault(Stats.HP, 0);
        bst += pokemon.getSpecies().getBaseStats().getOrDefault(Stats.ATTACK, 0);
        bst += pokemon.getSpecies().getBaseStats().getOrDefault(Stats.DEFENCE, 0);
        bst += pokemon.getSpecies().getBaseStats().getOrDefault(Stats.SPECIAL_ATTACK, 0);
        bst += pokemon.getSpecies().getBaseStats().getOrDefault(Stats.SPECIAL_DEFENCE, 0);
        bst += pokemon.getSpecies().getBaseStats().getOrDefault(Stats.SPEED, 0);

        if (bst >= 500) return WorkPower.STRONG;
        if (bst > 300) return WorkPower.MEDIUM;
        return WorkPower.WEAK;
    }

    public void insertPokemon(ServerPlayer player, Pokemon pokemon) {
        this.ownerUuid = player.getUUID();
        this.pokemonData = BreedingNetwork.savePokemonSecurely(pokemon, player);
        this.progressTicks = 0;
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public static Pokemon loadPokemonFromTag(CompoundTag tag, net.minecraft.core.RegistryAccess registryAccess) {
        if (tag == null || tag.isEmpty()) return null;

        String speciesStr = tag.contains("Species") ? tag.getString("Species") : "";
        int level = tag.contains("Level") ? tag.getInt("Level") : 1;

        Pokemon p;
        try {
            if (!speciesStr.isEmpty()) {
                p = com.cobblemon.mod.common.api.pokemon.PokemonProperties.Companion.parse("species=" + speciesStr + " level=" + level).create();
            } else {
                p = new Pokemon();
            }
        } catch (Exception e) {
            p = new Pokemon();
        }

        p.loadFromNBT(registryAccess, tag);
        return p;
    }

    public CompoundTag getPokemonData() {
        return pokemonData;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public int getMachineFriendship() {
        if (pokemonData == null) return 0;
        if (pokemonData.contains("Friendship")) return pokemonData.getInt("Friendship");
        if (pokemonData.contains("friendship")) return pokemonData.getInt("friendship");
        return 0;
    }

    public void setMachineFriendship(int value) {
        if (pokemonData != null) {
            pokemonData.putInt("Friendship", value);
            pokemonData.putInt("friendship", value);
            this.setChanged();
        }
    }

    public void clearPokemon() {
        this.pokemonData = null;
        this.progressTicks = 0;
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.ownerUuid != null) tag.putUUID("Owner", this.ownerUuid);
        if (this.pokemonData != null) tag.put("PokemonData", this.pokemonData);
        tag.putInt("Progress", this.progressTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("Owner")) this.ownerUuid = tag.getUUID("Owner");
        if (tag.contains("PokemonData")) {
            this.pokemonData = tag.getCompound("PokemonData");
        } else {
            this.pokemonData = null;
        }
        this.progressTicks = tag.getInt("Progress");
    }
}