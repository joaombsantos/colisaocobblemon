package me.marcronte.colisaocobblemon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfessionsCraftsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static ProfessionsCraftsConfig INSTANCE;

    public Map<String, Map<String, RankData>> crafts = new HashMap<>();

    public static class RankData {
        public String name;
        public List<CraftData> craft_list = new ArrayList<>();

        public RankData(String name) {
            this.name = name;
        }
    }

    public static class CraftData {
        public String result_item;
        public List<String> ingredients;
        public int exp_needed;
        public int exp_reward;
        public int limit_exp;

        public CraftData(String result_item, List<String> ingredients, int exp_needed, int exp_reward) {
            this.result_item = result_item;
            this.ingredients = ingredients;
            this.exp_needed = exp_needed;
            this.exp_reward = exp_reward;
        }
    }

    public ProfessionsCraftsConfig() {
        RankData rankE = new RankData("Aprendiz");

        rankE.craft_list.add(new CraftData(
                "minecraft:diamond_sword",
                List.of("2_minecraft:diamond", "1_minecraft:stick"),
                25,
                2
        ));

        rankE.craft_list.add(new CraftData(
                "minecraft:diamond_pickaxe",
                List.of("3_minecraft:diamond", "2_minecraft:stick"),
                30,
                3
        ));

        Map<String, RankData> engMap = new HashMap<>();
        engMap.put("rank_e", rankE);

        this.crafts.put("engenheiro", engMap);
    }

    public static void load(MinecraftServer server) {
        if (ColisaoSettingsManager.getSettingsFolder() == null) {
            ColisaoSettingsManager.init(server);
        }

        File file = new File(ColisaoSettingsManager.getSettingsFolder(), "professions_crafts.json");

        if (!file.exists()) {
            INSTANCE = new ProfessionsCraftsConfig();
            save(file);
        } else {
            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, ProfessionsCraftsConfig.class);
            } catch (IOException e) {
                ColisaoCobblemon.LOGGER.error("Error when loading Professions Crafts Config", e);
                INSTANCE = new ProfessionsCraftsConfig();
            }
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            ColisaoCobblemon.LOGGER.error("Error when saving Professions Crafts Config", e);
        }
    }
}