package me.marcronte.colisaocobblemon.features.pokeloot;

import me.marcronte.colisaocobblemon.ModScreenHandlers;
import net.minecraft.core.BlockPos; // Importante
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PokeLootMenu extends AbstractContainerMenu {

    private final Container lootContainer;
    public final PokeLootBlockEntity blockEntity;

    public PokeLootMenu(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, new SimpleContainer(1), (PokeLootBlockEntity) playerInventory.player.level().getBlockEntity(pos));
    }

    public PokeLootMenu(int syncId, Inventory playerInventory, Container lootContainer, PokeLootBlockEntity blockEntity) {
        super(ModScreenHandlers.POKE_LOOT_MENU, syncId);
        this.lootContainer = lootContainer;
        this.blockEntity = blockEntity;

        // Loot Slot (Slot 0)
        this.addSlot(new Slot(lootContainer, 0, 80, 35) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Player's inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemStack2, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return lootContainer.stillValid(player);
    }
}