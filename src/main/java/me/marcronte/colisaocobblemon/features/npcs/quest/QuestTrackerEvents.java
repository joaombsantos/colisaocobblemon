package me.marcronte.colisaocobblemon.features.npcs.quest;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.NpcConfig;
import me.marcronte.colisaocobblemon.data.QuestProgressData;
import me.marcronte.colisaocobblemon.features.npcs.NpcData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class QuestTrackerEvents {

    public static void register() {
        CobblemonEvents.BATTLE_VICTORY.subscribe(event -> {
            MinecraftServer server = ColisaoCobblemon.getServer();
            if (server == null) return;

            server.execute(() -> {
                try {
                    for (BattleActor winner : event.getWinners()) {
                        for (UUID uuid : winner.getPlayerUUIDs()) {
                            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                            if (player == null) continue;

                            QuestProgressData progress = QuestProgressData.get(player.serverLevel());
                            QuestCounterData counters = QuestCounterData.get(player.serverLevel());

                            for (BattleActor loser : event.getLosers()) {
                                for (BattlePokemon battlePokemon : loser.getPokemonList()) {

                                    Pokemon faintedPokemon = battlePokemon.getEffectedPokemon();

                                    if (faintedPokemon == null || !faintedPokemon.isWild()) continue;

                                    for (NpcData data : NpcConfig.getAll()) {
                                        if ("questline".equalsIgnoreCase(data.type) && data.quest_line != null) {

                                            int currentIndex = -1;
                                            for (int i = 0; i < data.quest_line.size(); i++) {
                                                String stageId = data.npc_id + "_stage_" + i;
                                                if (progress.canDoQuest(player.getUUID(), stageId, 0)) {
                                                    currentIndex = i;
                                                    break;
                                                }
                                            }

                                            if (currentIndex != -1) {
                                                NpcData.QuestNode currentNode = data.quest_line.get(currentIndex);
                                                String stageId = data.npc_id + "_stage_" + currentIndex;

                                                if (progress.hasStartedQuest(player.getUUID(), stageId)) {

                                                    if ("defeat_pokemon".equalsIgnoreCase(currentNode.objective_type)) {
                                                        String[] possibleTarget = currentNode.objective_target.split(",");
                                                        boolean correctTarget = false;

                                                        for (String target : possibleTarget) {
                                                            if (faintedPokemon.getSpecies().getName().equalsIgnoreCase(target.trim())) {
                                                                correctTarget = true;
                                                                break;
                                                            }
                                                        }

                                                        if (correctTarget) {
                                                            counters.incrementCount(player.getUUID(), stageId, 1);
                                                        }
                                                    }
                                                    else if ("defeat_type".equalsIgnoreCase(currentNode.objective_type)) {
                                                        boolean hasType = false;
                                                        for (ElementalType type : faintedPokemon.getTypes()) {
                                                            if (type.getName().equalsIgnoreCase(currentNode.objective_target)) {
                                                                hasType = true;
                                                                break;
                                                            }
                                                        }
                                                        if (hasType) {
                                                            counters.incrementCount(player.getUUID(), stageId, 1);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else if ("quest".equalsIgnoreCase(data.type)) {
                                            if (progress.hasStartedQuest(player.getUUID(), data.npc_id) && progress.canDoQuest(player.getUUID(), data.npc_id, data.quest_cooldown_hours)) {

                                                if ("defeat_pokemon".equalsIgnoreCase(data.objective_type)) {
                                                    String[] possibleTarget = data.objective_target.split(",");
                                                    boolean correctTarget = false;

                                                    for (String target : possibleTarget) {
                                                        if (faintedPokemon.getSpecies().getName().equalsIgnoreCase(target.trim())) {
                                                            correctTarget = true;
                                                            break;
                                                        }
                                                    }

                                                    if (correctTarget) {
                                                        counters.incrementCount(player.getUUID(), data.npc_id, 1);
                                                    }
                                                }
                                                else if ("defeat_type".equalsIgnoreCase(data.objective_type)) {
                                                    boolean hasType = false;
                                                    for (ElementalType type : faintedPokemon.getTypes()) {
                                                        if (type.getName().equalsIgnoreCase(data.objective_target)) {
                                                            hasType = true;
                                                            break;
                                                        }
                                                    }
                                                    if (hasType) {
                                                        counters.incrementCount(player.getUUID(), data.npc_id, 1);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[ColisaoCobblemon] Erro ao contabilizar quest de batalha!");
                    e.printStackTrace();
                }
            });
            return;
        });
    }
}