package me.marcronte.colisaocobblemon.config;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ColisaoSettingsManager {

    private static File settingsFolder;

    public static void init(MinecraftServer server) {
        Path rootPath = server.getWorldPath(LevelResource.ROOT);
        settingsFolder = rootPath.resolve("colisaocobblemonsettings").toFile();

        if (!settingsFolder.exists()) {
            settingsFolder.mkdirs();
        }

        // TODO REMOVE THIS ON NEXT PATCH
        migrateLevelCapFile(rootPath);

        EliteFourConfig.load(server);
        GenerationConfig.load(server);
        LevelCapConfig.load(server);
    }

    public static File getSettingsFolder() {
        return settingsFolder;
    }

    private static void migrateLevelCapFile(Path rootPath) {
        File oldFile = rootPath.resolve("cobblemon_level_cap.json").toFile();
        File newFile = new File(settingsFolder, "cobblemon_level_cap.json");

        if (oldFile.exists() && !newFile.exists()) {
            try {
                Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                ColisaoCobblemon.LOGGER.info("Migrado cobblemon_level_cap.json para a nova pasta.");
            } catch (IOException e) {
                ColisaoCobblemon.LOGGER.error("Erro na migração do Level Cap", e);
            }
        }
    }
}