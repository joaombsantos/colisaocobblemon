package me.marcronte.colisaocobblemon.features;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class UndroppableItems {

    private static final Set<String> UNDROPPABLE_IDS = Set.of(
            "minecraft:shears",
            "minecraft:netherite_pickaxe",
            "colisao-cobblemon:surf",
            "colisao-cobblemon:kanto_badge_case",
            "colisao-cobblemon:card_key",
            "colisao-cobblemon:coin_case",
            "colisao-cobblemon:golden_teeth",
            "colisao-cobblemon:lift_key",
            "colisao-cobblemon:oak_parcel",
            "colisao-cobblemon:secret_key",
            "colisao-cobblemon:ss_ticket",
            "colisao-cobblemon:tea",
            "colisao-cobblemon:poke_flute",
            "colisao-cobblemon:silph_scope",
            "colisao-cobblemon:running_shoes"
    );

    public static void register() {
        PlayerEvent.DROP_ITEM.register((player, itemEntity) -> {

            if (player.isCreative()) {
                return EventResult.pass();
            }

            ItemStack stackToDrop = itemEntity.getItem();
            Item itemType = stackToDrop.getItem();
            String itemId = BuiltInRegistries.ITEM.getKey(itemType).toString();

            if (UNDROPPABLE_IDS.contains(itemId)) {

                if (player instanceof ServerPlayer serverPlayer) {

                    itemEntity.setItem(ItemStack.EMPTY);
                    itemEntity.discard();

                    boolean alreadyHasItem = false;

                    for (ItemStack invStack : serverPlayer.getInventory().items) {
                        if (!invStack.isEmpty() && invStack.getItem() == itemType) {
                            alreadyHasItem = true;
                            break;
                        }
                    }
                    if (!alreadyHasItem) {
                        for (ItemStack invStack : serverPlayer.getInventory().offhand) {
                            if (!invStack.isEmpty() && invStack.getItem() == itemType) {
                                alreadyHasItem = true;
                                break;
                            }
                        }
                    }

                    if (alreadyHasItem) {
                        serverPlayer.displayClientMessage(
                                Component.translatable("message.colisao-cobblemon.drop_item")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                        serverPlayer.inventoryMenu.sendAllDataToRemote();
                    } else {
                        ItemStack toRestore = stackToDrop.copy();

                        serverPlayer.getServer().execute(() -> {
                            if (serverPlayer.isAlive()) {
                                if (!serverPlayer.getInventory().add(toRestore)) {
                                    serverPlayer.drop(toRestore, false);
                                }

                                serverPlayer.displayClientMessage(
                                        Component.translatable("message.colisao-cobblemon.drop_item")
                                                .withStyle(ChatFormatting.RED),
                                        true
                                );
                                serverPlayer.inventoryMenu.sendAllDataToRemote();
                            }
                        });
                    }

                    return EventResult.interruptFalse();
                }
            }

            return EventResult.pass();
        });
    }
}