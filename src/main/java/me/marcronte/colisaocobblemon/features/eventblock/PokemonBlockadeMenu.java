package me.marcronte.colisaocobblemon.features.eventblock;

import me.marcronte.colisaocobblemon.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PokemonBlockadeMenu extends AbstractContainerMenu {

    public final Container container;
    public final BlockPos pos;

    public final String loadedProps;
    public final String loadedEventId;
    public final boolean loadedCatchable;
    public final String loadedCheckMessage;
    public final String loadedWakeMessage;
    public final int loadedHitboxSize; // NOVO

    public PokemonBlockadeMenu(int syncId, Inventory playerInventory, PokemonBlockadeEntity.OpeningData data) {
        this(syncId, playerInventory, new SimpleContainer(1), data);
    }

    public PokemonBlockadeMenu(int syncId, Inventory playerInventory, Container container, PokemonBlockadeEntity.OpeningData data) {
        super(ModScreenHandlers.POKEMON_BLOCKADE_MENU, syncId);
        this.container = container;
        this.pos = data.pos();
        this.loadedProps = data.props();
        this.loadedEventId = data.eventId();
        this.loadedCatchable = data.catchable();
        this.loadedCheckMessage = data.checkMessage();
        this.loadedWakeMessage = data.wakeMessage();
        this.loadedHitboxSize = data.hitboxSize(); // CARREGA

        checkContainerSize(container, 1);
        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, 0, 80, 74));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 162));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (index < 1) {
                if (!this.moveItemStackTo(originalStack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(originalStack, 0, 1, false)) return ItemStack.EMPTY;

            if (originalStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}