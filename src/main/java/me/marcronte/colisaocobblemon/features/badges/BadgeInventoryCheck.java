package me.marcronte.colisaocobblemon.features.badges;

import dev.architectury.event.events.common.TickEvent;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import me.marcronte.colisaocobblemon.features.badgecase.BadgeCaseItem;
import me.marcronte.colisaocobblemon.storage.BadgeDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashSet;
import java.util.Set;

public class BadgeInventoryCheck {

    public static void register() {
        TickEvent.PLAYER_POST.register(player -> {

            if (player.tickCount % 20 != 0) return;

            if (!(player instanceof ServerPlayer serverPlayer)) return;
            if (serverPlayer.isCreative()) return;

            if (serverPlayer.tickCount < 100) return;

            MinecraftServer server = ColisaoCobblemon.getServer();
            if (server == null) return;

            Set<String> foundBadges = new HashSet<>();

            for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
                ItemStack stack = serverPlayer.getInventory().getItem(i);

                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof BadgeCaseItem) {
                        checkBadgeCase(serverPlayer, stack, server, foundBadges);
                    } else {
                        checkItem(serverPlayer, stack, i, server, foundBadges);
                    }
                }
            }
        });
    }

    private static void checkBadgeCase(ServerPlayer player, ItemStack caseStack, MinecraftServer server, Set<String> foundBadges) {
        CustomData customData = caseStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!tag.contains("Items")) return;

        ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);
        boolean modified = false;

        for (int i = listTag.size() - 1; i >= 0; i--) {
            CompoundTag itemTag = listTag.getCompound(i);
            ItemStack badgeStack = ItemStack.parseOptional(player.registryAccess(), itemTag);

            if (!badgeStack.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(badgeStack.getItem()).toString();
                String requiredTrainer = LevelCapConfig.get().getRequiredTrainer(itemId);

                boolean shouldRemove = false;

                if (requiredTrainer != null) {
                    BadgeDataManager dataManager = BadgeDataManager.getServerState(server);
                    if (!dataManager.hasDefeated(player.getUUID(), requiredTrainer)) {
                        shouldRemove = true;

                        ItemStack toDrop = badgeStack.copy();
                        toDrop.setCount(1);
                        ItemEntity itemEntity = player.drop(toDrop, true);
                        if (itemEntity != null) {
                            itemEntity.setPickUpDelay(100);
                        }

                        player.displayClientMessage(
                                Component.translatable("message.colisao-cobblemon.badge_requirement", requiredTrainer)
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                }

                if (foundBadges.contains(itemId)) {
                    shouldRemove = true;
                } else if (!shouldRemove) {
                    foundBadges.add(itemId);
                }

                if (shouldRemove) {
                    listTag.remove(i);
                    modified = true;
                }
            }
        }

        if (modified) {
            tag.put("Items", listTag);
            caseStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static void checkItem(ServerPlayer player, ItemStack stack, int slotIndex, MinecraftServer server, Set<String> foundBadges) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        if (!LevelCapConfig.get().badges.containsKey(itemId)) {
            return;
        }

        if (foundBadges.contains(itemId)) {
            player.getInventory().setItem(slotIndex, ItemStack.EMPTY);
            return;
        } else {
            foundBadges.add(itemId);
            if (stack.getCount() > 1) {
                stack.setCount(1);
            }
        }

        String requiredTrainer = LevelCapConfig.get().getRequiredTrainer(itemId);

        if (requiredTrainer != null) {
            BadgeDataManager dataManager = BadgeDataManager.getServerState(server);

            if (!dataManager.hasDefeated(player.getUUID(), requiredTrainer)) {
                ItemStack toDrop = stack.copy();
                toDrop.setCount(1);
                player.getInventory().setItem(slotIndex, ItemStack.EMPTY);

                ItemEntity itemEntity = player.drop(toDrop, true);
                if (itemEntity != null) {
                    itemEntity.setPickUpDelay(100);
                }

                player.displayClientMessage(
                        Component.translatable("message.colisao-cobblemon.badge_requirement", requiredTrainer)
                                .withStyle(ChatFormatting.RED),
                        true
                );
            }
        }
    }
}