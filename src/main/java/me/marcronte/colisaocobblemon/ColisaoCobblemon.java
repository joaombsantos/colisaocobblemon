package me.marcronte.colisaocobblemon;

import net.fabricmc.api.ModInitializer;
import me.marcronte.colisaocobblemon.features.hms.HmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColisaoCobblemon implements ModInitializer {
	public static final String MOD_ID = "colisao-cobblemon";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Inicializando Colisao Cobblemon...");

		// Inicializa o módulo de HMs
		HmManager.register();

	}
}