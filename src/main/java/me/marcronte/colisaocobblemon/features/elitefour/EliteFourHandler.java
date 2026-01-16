package me.marcronte.colisaocobblemon.features.elitefour;

import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.EliteFourConfig;
import me.marcronte.colisaocobblemon.storage.EliteFourDataManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class EliteFourHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EliteFourDataManager dataManager = EliteFourDataManager.getServerState(server);

            for (UUID uuid : new java.util.HashSet<>(dataManager.getAllPlayers().keySet())) {
                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player == null) continue;

                checkDistance(player, uuid, dataManager);
            }
        });

        CobblemonEvents.BATTLE_STARTED_PRE.subscribe(EliteFourHandler::handleBattleStart);

        CobblemonEvents.BATTLE_VICTORY.subscribe(EliteFourHandler::handleBattleResult);

        CobblemonEvents.BATTLE_FLED.subscribe(EliteFourHandler::handleBattleFled);
    }

    private static void checkDistance(ServerPlayer player, UUID uuid, EliteFourDataManager dataManager) {
        EliteFourDataManager.EliteData data = dataManager.getPlayerProgress(uuid);
        if (data == null || data.anchor == null) return;

        EliteFourConfig config = EliteFourConfig.get();
        BlockPos currentPos = player.blockPosition();
        BlockPos anchor = data.anchor;

        boolean tooFarX = Math.abs(currentPos.getX() - anchor.getX()) > config.tolerance_x;
        boolean tooFarZ = Math.abs(currentPos.getZ() - anchor.getZ()) > config.tolerance_z;
        boolean tooFarY = Math.abs(currentPos.getY() - anchor.getY()) > config.tolerance_y;

        if (tooFarX || tooFarZ || tooFarY) {
            dataManager.removePlayer(uuid);
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_too_far"));
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_goto_start"));
        }
    }

    private static void handleBattleStart(BattleStartedEvent.Pre event) {
        ServerPlayer player = null;
        String opponentName = null;

        MinecraftServer server = ColisaoCobblemon.getServer();
        if (server == null) return;

        for (BattleActor actor : event.getBattle().getActors()) {
            if (actor.getType() == ActorType.PLAYER) {
                player = server.getPlayerList().getPlayer(actor.getUuid());
            } else {
                opponentName = actor.getName().getString();
            }
        }

        if (player == null || opponentName == null) return;

        EliteFourDataManager dataManager = EliteFourDataManager.getServerState(server);
        EliteFourDataManager.EliteData progress = dataManager.getPlayerProgress(player.getUUID());

        int currentStage = (progress == null) ? 0 : progress.stage;
        int targetStage = getEliteFourStage(opponentName);

        if (targetStage == -1) return;

        if (targetStage != currentStage) {
            event.cancel();
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_cannot_challenge", opponentName));

            EliteFourConfig config = EliteFourConfig.get();
            if (currentStage == 0) {
                player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_beat_first", config.elite_four_first));
            } else {
                player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_follow_order"));
            }
        }
    }

    private static void handleBattleResult(BattleVictoryEvent event) {
        MinecraftServer server = ColisaoCobblemon.getServer();
        if (server == null) return;

        for (BattleActor winner : event.getWinners()) {
            if (winner.getType() == ActorType.PLAYER) {
                ServerPlayer player = server.getPlayerList().getPlayer(winner.getUuid());
                if (player != null) {
                    // Quem ele venceu?
                    for (BattleActor loser : event.getLosers()) {
                        if (loser.getType() != ActorType.PLAYER) {
                            String npcName = loser.getName().getString();
                            handlePlayerWin(server, player, npcName);
                        }
                    }
                }
            }
        }

        for (BattleActor loser : event.getLosers()) {
            if (loser.getType() == ActorType.PLAYER) {
                ServerPlayer player = server.getPlayerList().getPlayer(loser.getUuid());
                if (player != null) {
                    // Para quem ele perdeu?
                    for (BattleActor winner : event.getWinners()) {
                        if (winner.getType() != ActorType.PLAYER) {
                            String npcName = winner.getName().getString();
                            handlePlayerLoss(server, player, npcName);
                        }
                    }
                }
            }
        }
    }

    private static void handleBattleFled(BattleFledEvent event) {
        MinecraftServer server = ColisaoCobblemon.getServer();
        if (server == null) return;

        ServerPlayer player = null;
        String opponentName = null;

        for (BattleActor actor : event.getBattle().getActors()) {
            if (actor.getType() == ActorType.PLAYER) {
                player = server.getPlayerList().getPlayer(actor.getUuid());
            } else {
                opponentName = actor.getName().getString();
            }
        }

        if (player != null && opponentName != null) {
            handlePlayerLoss(server, player, opponentName);
        }
    }


    private static void handlePlayerWin(MinecraftServer server, ServerPlayer player, String npcName) {
        EliteFourConfig config = EliteFourConfig.get();

        if (npcName.equalsIgnoreCase(config.elite_four_first)) advanceProgress(server, player, 1);
        else if (npcName.equalsIgnoreCase(config.elite_four_second)) advanceProgress(server, player, 2);
        else if (npcName.equalsIgnoreCase(config.elite_four_third)) advanceProgress(server, player, 3);
        else if (npcName.equalsIgnoreCase(config.elite_four_fourth)) advanceProgress(server, player, 4);
        else if (npcName.equalsIgnoreCase(config.elite_four_champion)) completeChallenge(server, player);
    }

    private static void handlePlayerLoss(MinecraftServer server, ServerPlayer player, String npcName) {
        int stage = getEliteFourStage(npcName);

        if (stage != -1) {
            EliteFourDataManager.getServerState(server).removePlayer(player.getUUID());

            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_lost"));
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_lost_to", npcName));
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_progress_reset"));
        }
    }

    private static int getEliteFourStage(String name) {
        EliteFourConfig config = EliteFourConfig.get();
        if (name.equalsIgnoreCase(config.elite_four_first)) return 0;
        if (name.equalsIgnoreCase(config.elite_four_second)) return 1;
        if (name.equalsIgnoreCase(config.elite_four_third)) return 2;
        if (name.equalsIgnoreCase(config.elite_four_fourth)) return 3;
        if (name.equalsIgnoreCase(config.elite_four_champion)) return 4;
        return -1;
    }

    private static void advanceProgress(MinecraftServer server, ServerPlayer player, int newStage) {
        BlockPos newAnchor = player.blockPosition();
        EliteFourDataManager.getServerState(server).setPlayerProgress(player.getUUID(), newStage, newAnchor);

        EliteFourConfig config = EliteFourConfig.get();
        String nextOpponent = switch (newStage) {
            case 1 -> config.elite_four_second;
            case 2 -> config.elite_four_third;
            case 3 -> config.elite_four_fourth;
            case 4 -> config.elite_four_champion;
            default -> "???";
        };

        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_victory"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_next", nextOpponent));
    }

    private static void completeChallenge(MinecraftServer server, ServerPlayer player) {
        EliteFourDataManager.getServerState(server).removePlayer(player.getUUID());
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.elite_four_champion"));
    }
}