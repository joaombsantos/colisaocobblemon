package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import me.marcronte.colisaocobblemon.features.RideRequirement;
import me.marcronte.colisaocobblemon.features.UndroppableItems;
import me.marcronte.colisaocobblemon.features.badges.*;
import me.marcronte.colisaocobblemon.features.boostpad.BoostPadBlock;
import me.marcronte.colisaocobblemon.features.boostpad.BoostPadHandler;
import me.marcronte.colisaocobblemon.features.fadeblock.*;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootNetwork;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootRegistry;
import me.marcronte.colisaocobblemon.network.BadgeNetwork;
import me.marcronte.colisaocobblemon.network.BoostNetwork;
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

		// INITIALIZE MODULES
		ModItemGroup.register();

		// Badges & Level Cap
		BadgeItems.register();
		BadgePickupEvents.register();
		BadgeInventoryCheck.register();
		BadgeNetwork.register();
		LevelCapConfig.register();
		LevelCapEvents.register();
		TrainerBattleEvents.register();

		// Features Diversas
		HmManager.register();
		ModScreenHandlers.register();
		UndroppableItems.register();
		RideRequirement.register();

		// PokeLoot
		PokeLootRegistry.register();
		PokeLootNetwork.register();

		// Boost Pad
		BoostNetwork.registerCommon();
		BoostPadBlock.register();
		BoostPadHandler.register();

		// Fade Block
		FadeBlockRegistry.register();


		// SERVER START CAPTURE
		ServerLifecycleEvents.SERVER_STARTED.register(server -> serverInstance = server);

		// CLEARS THE REFERENCE WHEN STOPPED (memory leak protection)
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverInstance = null);
	}
	public static MinecraftServer getServer() {
		return serverInstance;
	}

}
