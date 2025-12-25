package me.marcronte.colisaocobblemon.features.badges;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

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
                ServerPlayer player = server.getPlayerList().getPlayer(ownerId);

                if (player != null) {
                    int cap = LevelCapCalculator.getPlayerLevelCap(player);

                    if (pokemon.getLevel() >= cap) {
                        event.setExperience(0);

                        player.displayClientMessage(
                                Component.translatable("message.colisao-cobblemon.reached_level_cap_limit")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
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

                        player.displayClientMessage(
                                Component.translatable("message.colisao-cobblemon.reached_level_cap_limit")
                                        .withStyle(ChatFormatting.RED),
                                true
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

                        player.displayClientMessage(
                                Component.translatable("message.colisao-cobblemon.level_cap_limit")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                }
            }
        });

        // --- 4. BLOQUEIO DE BATALHAS (Via Party Store) ---
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe(event -> {
            MinecraftServer server = ColisaoCobblemon.getServer();
            if (server == null) return;

            for (BattleActor actor : event.getBattle().getActors()) {

                // Pega o jogador pelo UUID do ator da batalha
                ServerPlayer player = server.getPlayerList().getPlayer(actor.getUuid());

                if (player != null) {
                    if (player.isCreative()) continue;

                    int cap = LevelCapCalculator.getPlayerLevelCap(player);
                    boolean hasOverleveledPokemon = false;

                    // AQUI ESTÁ A CORREÇÃO:
                    // Pegamos a Party (Time) direto da storage do Cobblemon
                    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);

                    // A Party é iterável e contém objetos 'Pokemon' puros (sem a complicação de BattlePokemon)
                    for (Pokemon pokemon : party) {
                        if (pokemon.getLevel() > cap) {
                            hasOverleveledPokemon = true;
                            break;
                        }
                    }

                    if (hasOverleveledPokemon) {
                        event.cancel();

                        player.displayClientMessage(
                                Component.translatable("message.colisao-cobblemon.battle_blocked")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                }
            }
        });

        //TODO Check badges on badge case
    }
}