package me.marcronte.colisaocobblemon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.marcronte.colisaocobblemon.features.npcs.NpcData;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Map<String, NpcData> NPCS = new HashMap<>();

    public static void load(MinecraftServer server) {
        File settingsFolder = ColisaoSettingsManager.getSettingsFolder();
        File configFile = new File(settingsFolder, "npcs.json");

        if (!configFile.exists()) {
            createDefault(configFile);
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type listType = new TypeToken<List<NpcData>>() {}.getType();
            List<NpcData> loadedList = GSON.fromJson(reader, listType);

            NPCS.clear();
            if (loadedList != null) {
                for (NpcData data : loadedList) {
                    NPCS.put(data.npc_id, data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDefault(File configFile) {
        configFile.getParentFile().mkdirs();

        NpcData questNpc = new NpcData();
        questNpc.npc_id = "joao_pescador";
        questNpc.npc_name = "João Pescador";
        questNpc.skin = "Steve";
        questNpc.type = "quest";
        questNpc.quest_item = "minecraft:pufferfish";
        questNpc.quest_amount = 5;
        questNpc.give_quest = "Olá viajante! Preciso de 5 Baiacus para minha sopa.";
        questNpc.quest_in_progress = "Ainda não pescou os Baiacus? Estou com fome!";
        questNpc.quest_delivery = "Oh! Meus peixes!";
        questNpc.quest_complete = "Muito obrigado! Aqui está sua recompensa.";
        questNpc.cooldown_message = "Volte amanhã, agora estou cheio.";
        questNpc.quest_cooldown_hours = 24;
        questNpc.commands = List.of("give {player} diamond 1", "say {player} ajudou o João!");


        NpcData dialogNpc = new NpcData();
        dialogNpc.npc_id = "mestre_pocoes";
        dialogNpc.npc_name = "Mestre das Poções";
        dialogNpc.skin = "https://www.minecraftskins.com/uploads/skins/2025/03/24/professor-oak-23138919.png?v939";
        dialogNpc.type = "dialog";
        dialogNpc.dialog = "Pegue esta poção por conta da casa, jovem treinador!";
        dialogNpc.dialog_option = "Obrigado!";
        dialogNpc.commands = List.of(
                "give {player} cobblemon:potion 1",
                "say O Mestre das Poções presenteou {player}!"
        );

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(List.of(questNpc, dialogNpc), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static NpcData get(String id) {
        return NPCS.get(id);
    }
}