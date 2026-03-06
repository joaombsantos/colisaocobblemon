package me.marcronte.colisaocobblemon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfessionsPerksConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static ProfessionsPerksConfig INSTANCE;

    public Map<String, Map<String, Object>> perks = new HashMap<>();

    public ProfessionsPerksConfig() {
        Map<String, Object> estilistaRankE = new HashMap<>();
        estilistaRankE.put("pikachu_cap", "minecraft:leather_5");

        Map<String, Object> estilistaData = new HashMap<>();
        estilistaData.put("rank_e", estilistaRankE);

        this.perks.put("estilista", estilistaData);
    }

    public static void load(MinecraftServer server) {
        if (ColisaoSettingsManager.getSettingsFolder() == null) {
            ColisaoSettingsManager.init(server);
        }

        File file = new File(ColisaoSettingsManager.getSettingsFolder(), "professions_perks.json");

        if (!file.exists()) {
            INSTANCE = new ProfessionsPerksConfig();
            save(file);
        } else {
            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, ProfessionsPerksConfig.class);
            } catch (IOException e) {
                ColisaoCobblemon.LOGGER.error("Error when loading Professions Perks Config", e);
                INSTANCE = new ProfessionsPerksConfig();
            }
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            ColisaoCobblemon.LOGGER.error("Error when saving Professions Perks Config", e);
        }
    }
}