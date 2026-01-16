package me.marcronte.colisaocobblemon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class EliteFourConfig {

    private static EliteFourConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public String elite_four_first = "Lorelei";
    public String elite_four_second = "Bruno";
    public String elite_four_third = "Agatha";
    public String elite_four_fourth = "Lance";
    public String elite_four_champion = "Champion Gary";

    public int tolerance_x = 100;
    public int tolerance_z = 100;
    public int tolerance_y = 30;

    public static void load(MinecraftServer server) {
        if (ColisaoSettingsManager.getSettingsFolder() == null) {
            ColisaoSettingsManager.init(server);
        }

        File file = new File(ColisaoSettingsManager.getSettingsFolder(), "elite_four_config.json");

        if (!file.exists()) {
            INSTANCE = new EliteFourConfig();
            save(file);
        } else {
            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, EliteFourConfig.class);
            } catch (IOException e) {
                ColisaoCobblemon.LOGGER.error("Error when loading Elite 4 Config", e);
                INSTANCE = new EliteFourConfig();
            }
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            ColisaoCobblemon.LOGGER.error("Error when saving Elite 4 Config", e);
        }
    }

    public static EliteFourConfig get() {
        return INSTANCE;
    }
}