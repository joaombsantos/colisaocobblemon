package me.marcronte.colisaocobblemon.features.clans;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;

public class ClanMissionHandler {

    public static void register() {

        // 1. MISSION: Defeat Pokémon
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, (BattleVictoryEvent event) -> {
            event.getWinners().forEach((BattleActor winnerActor) -> {
                if (winnerActor instanceof PlayerBattleActor playerActor) {
                    ServerPlayer player = playerActor.getEntity();
                    if (player != null) {
                        ClanSavedData data = ClanSavedData.get(player.serverLevel());
                        Clan clan = data.getClanByPlayer(player.getUUID());

                        if (clan != null) {
                            event.getLosers().forEach((BattleActor loserActor) -> {
                                if (!(loserActor instanceof PlayerBattleActor)) {
                                    loserActor.getPokemonList().forEach((BattlePokemon loserBattlePkmn) -> {
                                        Pokemon loserPkmn = loserBattlePkmn.getEffectedPokemon();
                                        boolean isClanType = isPokemonClanType(loserPkmn, clan);

                                        clan.progressDefeat(player.serverLevel(), isClanType);
                                        data.setDirty();
                                    });
                                }
                            });
                        }
                    }
                }
            });
            return Unit.INSTANCE;
        });

        // 2. MISSION: Capture Pokémon
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, (PokemonCapturedEvent event) -> {
            ServerPlayer player = event.getPlayer();
            ClanSavedData data = ClanSavedData.get(player.serverLevel());
            Clan clan = data.getClanByPlayer(player.getUUID());

            if (clan != null) {
                Pokemon caughtPkmn = event.getPokemon();
                boolean isClanType = isPokemonClanType(caughtPkmn, clan);

                clan.progressCatch(player.serverLevel(), isClanType);
                data.setDirty();
            }
            return Unit.INSTANCE;
        });
    }

    // 3. MISSION: Hatch Eggs
    public static void onEggHatched(ServerPlayer player) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan != null) {
            clan.progressHatch(player.serverLevel());
            data.setDirty();
        }
    }


    private static boolean isPokemonClanType(Pokemon pokemon, Clan clan) {
        String primary = pokemon.getPrimaryType().getName().toLowerCase();
        String secondary = pokemon.getSecondaryType() != null ? pokemon.getSecondaryType().getName().toLowerCase() : "";

        String ptPrimary = clan.getPrimaryType();
        String ptSecondary = clan.getSecondaryType();

        String engPrimary = ptPrimary != null ? getEnglishType(ptPrimary) : "none";
        String engSecondary = ptSecondary != null ? getEnglishType(ptSecondary) : "none";

        return primary.equals(engPrimary) || primary.equals(engSecondary) ||
                secondary.equals(engPrimary) || secondary.equals(engSecondary);
    }

    private static String getEnglishType(String ptType) {
        return switch (ptType) {
            case "Fogo" -> "fire"; case "Água" -> "water"; case "Grama" -> "grass";
            case "Elétrico" -> "electric"; case "Gelo" -> "ice"; case "Lutador" -> "fighting";
            case "Venenoso" -> "poison"; case "Terra" -> "ground"; case "Voador" -> "flying";
            case "Psíquico" -> "psychic"; case "Inseto" -> "bug"; case "Pedra" -> "rock";
            case "Fantasma" -> "ghost"; case "Dragão" -> "dragon"; case "Sombrio" -> "dark";
            case "Aço" -> "steel"; case "Fada" -> "fairy"; default -> "normal";
        };
    }
}