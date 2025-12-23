package me.marcronte.colisaocobblemon.features.badges;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class LevelCapEvents {

    public static void register() {

        // --- 1. BLOQUEAR GANHO DE XP (Batalhas) ---
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            MinecraftServer server = ColisaoCobblemon.getServer();

            if (server == null) return;

            UUID ownerId = pokemon.getOwnerUUID();
            if (ownerId != null) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(ownerId);

                if (player != null) {
                    int cap = LevelCapCalculator.getPlayerLevelCap(player);

                    if (pokemon.getLevel() >= cap) {
                        event.setExperience(0);
                    }
                }
            }
        });

        // --- 2. TRAVA CONTRA RARE CANDY (Modificando o Resultado) ---
        CobblemonEvents.LEVEL_UP_EVENT.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            MinecraftServer server = ColisaoCobblemon.getServer();

            if (server == null) return;

            // O nível que ele ESTÁ tentando alcançar
            int proposedLevel = event.getNewLevel();

            UUID ownerId = pokemon.getOwnerUUID();
            if (ownerId != null) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(ownerId);

                if (player != null) {
                    int cap = LevelCapCalculator.getPlayerLevelCap(player);

                    // Se o nível proposto for maior que o permitido
                    if (proposedLevel > cap) {
                        // AQUI ESTÁ O TRUQUE:
                        // Forçamos o "Novo Nível" a ser igual ao "Nível Atual"
                        event.setNewLevel(pokemon.getLevel());

                        // Avisa o jogador que ele gastou o item à toa
                        player.sendMessage(
                                Text.translatable("message.colisao-cobblemon.level_cap_limit").formatted(Formatting.RED),
                                true
                        );
                    }
                }
            }
        });

        // --- 3. BLOQUEIO DE SEND OUT (Jogar a Pokébola) ---
        CobblemonEvents.POKEMON_SENT_PRE.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            MinecraftServer server = ColisaoCobblemon.getServer();

            if (server == null) return;

            UUID ownerId = pokemon.getOwnerUUID();
            if (ownerId != null) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(ownerId);

                if (player != null && !player.isCreative()) {
                    int cap = LevelCapCalculator.getPlayerLevelCap(player);

                    if (pokemon.getLevel() > cap) {
                        event.cancel();

                        player.sendMessage(
                                Text.translatable("message.colisao-cobblemon.level_cap_limit").formatted(Formatting.RED),
                                true
                        );
                    }
                }
            }
        });
    }
}