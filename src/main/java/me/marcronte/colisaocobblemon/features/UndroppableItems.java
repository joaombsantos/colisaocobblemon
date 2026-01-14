package me.marcronte.colisaocobblemon.features;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

            ItemStack stack = itemEntity.getItem();
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            if (UNDROPPABLE_IDS.contains(itemId)) {

                if (player instanceof ServerPlayer serverPlayer) {
                    ItemStack toReturn = stack.copy();

                    if (!serverPlayer.getInventory().add(toReturn)) {
                        return EventResult.pass();
                    }

                    serverPlayer.displayClientMessage(
                            Component.translatable("message.colisao-cobblemon.drop_item")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );

                    serverPlayer.inventoryMenu.sendAllDataToRemote();
                }

                return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });
    }
}