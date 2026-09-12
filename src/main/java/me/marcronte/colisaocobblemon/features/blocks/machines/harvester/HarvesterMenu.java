package me.marcronte.colisaocobblemon.features.blocks.machines.harvester;

import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.ModScreenHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public class HarvesterMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;
    private final BlockPos blockPos;
    private final HarvesterBlockEntity blockEntity;

    public HarvesterMenu(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, new net.minecraft.world.SimpleContainer(27), new net.minecraft.world.inventory.SimpleContainerData(3), pos);
    }

    public HarvesterMenu(int syncId, Inventory playerInventory, Container container, ContainerData data, BlockPos pos) {
        super(ModScreenHandlers.HARVESTER_MENU, syncId);
        this.container = container;
        this.data = data;
        this.blockPos = pos;

        if (playerInventory.player.level().getBlockEntity(pos) instanceof HarvesterBlockEntity be) {
            this.blockEntity = be;
        } else {
            this.blockEntity = null;
        }

        checkContainerSize(container, 27);
        checkContainerDataCount(data, 3);
        container.startOpen(playerInventory.player);

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 87 + row * 18));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 153 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 211));
        }

        this.addDataSlots(data);
    }

    public int getFriendship() { return this.data.get(0); }
    public boolean hasPokemon() { return this.data.get(1) == 1; }
    public int getPowerLevel() { return this.data.get(2); }
    public BlockPos getBlockPos() { return this.blockPos; }


    private Pokemon cachedPokemon = null;
    private CompoundTag lastKnownTag = null;

    public Pokemon getPokemon() {
        if (this.blockEntity != null && blockEntity.getPokemonData() != null) {
            CompoundTag currentTag = blockEntity.getPokemonData();

            if (this.cachedPokemon == null || !currentTag.equals(this.lastKnownTag)) {
                this.lastKnownTag = currentTag;
                try {
                    assert Minecraft.getInstance().level != null;
                    this.cachedPokemon = me.marcronte.colisaocobblemon.network.BreedingNetwork.reconstructPokemon(currentTag, Minecraft.getInstance().level.registryAccess());
                } catch (Exception e) {
                    return null;
                }
            }
            return this.cachedPokemon;
        }

        this.cachedPokemon = null;
        this.lastKnownTag = null;
        return null;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            if (index < 27) {
                if (!this.moveItemStackTo(slotStack, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }
}