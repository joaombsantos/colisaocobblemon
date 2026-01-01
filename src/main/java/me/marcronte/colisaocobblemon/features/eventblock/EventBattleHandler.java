package me.marcronte.colisaocobblemon.features.eventblock;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeBlockData;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeNetwork;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Objects;



public class EventBattleHandler {

    public static void register() {
        // 1. CAPTURE
        CobblemonEvents.POKEMON_CAPTURED.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            Entity owner = event.getPlayer();
            if (owner instanceof ServerPlayer player) {
                checkAndCompleteEvent(pokemon, player);
            }
        });


        // 2. VICTORY
        CobblemonEvents.BATTLE_VICTORY.subscribe(event -> {
            List<BattleActor> winners = event.getWinners();
            List<BattleActor> losers = event.getLosers();

            ServerPlayer player = null;
            if (!event.getBattle().getPlayers().isEmpty()) {
                player = event.getBattle().getPlayers().getFirst();
            }

            for (BattleActor winner : winners) {
                if (winner.getType() == ActorType.PLAYER) {
                    if (player != null) {
                        for (BattleActor loser : losers) {
                            for (BattlePokemon battlePokemon : loser.getPokemonList()) {
                                checkAndCompleteEvent(battlePokemon.getOriginalPokemon(), player);
                            }
                        }
                    }
                }

                if (winner.getType() == ActorType.WILD) {
                    for (BattlePokemon battlePokemon : winner.getPokemonList()) {
                        forceDespawnIfEventEntity(battlePokemon);
                        if (player != null) {
                            restoreBlockVisuals(battlePokemon.getOriginalPokemon(), player);
                        }
                    }
                }
            }
        });



        // 3. BATTLE FLED
        CobblemonEvents.BATTLE_FLED.subscribe(event -> {
            ServerPlayer player = null;
            if (!event.getBattle().getPlayers().isEmpty()) {
                player = event.getBattle().getPlayers().getFirst();
            }

            for (BattleActor actor : event.getBattle().getActors()) {
                if (actor.getType() == ActorType.WILD) {
                    for (BattlePokemon battlePokemon : actor.getPokemonList()) {
                        forceDespawnIfEventEntity(battlePokemon);
                        if (player != null) {
                            restoreBlockVisuals(battlePokemon.getOriginalPokemon(), player);
                        }
                    }
                }
            }
        });

        // 4. DISCONNECT
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            ServerLevel level = (ServerLevel) player.level();

            AABB searchBox = player.getBoundingBox().inflate(32);
            List<PokemonEntity> nearbyPokemons = level.getEntitiesOfClass(PokemonEntity.class, searchBox,
                    e -> e.getPokemon().getPersistentData().contains("event_id"));

            for (PokemonEntity pokeEntity : nearbyPokemons) {
                CompoundTag data = pokeEntity.getPokemon().getPersistentData();
                if (data.hasUUID("event_owner") && data.getUUID("event_owner").equals(player.getUUID())) {
                    pokeEntity.discard();
                }
            }
        });

        // 5. PREVENT CAPTURE
        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe(event -> {
            EmptyPokeBallEntity pokeballEntity = event.getPokeBall();
            PokemonEntity pokemonEntity = event.getPokemon();

            boolean isUncatchable = pokemonEntity.getPokemon().getPersistentData().getBoolean("uncatchable");

            if (isUncatchable) {
                if (pokeballEntity.getOwner() instanceof ServerPlayer player) {
                    player.displayClientMessage(
                            Component.translatable("message.colisao-cobblemon.not_capturable").withStyle(ChatFormatting.RED),
                            true
                    );
                }
                // event.cancel() freezes the battle
            }
        });

        CobblemonEvents.POKE_BALL_CAPTURE_CALCULATED.subscribe(event -> {
            PokemonEntity pokemonEntity = event.getPokemonEntity();

            boolean isUncatchable = pokemonEntity.getPokemon().getPersistentData().getBoolean("uncatchable");

            if (isUncatchable) {
                CaptureContext failedContext = new CaptureContext(
                        0,      // numberOfShakes
                        false,  // isSuccessfulCapture = false
                        false   // isCriticalCapture = false
                );

                event.setCaptureResult(failedContext);
            }

        });

    }

    private static void forceDespawnIfEventEntity(BattlePokemon battlePokemon) {
        Pokemon pokemon = battlePokemon.getOriginalPokemon();
        if (pokemon.getPersistentData().contains("event_id")) {
            Entity entity = battlePokemon.getEntity();

            if (entity != null && entity.isAlive()) {
                if (entity.level() instanceof ServerLevel serverLevel) {
                    Objects.requireNonNull(entity.getServer()).execute(() -> {
                        if (entity.isAlive()) {
                            entity.discard();
                        }
                    });
                }
            }
        }
    }

    private static void restoreBlockVisuals(Pokemon pokemon, ServerPlayer player) {
        CompoundTag nbt = pokemon.getPersistentData();
        if (nbt.contains("event_pos")) {
            try {
                BlockPos pos = BlockPos.of(nbt.getLong("event_pos"));
                ServerLevel level = (ServerLevel) player.level();

                BlockState realState = level.getBlockState(pos);

                player.connection.send(new ClientboundBlockUpdatePacket(pos, realState));

                if (level.getBlockEntity(pos) instanceof PokemonBlockadeEntity be) {
                    Packet<ClientGamePacketListener> packet = be.getUpdatePacket();
                    if (packet != null) {
                        player.connection.send(packet);
                    }
                }

            } catch (Exception ignored) {}
        }
    }

    private static void checkAndCompleteEvent(Pokemon pokemon, ServerPlayer player) {
        String eventId = null;
        BlockPos pos = null;

        CompoundTag nbt = pokemon.getPersistentData();

        if (nbt.contains("event_id")) {
            eventId = nbt.getString("event_id");
        }
        if (nbt.contains("event_pos")) {
            try { pos = BlockPos.of(nbt.getLong("event_pos")); }
            catch (Exception ignored) {}
        }

        if (eventId != null && pos != null) {
            ServerLevel level = (ServerLevel) player.level();

            EventBlockData data = EventBlockData.get(level);
            data.completeEvent(player.getUUID(), eventId);

            FadeBlockData.get(level).unlock(pos, player.getUUID());
            FadeNetwork.sendSync(player, java.util.List.of(pos));

            player.connection.send(new ClientboundBlockUpdatePacket(pos, Blocks.AIR.defaultBlockState()));
        }
    }
}