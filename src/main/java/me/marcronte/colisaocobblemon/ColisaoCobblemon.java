package me.marcronte.colisaocobblemon;

import dev.architectury.networking.NetworkManager;
import me.marcronte.colisaocobblemon.config.ColisaoSettingsManager;
import me.marcronte.colisaocobblemon.config.GenerationConfig;
import me.marcronte.colisaocobblemon.features.RideRequirement;
import me.marcronte.colisaocobblemon.features.UndroppableItems;
import me.marcronte.colisaocobblemon.features.badges.*;
import me.marcronte.colisaocobblemon.features.boostpad.BoostPadBlock;
import me.marcronte.colisaocobblemon.features.boostpad.BoostPadHandler;
import me.marcronte.colisaocobblemon.features.elitefour.EliteFourHandler;
import me.marcronte.colisaocobblemon.features.eventblock.EventBlockRegistry;
import me.marcronte.colisaocobblemon.features.fadeblock.*;
import me.marcronte.colisaocobblemon.features.eventblock.EventBattleHandler;
import me.marcronte.colisaocobblemon.features.genlimit.GenerationCommand;
import me.marcronte.colisaocobblemon.features.genlimit.GenerationLimiter;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootNetwork;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootRegistry;
import me.marcronte.colisaocobblemon.features.switchstate.*;
import me.marcronte.colisaocobblemon.features.teleportblock.TeleportRegistry;
import me.marcronte.colisaocobblemon.network.BadgeNetwork;
import me.marcronte.colisaocobblemon.network.BoostNetwork;
import me.marcronte.colisaocobblemon.network.GenLimitNetwork;
import me.marcronte.colisaocobblemon.network.TeleportNetwork;
import net.fabricmc.api.ModInitializer;
import me.marcronte.colisaocobblemon.features.hms.HmManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ColisaoCobblemon implements ModInitializer {
	public static final String MOD_ID = "colisao-cobblemon";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static MinecraftServer serverInstance;
	public static final ResourceLocation GEN_LIMIT_PACKET_ID = ResourceLocation.fromNamespaceAndPath("colisao_cobblemon", "gen_limit");

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

		// Event Block
		EventBlockRegistry.register();
		EventBattleHandler.register();

		// Items
		ModItems.register();

		// State Block Mechanic
		SwitchStateRegistry.register();
		SwitchNetwork.registerCommon();

		// Teleport Block
		TeleportRegistry.register();
		TeleportNetwork.registerCommon();
		TeleportNetwork.registerServerReceiver();

		// Elite Four
		EliteFourHandler.register();

		// Gen Limit
		GenerationLimiter.register();
		GenLimitNetwork.registerCommon();

		// Gen Command
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> GenerationCommand.register(dispatcher));

		// Creative only Sign edit
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!player.isCreative()) {
				var state = world.getBlockState(hitResult.getBlockPos());
				if (state.is(BlockTags.SIGNS) || state.is(BlockTags.ALL_HANGING_SIGNS)) {
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});

        ServerLifecycleEvents.SERVER_STARTING.register(ColisaoSettingsManager::init);


		PayloadTypeRegistry.playS2C().register(GenLimitPayload.ID, GenLimitPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			int limit = GenerationConfig.get().max_generation;
			ServerPlayNetworking.send(handler.getPlayer(), new GenLimitPayload(limit));
		});

		// SERVER START CAPTURE
		ServerLifecycleEvents.SERVER_STARTED.register(server -> serverInstance = server);

		// CLEARS THE REFERENCE WHEN STOPPED (memory leak protection)
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverInstance = null);
	}
	public static MinecraftServer getServer() {
		return serverInstance;
	}

}
