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

public class RouteConfig {

    private static RouteConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Map<String, RouteData> routes = new HashMap<>();

    public static class RouteData {
        public int limit = 15;

        public List<SpawnEntry> species = new ArrayList<>();
        public List<SpawnEntry> fishing_species = new ArrayList<>();
    }

    public static class SpawnEntry {
        public String species;
        public int min_iv = 0;
        public int max_iv = 31;
        public int min_lvl = 5;
        public int max_lvl = 10;
        public float chance = 10.0f;
        public String spawnWeather = "all";
        public String spawnTime = "all";
        public List<String> spawnsOn = new ArrayList<>();
        public String specific_pokeball;
    }

    public static void load(MinecraftServer server) {
        if (ColisaoSettingsManager.getSettingsFolder() == null) ColisaoSettingsManager.init(server);
        File file = new File(ColisaoSettingsManager.getSettingsFolder(), "routes.json");

        if (!file.exists()) {
            INSTANCE = new RouteConfig();
            // Exemplo para o arquivo inicial
            RouteData exampleRoute = new RouteData();
            exampleRoute.limit = 15; // Padrão

            SpawnEntry pidgey = new SpawnEntry();
            pidgey.species = "pidgey";
            pidgey.chance = 25.0f;
            pidgey.spawnsOn.add("minecraft:grass_block");

            exampleRoute.species.add(pidgey);
            INSTANCE.routes.put("route1", exampleRoute);

            save(file);
        } else {
            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, RouteConfig.class);
            } catch (IOException e) {
                ColisaoCobblemon.LOGGER.error("Error when loading routes.json", e);
                INSTANCE = new RouteConfig();
            }
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            ColisaoCobblemon.LOGGER.error("Error when saving routes.json", e);
        }
    }

    public static RouteConfig get() { return INSTANCE; }
}