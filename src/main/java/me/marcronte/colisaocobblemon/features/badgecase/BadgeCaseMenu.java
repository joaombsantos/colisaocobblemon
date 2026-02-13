package me.marcronte.colisaocobblemon.features.badgecase;

import me.marcronte.colisaocobblemon.ModScreenHandlers;
import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import me.marcronte.colisaocobblemon.storage.BadgeDataManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BadgeCaseMenu extends AbstractContainerMenu {

    private final SimpleContainer container;
    private final Inventory playerInventory;
    private final InteractionHand hand;
    private boolean isLoading = false;

    public record Payload(boolean isMainHand) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Payload::isMainHand,
                Payload::new
        );
    }

    public BadgeCaseMenu(int syncId, Inventory playerInventory, Payload payload) {
        super(ModScreenHandlers.KANTO_BADGE_CASE_MENU, syncId);
        this.playerInventory = playerInventory;
        this.hand = payload.isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        this.container = new SimpleContainer(9) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (!isLoading && !playerInventory.player.level().isClientSide) {
                    saveToNbt();
                }
            }
        };

        loadFromNbt();

        // --- SLOTS BADGE CASE ---
        addBadgeSlot(0, 15, 16);   // First
        addBadgeSlot(1, 49, 16);   // Second
        addBadgeSlot(2, 82, 16);   // Third
        addBadgeSlot(3, 114, 16);  // Forth
        addBadgeSlot(4, 15, 40);   // Fifth
        addBadgeSlot(5, 49, 40);   // Sixth
        addBadgeSlot(6, 82, 40);   // Seventh
        addBadgeSlot(7, 114, 40);  // Eighth
        addBadgeSlot(8, 145, 28);  // Champion

        // --- INVENTORY ITERATION ---
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // --- HOTBAR ---
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private void addBadgeSlot(int index, int x, int y) {
        this.addSlot(new Slot(container, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                Item requiredBadge = BadgeCaseData.getBadgeForSlot(index);
                return requiredBadge != null && stack.is(requiredBadge);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                if (!isLoading && !playerInventory.player.level().isClientSide && hasItem()) {
                    syncLevelCapData((ServerPlayer) playerInventory.player, getItem());
                }
            }
        });
    }

    private ItemStack getBadgeCaseStack() {
        return playerInventory.player.getItemInHand(this.hand);
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide) {
            saveToNbt();
        }
        super.removed(player);
    }

    private void loadFromNbt() {
        isLoading = true;
        try {
            ItemStack stack = getBadgeCaseStack();
            if (stack.isEmpty()) return;

            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            if (tag.contains("Items")) {
                ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);
                container.clearContent();

                for (int i = 0; i < listTag.size(); ++i) {
                    CompoundTag itemTag = listTag.getCompound(i);
                    int slot = itemTag.getByte("Slot") & 255;

                    if (slot >= 0 && slot < container.getContainerSize()) {
                        ItemStack item = ItemStack.parseOptional(playerInventory.player.registryAccess(), itemTag);
                        container.setItem(slot, item);
                    }
                }
            }
        } finally {
            isLoading = false;
        }
    }

    private void saveToNbt() {
        if (playerInventory.player.level().isClientSide) return;

        ItemStack stack = getBadgeCaseStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof BadgeCaseItem)) return;

        ListTag listTag = new ListTag();

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack item = container.getItem(i);
            if (!item.isEmpty()) {
                CompoundTag itemTag = (CompoundTag) item.save(playerInventory.player.registryAccess());
                itemTag.putByte("Slot", (byte) i);
                listTag.add(itemTag);

                syncLevelCapData((ServerPlayer) playerInventory.player, item);
            }
        }

        CompoundTag rootTag = new CompoundTag();
        rootTag.put("Items", listTag);

        CustomData newData = CustomData.of(rootTag);
        stack.set(DataComponents.CUSTOM_DATA, newData);

        playerInventory.setItem(hand == InteractionHand.MAIN_HAND ? playerInventory.selected : 40, stack);
    }

    public void recoverBadges(ServerPlayer player) {
        BadgeDataManager data = BadgeDataManager.getServerState(player.server);
        Set<String> unlocked = data.getBadges(player.getUUID());

        int recoveredCount = 0;

        for (String badgeId : unlocked) {
            if (hasItemInInventory(player, badgeId)) continue;
            if (hasItemInCase(badgeId)) continue;

            Item item = BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(badgeId));
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                recoveredCount++;
            }
        }

        if (recoveredCount > 0) {
            /*player.sendSystemMessage(Component.literal("Recovered " + recoveredCount + " lost badges."));*/
            saveToNbt();
        } else {
            /*player.sendSystemMessage(Component.literal("No lost badge was found."));*/
        }
    }

    private boolean hasItemInInventory(ServerPlayer player, String badgeId) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(badgeId)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasItemInCase(String badgeId) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(badgeId)) {
                return true;
            }
        }
        return false;
    }

    private void syncLevelCapData(ServerPlayer player, ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (LevelCapConfig.get().badges.containsKey(itemId)) {
            BadgeDataManager data = BadgeDataManager.getServerState(player.server);
            if (!data.hasBadge(player.getUUID(), itemId)) {
                data.addBadge(player.getUUID(), itemId);
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();

            if (index < 9) {
                if (!this.moveItemStackTo(itemStack2, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, 9, false)) {
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
        ItemStack currentStack = player.getItemInHand(this.hand);
        return !currentStack.isEmpty() && currentStack.getItem() instanceof BadgeCaseItem;
    }
}