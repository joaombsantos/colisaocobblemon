package me.marcronte.colisaocobblemon.features.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LureRarityManager {

    private static final Map<String, Integer> SPECIES_WEIGHTS = new HashMap<>();

    private static int getWeightFromBucket(String bucket) {
        if (bucket == null) {
            System.out.println("Bucket é nulo");
            return 50;
        }
        return switch (bucket.toLowerCase()) {
            case "common" -> 100;
            case "uncommon" -> 50;
            case "rare" -> 15;
            case "ultra-rare" -> 1;
            default -> 50;
        };
    }

    public static void initializeRarities(MinecraftServer server) {
        SPECIES_WEIGHTS.clear();
        ResourceManager manager = server.getResourceManager();
        Gson gson = new Gson();

        Map<ResourceLocation, Resource> resources = manager.listResources("cobblemon/spawn_pool_world", path -> path.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try (InputStream stream = entry.getValue().open();
                 InputStreamReader reader = new InputStreamReader(stream)) {

                JsonObject json = gson.fromJson(reader, JsonObject.class);

                if (json.has("spawns")) {
                    JsonArray spawns = json.getAsJsonArray("spawns");

                    for (JsonElement element : spawns) {
                        JsonObject spawnInfo = element.getAsJsonObject();

                        if (spawnInfo.has("pokemon") && spawnInfo.has("bucket")) {
                            String speciesName = "";

                            if (spawnInfo.get("pokemon").isJsonPrimitive()) {
                                speciesName = spawnInfo.get("pokemon").getAsString();
                            } else if (spawnInfo.get("pokemon").isJsonObject()) {
                                JsonObject pokeObj = spawnInfo.get("pokemon").getAsJsonObject();
                                if (pokeObj.has("species")) {
                                    speciesName = pokeObj.get("species").getAsString();
                                }
                            }

                            if (!speciesName.isEmpty()) {
                                speciesName = speciesName.replace("cobblemon:", "").replace("species:", "").trim().toLowerCase();
                                String bucket = spawnInfo.get("bucket").getAsString();

                                int weight = getWeightFromBucket(bucket);
                                SPECIES_WEIGHTS.merge(speciesName, weight, Math::max);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        System.out.println("[ColisaoCobblemon] Raridades mapeadas via JSON! Espécies registradas: " + SPECIES_WEIGHTS.size());
    }

    public static int getSpeciesWeight(String speciesName) {
        return SPECIES_WEIGHTS.getOrDefault(speciesName.toLowerCase(), 1);
    }
}