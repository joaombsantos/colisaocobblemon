package me.marcronte.colisaocobblemon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GeneralConfig {
    private static GeneralConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean breedingCommand = true;

    public static void load(MinecraftServer server) {
        if (ColisaoSettingsManager.getSettingsFolder() == null) {
            ColisaoSettingsManager.init(server);
        }

        File file = new File(ColisaoSettingsManager.getSettingsFolder(), "general_settings.json");

        if (!file.exists()) {
            INSTANCE = new GeneralConfig();
            save(file);
        } else {
            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, GeneralConfig.class);
            } catch (IOException e) {
                ColisaoCobblemon.LOGGER.error("Erro ao carregar General Settings", e);
                INSTANCE = new GeneralConfig();
            }
        }
    }

    public static void save() {
        if (ColisaoSettingsManager.getSettingsFolder() != null) {
            File file = new File(ColisaoSettingsManager.getSettingsFolder(), "general_settings.json");
            save(file);
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            ColisaoCobblemon.LOGGER.error("Erro ao salvar General Settings", e);
        }
    }

    public static GeneralConfig get() {
        return INSTANCE;
    }
}