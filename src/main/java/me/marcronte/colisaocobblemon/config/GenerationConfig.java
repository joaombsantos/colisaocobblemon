package me.marcronte.colisaocobblemon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GenerationConfig {

    private static GenerationConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int max_generation = 9;

    public static void load(MinecraftServer server) {
        if (ColisaoSettingsManager.getSettingsFolder() == null) {
            ColisaoSettingsManager.init(server);
        }

        File file = new File(ColisaoSettingsManager.getSettingsFolder(), "generation_limit.json");

        if (!file.exists()) {
            INSTANCE = new GenerationConfig();
            save(file);
        } else {
            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, GenerationConfig.class);
            } catch (IOException e) {
                ColisaoCobblemon.LOGGER.error("Error when loading Generation Limit", e);
                INSTANCE = new GenerationConfig();
            }
        }
    }

    public static void save() {
        if (ColisaoSettingsManager.getSettingsFolder() != null) {
            File file = new File(ColisaoSettingsManager.getSettingsFolder(), "generation_limit.json");
            save(file);
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            ColisaoCobblemon.LOGGER.error("Error when saving Generation Limit", e);
        }
    }

    public static GenerationConfig get() {
        return INSTANCE;
    }
}