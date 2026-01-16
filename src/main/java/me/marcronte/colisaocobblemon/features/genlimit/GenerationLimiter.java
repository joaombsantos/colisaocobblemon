package me.marcronte.colisaocobblemon.features.genlimit;

import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.GenerationConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class GenerationLimiter {

    public static void register() {

        CobblemonEvents.POKEMON_SENT_PRE.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            Entity ownerEntity = pokemon.getOwnerPlayer();

            if (ownerEntity instanceof ServerPlayer player) {
                if (isAdmin(player)) return;

                int gen = getGenFromDex(pokemon.getSpecies().getNationalPokedexNumber());
                int limit = GenerationConfig.get().max_generation;

                if (gen > limit) {
                    event.cancel();
                    sendError(player, pokemon.getSpecies().getName(), gen, limit);
                }
            }
        });

        CobblemonEvents.BATTLE_STARTED_PRE.subscribe(event -> {
            MinecraftServer server = ColisaoCobblemon.getServer();
            if (server == null) return;

            for (BattleActor actor : event.getBattle().getActors()) {
                if (actor.getType() == ActorType.PLAYER) {
                    ServerPlayer player = server.getPlayerList().getPlayer(actor.getUuid());

                    if (player != null) {
                        if (isAdmin(player)) continue;

                        int limit = GenerationConfig.get().max_generation;

                        for (BattlePokemon battlePokemon : actor.getPokemonList()) {

                            Pokemon original = battlePokemon.getOriginalPokemon();

                            if (original == null) {
                                original = battlePokemon.getEffectedPokemon();
                            }

                            if (original != null) {
                                int gen = getGenFromDex(original.getSpecies().getNationalPokedexNumber());

                                if (gen > limit) {
                                    event.cancel();
                                    sendError(player, original.getSpecies().getName(), gen, limit);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        });

        CobblemonEvents.EVOLUTION_ACCEPTED.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            Entity ownerEntity = pokemon.getOwnerPlayer();

            if (ownerEntity instanceof ServerPlayer player) {
                if (isAdmin(player)) return;

                PokemonProperties resultProps = event.getEvolution().getResult();
                String speciesStr = resultProps.getSpecies();

                if (speciesStr == null) return;

                if (!speciesStr.contains(":")) {
                    speciesStr = "cobblemon:" + speciesStr;
                }

                ResourceLocation resultId = ResourceLocation.parse(speciesStr);
                Species targetSpecies = PokemonSpecies.getByIdentifier(resultId);

                if (targetSpecies == null) return;

                int targetDex = targetSpecies.getNationalPokedexNumber();
                int targetGen = getGenFromDex(targetDex);
                int limit = GenerationConfig.get().max_generation;


                if (targetGen > limit) {
                    event.cancel();
                    player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.genlimit_evolution_cancelled"));
                }
            }
        });

    }

    private static boolean isAdmin(ServerPlayer player) {
        return player.hasPermissions(2) || player.isCreative();
    }

    private static int getGenFromDex(int dex) {
        if (dex <= 151) return 1;
        if (dex <= 251) return 2;
        if (dex <= 386) return 3;
        if (dex <= 493) return 4;
        if (dex <= 649) return 5;
        if (dex <= 721) return 6;
        if (dex <= 809) return 7;
        if (dex <= 905) return 8;
        return 9;
    }

    private static void sendError(ServerPlayer player, String pokeName, int gen, int limit) {
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.genlimit_blocked", pokeName));
    }
}