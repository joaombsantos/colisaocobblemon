package me.marcronte.colisaocobblemon.features.badges;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import me.marcronte.colisaocobblemon.storage.BadgeDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class TrainerBattleEvents {

    public static void register() {
        CobblemonEvents.BATTLE_VICTORY.subscribe(event -> {
            MinecraftServer server = ColisaoCobblemon.getServer();
            if (server == null) return;

            BadgeDataManager dataManager = BadgeDataManager.getServerState(server);

            for (BattleActor winner : event.getWinners()) {
                ServerPlayer player = server.getPlayerList().getPlayer(winner.getUuid());

                if (player != null) {

                    for (BattleActor loser : event.getLosers()) {

                        ServerPlayer loserPlayer = server.getPlayerList().getPlayer(loser.getUuid());

                        if (loserPlayer == null) {

                            String trainerName = loser.getName().getString();

                            if (!dataManager.hasDefeated(player.getUUID(), trainerName)) {
                                dataManager.addDefeatedTrainer(player.getUUID(), trainerName);

                                player.displayClientMessage(
                                        Component.translatable("message.colisao-cobblemon.trainer_defeated", trainerName)
                                                .withStyle(ChatFormatting.GREEN),
                                        true
                                );

                                giveBadgeReward(player, trainerName);

                            }
                        }
                    }
                }
            }
        });
    }

    private static void giveBadgeReward(ServerPlayer player, String trainerName) {
        for (Map.Entry<String, String> entry : LevelCapConfig.get().badgeRequirements.entrySet()) {
            String badgeId = entry.getKey();
            String requiredTrainer = entry.getValue();

            if (requiredTrainer.equalsIgnoreCase(trainerName)) {

                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(badgeId));
                ItemStack badgeStack = new ItemStack(item);

                boolean added = player.getInventory().add(badgeStack);

                if (!added) {
                    player.drop(badgeStack, false);
                    player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.inventory_is_full")
                            .withStyle(ChatFormatting.YELLOW));
                }

                break;
            }
        }
    }
}