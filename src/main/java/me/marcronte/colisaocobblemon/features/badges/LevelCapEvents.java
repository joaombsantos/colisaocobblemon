package me.marcronte.colisaocobblemon.features.badges;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer; // Mudou de ServerPlayerEntity
import net.minecraft.network.chat.Component;   // Mudou de Text
import net.minecraft.ChatFormatting;             // Mudou de Formatting

import java.util.UUID;

public class LevelCapEvents {

    public static void register() {

        // --- 1. BLOQUEAR GANHO DE XP ---
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            MinecraftServer server = ColisaoCobblemon.getServer();

            if (server == null) return;

            UUID ownerId = pokemon.getOwnerUUID();
            if (ownerId != null) {
                // ServerPlayer em vez de ServerPlayerEntity
                ServerPlayer player = server.getPlayerList().getPlayer(ownerId); // getPlayerList() em vez de getPlayerManager()

                if (player != null) {
                    int cap = LevelCapCalculator.getPlayerLevelCap(player);

                    if (pokemon.getLevel() >= cap) {
                        event.setExperience(0);
                    }
                }
            }
        });

        // --- 2. TRAVA CONTRA RARE CANDY (Level Up) ---
        CobblemonEvents.LEVEL_UP_EVENT.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            MinecraftServer server = ColisaoCobblemon.getServer();

            if (server == null) return;

            int proposedLevel = event.getNewLevel();
            UUID ownerId = pokemon.getOwnerUUID();

            if (ownerId != null) {
                ServerPlayer player = server.getPlayerList().getPlayer(ownerId);

                if (player != null) {
                    int cap = LevelCapCalculator.getPlayerLevelCap(player);

                    if (proposedLevel > cap) {
                        event.setNewLevel(pokemon.getLevel()); // Mantém no nível atual

                        // Component.translatable em vez de Text.translatable
                        player.sendSystemMessage(
                                Component.translatable("message.colisao-cobblemon.level_cap_limit")
                                        .withStyle(ChatFormatting.RED) // withStyle em vez de formatted
                        );
                    }
                }
            }
        });

        // --- 3. BLOQUEIO DE SEND OUT ---
        CobblemonEvents.POKEMON_SENT_PRE.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            MinecraftServer server = ColisaoCobblemon.getServer();

            if (server == null) return;

            UUID ownerId = pokemon.getOwnerUUID();
            if (ownerId != null) {
                ServerPlayer player = server.getPlayerList().getPlayer(ownerId);

                // isCreative (igual)
                if (player != null && !player.isCreative()) {
                    int cap = LevelCapCalculator.getPlayerLevelCap(player);

                    if (pokemon.getLevel() > cap) {
                        event.cancel();

                        player.sendSystemMessage(
                                Component.translatable("message.colisao-cobblemon.level_cap_limit")
                                        .withStyle(ChatFormatting.RED)
                        );
                    }
                }
            }
        });
    }
}