package me.marcronte.colisaocobblemon.features.breeding.habitat;

import me.marcronte.colisaocobblemon.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HabitatMenu extends AbstractContainerMenu {

    private final Container habitatInventory;
    private final ContainerData data;
    public final BlockPos blockPos;

    public HabitatMenu(int syncId, Inventory playerInventory, Container inventory, ContainerData data, BlockPos pos) {
        super(ModScreenHandlers.HABITAT_MENU, syncId);
        this.habitatInventory = inventory;
        this.data = data;
        this.blockPos = pos;

        checkContainerSize(inventory, 2);
        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 44, 189));
        this.addSlot(new Slot(inventory, 1, 197, 189));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 48 + col * 18, 260 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 48 + col * 18, 318));
        }

        this.addDataSlots(data);
    }

    public HabitatMenu(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, new SimpleContainer(2), new SimpleContainerData(5), pos);
    }

    public int getProgress() { return this.data.get(0); }
    public int getRequired() { return this.data.get(1); }
    public int getFuel() { return this.data.get(2); }
    public int getPoints() { return this.data.get(3); }
    public boolean isActive() { return this.data.get(4) == 1; }

    @Override
    public boolean stillValid(Player player) {
        return this.habitatInventory.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            itemStack = originalStack.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(originalStack, 2, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(originalStack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
            if (originalStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.habitatInventory.stopOpen(player);
    }
}