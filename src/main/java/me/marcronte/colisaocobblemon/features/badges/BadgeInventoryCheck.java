package me.marcronte.colisaocobblemon.features.badges;

import dev.architectury.event.events.common.TickEvent;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import me.marcronte.colisaocobblemon.storage.BadgeDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

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
                    checkItem(serverPlayer, stack, i, server, foundBadges);
                }
            }
        });
    }

    private static void checkItem(ServerPlayer player, ItemStack stack, int slotIndex, MinecraftServer server, Set<String> foundBadges) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        if (!LevelCapConfig.get().badges.containsKey(itemId)) {
            return;
        }

        // --- 1. DOUBLE CHECK (Remove Duplicatas) ---
        if (foundBadges.contains(itemId)) {
            player.getInventory().setItem(slotIndex, ItemStack.EMPTY);
            return;
        } else {
            foundBadges.add(itemId);
            if (stack.getCount() > 1) {
                stack.setCount(1);
            }
        }

        // --- 2. REQUISITES VERIFICATION (Quem é o dono?) ---
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