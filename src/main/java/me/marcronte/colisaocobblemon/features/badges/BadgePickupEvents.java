package me.marcronte.colisaocobblemon.features.badges;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import me.marcronte.colisaocobblemon.storage.BadgeDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class BadgePickupEvents {

    public static void register() {
        PlayerEvent.PICKUP_ITEM_PRE.register((player, itemEntity, stack) -> {

            if (!(player instanceof ServerPlayer serverPlayer)) return EventResult.pass();

            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            String requiredTrainer = LevelCapConfig.get().getRequiredTrainer(itemId);

            if (requiredTrainer != null) {
                MinecraftServer server = ColisaoCobblemon.getServer();
                if (server != null) {
                    BadgeDataManager dataManager = BadgeDataManager.getServerState(server);

                    if (!dataManager.hasDefeated(serverPlayer.getUUID(), requiredTrainer)) {

                        serverPlayer.displayClientMessage(
                                Component.translatable("message.colisao-cobblemon.badge_requirement", requiredTrainer)
                                        .withStyle(ChatFormatting.RED),
                                true
                        );

                        return EventResult.interruptFalse();
                    }
                }
            }

            return EventResult.pass();
        });
    }
}