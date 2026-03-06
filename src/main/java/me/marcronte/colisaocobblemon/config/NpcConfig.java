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
import java.util.ArrayList;
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

        // ==========================================
        // 1. QUEST
        // ==========================================
        NpcData questNpc = new NpcData();
        questNpc.npc_id = "joao_pescador";
        questNpc.npc_name = "João Pescador";
        questNpc.skin = "Steve";
        questNpc.type = "quest";
        questNpc.quest_item = "minecraft:pufferfish";
        questNpc.quest_amount = 5;
        questNpc.give_quest = "Olá viajante! Preciso de 5 Baiacus para minha sopa.";
        questNpc.quest_accept = "Vou pescar!";
        questNpc.quest_in_progress = "Ainda não pescou os Baiacus? Estou com fome!";
        questNpc.quest_delivery = "Oh! Meus peixes!";
        questNpc.deliver_quest = "Aqui estão!";
        questNpc.quest_complete = "Muito obrigado! Aqui está sua recompensa.";
        questNpc.cooldown_message = "Volte amanhã, agora estou cheio.";
        questNpc.quest_cooldown_hours = 24;
        questNpc.commands = List.of("give {player} diamond 1", "say {player} ajudou o João!");

        // ==========================================
        // 2. DIALOG
        // ==========================================
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
        dialogNpc.repeatable = true;

        // ==========================================
        // 3. QUESTLINE
        // ==========================================
        NpcData questlineNpc = new NpcData();
        questlineNpc.npc_id = "mestre_pokemon";
        questlineNpc.npc_name = "Mestre Carvalho";
        questlineNpc.skin = "https://www.minecraftskins.com/uploads/skins/2025/03/24/professor-oak-23138919.png?v940"; // It can be an URL too
        questlineNpc.type = "questline";
        questlineNpc.questline_finished = "Você provou ser um verdadeiro Mestre Pokémon! Não tenho mais nada a ensinar.";
        questlineNpc.quest_line = new ArrayList<>();

        // STEP 1: Bring Item
        NpcData.QuestNode stage0 = new NpcData.QuestNode();
        stage0.objective_type = "item";
        stage0.objective_target = "minecraft:diamond";
        stage0.quest_amount = 1;
        stage0.give_quest = "Para começar seu treinamento, me traga 1 Diamante para provar sua determinação.";
        stage0.quest_accept = "Vou procurar!";
        stage0.quest_in_progress = "Ainda estou esperando aquele diamante...";
        stage0.quest_delivery = "Você encontrou o diamante?";
        stage0.deliver_quest = "Sim, aqui está.";
        stage0.quest_complete = "Excelente! Primeira lição concluída.";
        stage0.commands = List.of("give {player} cobblemon:rare_candy 1");
        questlineNpc.quest_line.add(stage0);

        // STEP 2: Beat specific Pokémon
        NpcData.QuestNode stage1 = new NpcData.QuestNode();
        stage1.objective_type = "defeat_pokemon";
        stage1.objective_target = "zubat";
        stage1.quest_amount = 3;
        stage1.give_quest = "A caverna aqui perto está infestada! Derrote 3 Zubats selvagens.";
        stage1.quest_accept = "Deixa comigo!";
        stage1.quest_in_progress = "Ouço os morcegos daqui. Continue batalhando!";
        stage1.quest_delivery = "A caverna parece mais silenciosa. Você terminou?";
        stage1.deliver_quest = "Sim, derrotei todos.";
        stage1.quest_complete = "Muito bem! O ar puro voltou.";
        stage1.commands = List.of("give {player} cobblemon:repel 5");
        questlineNpc.quest_line.add(stage1);

        // STEP 3: Beat specific Type
        NpcData.QuestNode stage2 = new NpcData.QuestNode();
        stage2.objective_type = "defeat_type";
        stage2.objective_target = "flying";
        stage2.quest_amount = 3;
        stage2.give_quest = "Quero ver como você lida com vantagem de tipos. Derrote 3 Pokémons do tipo Voador.";
        stage2.quest_accept = "Entendido.";
        stage2.quest_in_progress = "Pokémons voadores são rápidos. Use ataques elétricos ou de gelo!";
        stage2.quest_delivery = "Terminou de limpar os céus?";
        stage2.deliver_quest = "Missão cumprida.";
        stage2.quest_complete = "Belo trabalho com as vantagens de tipo!";
        stage2.commands = List.of("give {player} cobblemon:thunder_stone 1");
        questlineNpc.quest_line.add(stage2);

        // STEP 4: Capture specific Pokémon
        NpcData.QuestNode stage3 = new NpcData.QuestNode();
        stage3.objective_type = "dex_specific";
        stage3.objective_target = "pikachu";
        stage3.quest_amount = 1;
        stage3.give_quest = "Dizem que o Pikachu é um Pokémon muito leal. Capture um para sua Pokédex.";
        stage3.quest_accept = "Vou capturar!";
        stage3.quest_in_progress = "Ele adora florestas. Já achou um?";
        stage3.quest_delivery = "Me mostre sua Pokédex...";
        stage3.deliver_quest = "Olhe aqui meu Pikachu.";
        stage3.quest_complete = "Incrível! Ele é adorável.";
        stage3.commands = List.of("give {player} cobblemon:light_ball 1");
        questlineNpc.quest_line.add(stage3);

        // STEP 5: Capture specific amount
        NpcData.QuestNode stage4 = new NpcData.QuestNode();
        stage4.objective_type = "dex_count";
        stage4.objective_target = "any";
        stage4.quest_amount = 5;
        stage4.give_quest = "Você está pegando o jeito! Volte a falar comigo quando tiver 5 Pokémons diferentes registrados.";
        stage4.quest_accept = "Vou completar a dex!";
        stage4.quest_in_progress = "Continue explorando e jogando Pokébolas!";
        stage4.quest_delivery = "Sua Pokédex parece pesada. Alcançou a meta?";
        stage4.deliver_quest = "Sim, veja!";
        stage4.quest_complete = "5 Pokémons! Você é um pesquisador nato.";
        stage4.commands = List.of("give {player} cobblemon:ultra_ball 30");
        questlineNpc.quest_line.add(stage4);

        // STEP 6: Capture specific entries or entire generation
        NpcData.QuestNode stage5 = new NpcData.QuestNode();
        stage5.objective_type = "dex_gen";
        stage5.objective_target = "1-3";
        stage5.quest_amount = 3;
        stage5.give_quest = "Seu desafio final: Complete a Pokédex da Região de Kanto (1 a 151)!";
        stage5.quest_accept = "Desafio aceito!";
        stage5.quest_in_progress = "Kanto tem muitos mistérios. Continue buscando!";
        stage5.quest_delivery = "É verdade? Você tem todos os 151?";
        stage5.deliver_quest = "Eu sou um Mestre!";
        stage5.quest_complete = "FANTÁSTICO! Você fez história hoje.";
        stage5.commands = List.of("give {player} cobblemon:master_ball 1");
        questlineNpc.quest_line.add(stage5);


        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(List.of(questNpc, dialogNpc, questlineNpc), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static NpcData get(String id) {
        return NPCS.get(id);
    }

    public static java.util.Collection<NpcData> getAll() {
        return NPCS.values();
    }
}