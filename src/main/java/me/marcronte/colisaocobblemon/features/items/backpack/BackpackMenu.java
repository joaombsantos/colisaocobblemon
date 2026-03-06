package me.marcronte.colisaocobblemon.features.items.backpack;

import me.marcronte.colisaocobblemon.ModScreenHandlers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

public class BackpackMenu extends AbstractContainerMenu {

    private final SimpleContainer container;
    private final Inventory playerInventory;
    private final InteractionHand hand;

    public final int numSlots;
    private boolean isLoading = false;

    public record Payload(boolean isMainHand, int slots) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "backpack_menu_payload"));

        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Payload::isMainHand,
                ByteBufCodecs.INT, Payload::slots,
                Payload::new
        );

        @Override
        public @NotNull Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() { return TYPE; }
    }

    public BackpackMenu(int syncId, Inventory playerInventory, Payload payload) {
        super(ModScreenHandlers.BACKPACK_MENU, syncId);
        this.playerInventory = playerInventory;
        this.hand = payload.isMainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        this.numSlots = payload.slots();

        this.container = new SimpleContainer(this.numSlots) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (!isLoading && !playerInventory.player.level().isClientSide) {
                    saveToNbt();
                }
            }
        };

        loadFromNbt();

        int rows = this.numSlots / 9;
        int inventoryYOffset = 18 * rows + 31;

        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !(stack.getItem() instanceof BackpackItem);
                    }
                });
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, inventoryYOffset + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            int slotIndex = col;
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, inventoryYOffset + 58) {
                @Override
                public boolean mayPickup(Player playerIn) {
                    return hand != InteractionHand.MAIN_HAND || playerInventory.selected != slotIndex;
                }
            });
        }
    }

    private ItemStack getBackpackStack() {
        return playerInventory.player.getItemInHand(this.hand);
    }

    private void loadFromNbt() {
        isLoading = true;
        try {
            ItemStack stack = getBackpackStack();
            if (stack.isEmpty() || !(stack.getItem() instanceof BackpackItem)) return;

            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            if (tag.contains("BackpackItems")) {
                NonNullList<ItemStack> items = NonNullList.withSize(this.numSlots, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(tag.getCompound("BackpackItems"), items, playerInventory.player.registryAccess());
                for (int i = 0; i < items.size(); i++) {
                    container.setItem(i, items.get(i));
                }
            }
        } finally {
            isLoading = false;
        }
    }

    private void saveToNbt() {
        if (playerInventory.player.level().isClientSide) return;

        ItemStack stack = getBackpackStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof BackpackItem)) return;

        NonNullList<ItemStack> items = NonNullList.withSize(this.numSlots, ItemStack.EMPTY);
        for (int i = 0; i < this.numSlots; i++) {
            items.set(i, container.getItem(i));
        }

        CompoundTag inventoryTag = new CompoundTag();
        ContainerHelper.saveAllItems(inventoryTag, items, playerInventory.player.registryAccess());

        CompoundTag rootTag = new CompoundTag();
        rootTag.put("BackpackItems", inventoryTag);

        CustomData newData = CustomData.of(rootTag);
        stack.set(DataComponents.CUSTOM_DATA, newData);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            if (index >= this.container.getContainerSize() && slotStack.getItem() instanceof BackpackItem) {
                return ItemStack.EMPTY;
            }

            if (index < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(slotStack, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, this.container.getContainerSize(), false)) {
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
        ItemStack stack = player.getItemInHand(this.hand);
        return !stack.isEmpty() && stack.getItem() instanceof BackpackItem;
    }
}