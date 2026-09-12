package me.marcronte.colisaocobblemon.network;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.features.blocks.machines.PokemonWorkBlockEntity;
import me.marcronte.colisaocobblemon.network.payloads.MachineActionPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class MachineNetwork {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(MachineActionPayload.ID, (payload, context) ->
                context.server().execute(() -> handleMachineAction(
                        context.player(),
                        payload.actionId(),
                        payload.pos(),
                        payload.pokemonId()
                )));
    }

    private static void handleMachineAction(ServerPlayer player, int actionId, BlockPos pos, UUID pokemonId) {
        if (player == null) return;

        BlockEntity be = player.serverLevel().getBlockEntity(pos);
        if (!(be instanceof PokemonWorkBlockEntity machine)) return;

        /*if (actionId == 0) {
            if (machine.getPokemonData() != null) {
                Pokemon p = BreedingNetwork.reconstructPokemon(machine.getPokemonData(), player.registryAccess());

                if (p != null) {
                    BreedingNetwork.givePokemonOrToPC(player, p);
                }

                machine.clearPokemon();
                machine.setChanged();
                player.sendSystemMessage(Component.literal("§aPokémon removido da máquina com sucesso!"));
            }
        }*/

        if (actionId == 0) {
            if (machine.getPokemonData() != null) {
                Pokemon p = BreedingNetwork.reconstructPokemon(machine.getPokemonData(), player.registryAccess());

                if (p != null) {
                    int targetFriendship = machine.getMachineFriendship();
                    com.cobblemon.mod.common.api.pokemon.PokemonProperties.Companion.parse("friendship=" + targetFriendship).apply(p);

                    BreedingNetwork.givePokemonOrToPC(player, p);
                }

                machine.clearPokemon();
                machine.setChanged();
                player.sendSystemMessage(Component.literal("§aPokémon removido da máquina com sucesso!"));
            }
        }

        else if (actionId == 1 && pokemonId != null) {
            if (machine.getPokemonData() != null) return;

            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            Pokemon selected = null;

            for (Pokemon p : party) {
                if (p != null && p.getUuid().equals(pokemonId)) {
                    selected = p;
                    break;
                }
            }

            if (selected == null) {
                player.sendSystemMessage(Component.literal("§cPokémon não encontrado na equipe!").withStyle(ChatFormatting.RED));
                return;
            }

            if (machine.isValidPokemon(selected)) {
                party.remove(selected);

                machine.insertPokemon(player, selected);
                machine.setChanged();

                player.sendSystemMessage(Component.literal("§a" + selected.getSpecies().getName() + " começou a trabalhar!"));
            } else {
                player.sendSystemMessage(Component.literal("§cEste Pokémon não possui os tipos necessários para esta máquina!").withStyle(ChatFormatting.RED));
            }
        }
    }
}