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
import java.util.List;
import java.util.Map;

public class PokemonDropConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static PokemonDropConfig INSTANCE;

    public Map<String, List<DropEntry>> drops = new HashMap<>();

    public static class DropEntry {
        public String item;
        public double chance;
        public int min_quantity;
        public int max_quantity;

        public DropEntry(String item, double chance, int min_quantity, int max_quantity) {
            this.item = item;
            this.chance = chance;
            this.min_quantity = min_quantity;
            this.max_quantity = max_quantity;
        }
    }

    public PokemonDropConfig() {
        this.drops.put("normal", List.of(new DropEntry("minecraft:bone", 25.5, 1, 3)));
        this.drops.put("pidgey", List.of(new DropEntry("minecraft:feather", 50.0, 1, 2)));
    }

    public static void load(MinecraftServer server) {
        if (ColisaoSettingsManager.getSettingsFolder() == null) {
            ColisaoSettingsManager.init(server);
        }

        File file = new File(ColisaoSettingsManager.getSettingsFolder(), "pokemon_drop.json");

        if (!file.exists()) {
            INSTANCE = new PokemonDropConfig();
            save(file);
        } else {
            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, PokemonDropConfig.class);
            } catch (IOException e) {
                ColisaoCobblemon.LOGGER.error("Error when loading Pokemon Drop Config", e);
                INSTANCE = new PokemonDropConfig();
            }
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            ColisaoCobblemon.LOGGER.error("Error when saving Pokemon Drop Config", e);
        }
    }
}