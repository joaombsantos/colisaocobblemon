package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import me.marcronte.colisaocobblemon.features.badges.BadgeItems;
import me.marcronte.colisaocobblemon.features.badges.LevelCapEvents;
import net.fabricmc.api.ModInitializer;
import me.marcronte.colisaocobblemon.features.hms.HmManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColisaoCobblemon implements ModInitializer {
	public static final String MOD_ID = "colisao-cobblemon";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static MinecraftServer serverInstance;
	@Override
	public void onInitialize() {
		LOGGER.info("Inicializando Colisao Cobblemon...");

		// Inicializa os módulos
		ModItemGroup.register();
		HmManager.register();
		BadgeItems.register();
		LevelCapConfig.register();
		LevelCapEvents.register();

		// Captura o servidor quando ele inicia
		ServerLifecycleEvents.SERVER_STARTED.register(server -> serverInstance = server);

		// Limpa a referência quando desliga (para evitar memory leaks)
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverInstance = null);
	}
	public static MinecraftServer getServer() {
		return serverInstance;
	}
}
