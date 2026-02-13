package me.marcronte.colisaocobblemon.features.badges;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.pokemon.interaction.ExperienceCandyUseEvent;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelCapEvents {

    private static final Map<String, Integer> CANDY_XP_VALUES = new HashMap<>();
    static {
        CANDY_XP_VALUES.put("cobblemon:exp_candy_xs", 100);
        CANDY_XP_VALUES.put("cobblemon:exp_candy_s", 800);
        CANDY_XP_VALUES.put("cobblemon:exp_candy_m", 3000);
        CANDY_XP_VALUES.put("cobblemon:exp_candy_l", 10000);
        CANDY_XP_VALUES.put("cobblemon:exp_candy_xl", 30000);
    }

    public static void register() {

        // --- 0. BLOCK EXP CANDY USAGE ---
        CobblemonEvents.EXPERIENCE_CANDY_USE_PRE.subscribe(event -> {
            ServerPlayer player = event.getPlayer();
            if (player == null || player.isCreative()) return;

            Pokemon pokemon = event.getPokemon();
            int cap = LevelCapCalculator.getPlayerLevelCap(player);

            if (pokemon.getLevel() >= cap) {
                cancelAndNotify(event, player);
                return;
            }

            Item candyItem = event.getItem();
            String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(candyItem).toString();

            if (itemId.contains("rare_candy")) {
                if (pokemon.getLevel() + 1 > cap) {
                    cancelAndNotify(event, player);
                }
                return;
            }

            int candyXp = CANDY_XP_VALUES.getOrDefault(itemId, 0);

            if (candyXp == 0) {
                if (itemId.contains("_xs")) candyXp = 100;
                else if (itemId.contains("_xl")) candyXp = 30000;
                else if (itemId.contains("_l")) candyXp = 10000;
                else if (itemId.contains("_m")) candyXp = 3000;
                else if (itemId.contains("_s")) candyXp = 800;
            }

            if (candyXp > 0) {
                int currentXp = pokemon.getExperience();
                int projectedXp = currentXp + candyXp;

                int xpForLevelAboveCap = pokemon.getExperienceGroup().getExperience(cap + 1);

                if (projectedXp >= xpForLevelAboveCap) {
                    cancelAndNotify(event, player);
                }
            }
        });

        // --- 1. BLOCK XP GAIN ---
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

        // --- 2. BLOCK AGAINST RARE CANDY (Level Up) ---
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

        // --- 3. SEND OUT BLOCK ---
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

        // --- 4. BATTLE BLOCK (Via Party Store) ---
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe(event -> {
            MinecraftServer server = ColisaoCobblemon.getServer();
            if (server == null) return;

            for (BattleActor actor : event.getBattle().getActors()) {

                ServerPlayer player = server.getPlayerList().getPlayer(actor.getUuid());

                if (player != null) {
                    if (player.isCreative()) continue;

                    int cap = LevelCapCalculator.getPlayerLevelCap(player);
                    boolean hasOverleveledPokemon = false;

                    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);

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
    }

    private static void cancelAndNotify(ExperienceCandyUseEvent.Pre event, ServerPlayer player) {
        event.cancel();
        player.displayClientMessage(
                Component.translatable("message.colisao-cobblemon.reached_level_cap_limit")
                        .withStyle(ChatFormatting.RED),
                true
        );

        player.inventoryMenu.sendAllDataToRemote();
    }
}