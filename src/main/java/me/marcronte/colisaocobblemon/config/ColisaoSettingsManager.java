package me.marcronte.colisaocobblemon.config;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.features.routes.RouteCache;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.File;
import java.nio.file.Path;

public class ColisaoSettingsManager {

    private static File settingsFolder;

    public static void init(MinecraftServer server) {
        Path rootPath = server.getWorldPath(LevelResource.ROOT);
        settingsFolder = rootPath.resolve("colisaocobblemonsettings").toFile();

        if (!settingsFolder.exists()) {
            settingsFolder.mkdirs();
        }

        reload(server);
    }

    public static void reload(MinecraftServer server) {
        EliteFourConfig.load(server);
        GenerationConfig.load(server);
        LevelCapConfig.load(server);
        RouteConfig.load(server);
        NpcConfig.load(server);

        if (server.overworld() != null) {
            RouteCache.buildCache(server.overworld());
            ColisaoCobblemon.LOGGER.info("Cache and Routes settings loaded.");
        } else {
            ColisaoCobblemon.LOGGER.info("Settings loaded (Route's Cache waiting for world to start).");
        }
    }

    public static File getSettingsFolder() {
        return settingsFolder;
    }
}