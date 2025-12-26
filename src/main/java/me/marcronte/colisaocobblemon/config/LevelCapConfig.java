package me.marcronte.colisaocobblemon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LevelCapConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("colisao-cobblemon");
    private static LevelCapConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int startLevel = 10;
    public Map<String, Integer> badges = new HashMap<>();
    public Map<String, String> badgeRequirements = new HashMap<>();

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(LevelCapConfig::load);
    }

    public static void load(MinecraftServer server) {
        Path worldDir = server.getWorldPath(LevelResource.ROOT);
        File file = worldDir.resolve("cobblemon_level_cap.json").toFile();

        if (!file.exists()) {
            INSTANCE = new LevelCapConfig();
            INSTANCE.setupDefaults();
            save(file);
        } else {
            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, LevelCapConfig.class);
            } catch (IOException e) {
                LOGGER.error("Error when loading Level Cap configuration. Using standards.", e);
                INSTANCE = new LevelCapConfig();
            }
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            LOGGER.error("Error when saving Level Cap configurations.", e);
        }
    }

    private void setupDefaults() {
        startLevel = 10;
        badges.put("colisao-cobblemon:kanto_boulder_badge", 20);
        badges.put("colisao-cobblemon:kanto_cascade_badge", 30);
        badges.put("colisao-cobblemon:kanto_thunder_badge", 40);
        badges.put("colisao-cobblemon:kanto_rainbow_badge", 50);
        badges.put("colisao-cobblemon:kanto_soul_badge", 60);
        badges.put("colisao-cobblemon:kanto_marsh_badge", 70);
        badges.put("colisao-cobblemon:kanto_volcano_badge", 80);
        badges.put("colisao-cobblemon:kanto_earth_badge", 90);
        badges.put("colisao-cobblemon:kanto_champion_badge", 100);

        badgeRequirements.put("colisao-cobblemon:kanto_boulder_badge", "GYM Leader Brock");
        badgeRequirements.put("colisao-cobblemon:kanto_cascade_badge", "GYM Leader Misty");
        badgeRequirements.put("colisao-cobblemon:kanto_thunder_badge", "GYM Leader Lt. Surge");
        badgeRequirements.put("colisao-cobblemon:kanto_rainbow_badge", "GYM Leader Erika");
        badgeRequirements.put("colisao-cobblemon:kanto_soul_badge", "GYM Leader Koga");
        badgeRequirements.put("colisao-cobblemon:kanto_marsh_badge", "GYM Leader Sabrina");
        badgeRequirements.put("colisao-cobblemon:kanto_volcano_badge", "GYM Leader Blaine");
        badgeRequirements.put("colisao-cobblemon:kanto_earth_badge", "GYM Leader Giovani");
        badgeRequirements.put("colisao-cobblemon:kanto_champion_badge", "Pokemon Champion Blue");
    }

    public static LevelCapConfig get() {
        return INSTANCE;
    }

    public int getCapForBadge(String itemId) {
        return badges.getOrDefault(itemId, 0);
    }

    public String getRequiredTrainer(String itemId) { return badgeRequirements.get(itemId); }
}