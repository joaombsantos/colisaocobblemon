package me.marcronte.colisaocobblemon.features.npcs.quest;

import me.marcronte.colisaocobblemon.config.NpcConfig;
import me.marcronte.colisaocobblemon.data.QuestProgressData;
import me.marcronte.colisaocobblemon.features.npcs.NpcData;
import me.marcronte.colisaocobblemon.network.payloads.QuestBookPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class QuestBookManager {

    public static void openBookFor(ServerPlayer player) {
        QuestProgressData progress = QuestProgressData.get(player.serverLevel());
        List<QuestBookPayload.QuestEntry> entries = new ArrayList<>();

        for (NpcData data : NpcConfig.getAll()) {
            if ("dialog".equalsIgnoreCase(data.type)) continue;

            String baseName = (data.quest_name != null && !data.quest_name.isEmpty()) ? data.quest_name : data.npc_name;
            int category = 1;
            int status = 0;
            String title = "???";
            String description = "Você ainda não encontrou este personagem.";

            if ("quest".equalsIgnoreCase(data.type)) {
                if (data.quest_cooldown_hours > 0) category = 0;

                boolean started = progress.hasStartedQuest(player.getUUID(), data.npc_id);
                boolean canDo = progress.canDoQuest(player.getUUID(), data.npc_id, data.quest_cooldown_hours);

                if (!canDo) {
                    status = 2;
                    title = baseName;
                    description = "Concluída! " + (category == 0 ? "Volte amanhã." : "");
                } else if (started) {
                    status = 1;
                    title = baseName;
                    description = data.give_quest + "\n\nFalta: " + data.quest_amount;
                }
            }
            else if ("questline".equalsIgnoreCase(data.type) && data.quest_line != null) {
                category = 2;
                int totalStages = data.quest_line.size();
                int currentIndex = -1;

                for (int i = 0; i < totalStages; i++) {
                    if (progress.canDoQuest(player.getUUID(), data.npc_id + "_stage_" + i, 0)) {
                        currentIndex = i;
                        break;
                    }
                }

                if (currentIndex == -1) {
                    status = 2;
                    title = baseName;
                    description = "Questline totalmente concluída!";
                } else {
                    String stageId = data.npc_id + "_stage_" + currentIndex;
                    if (progress.hasStartedQuest(player.getUUID(), stageId)) {
                        status = 1;
                        NpcData.QuestNode node = data.quest_line.get(currentIndex);
                        title = baseName + " (" + (currentIndex + 1) + "/" + totalStages + ")";
                        description = node.give_quest;
                    } else if (currentIndex > 0) {
                        status = 1;
                        title = baseName + " (" + (currentIndex + 1) + "/" + totalStages + ")";
                        description = "Fale com " + data.npc_name + " para continuar a história.";
                    }
                }
            }

            entries.add(new QuestBookPayload.QuestEntry(data.npc_id, category, status, title, description));
        }

        ServerPlayNetworking.send(player, new QuestBookPayload(entries));
    }
}