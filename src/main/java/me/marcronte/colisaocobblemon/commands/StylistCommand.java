package me.marcronte.colisaocobblemon.commands;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import me.marcronte.colisaocobblemon.config.ProfessionsPerksConfig;
import me.marcronte.colisaocobblemon.data.ProfessionPlayerData;
import me.marcronte.colisaocobblemon.network.payloads.StylistPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StylistCommand {

    private static final List<String> RANK_ORDER = List.of("rank_e", "rank_d", "rank_c", "rank_b", "rank_a");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("roupas")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ProfessionPlayerData data = ProfessionPlayerData.get(player.serverLevel());
                    ProfessionPlayerData.PlayerProf prof = data.getPlayer(player.getUUID());

                    if (!"estilista".equalsIgnoreCase(prof.profession)) {
                        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.must_be_stylist"));
                        return 0;
                    }

                    if (ProfessionsPerksConfig.INSTANCE == null || ProfessionsPerksConfig.INSTANCE.perks == null) {
                        //player.sendSystemMessage(Component.literal("§cError: Perks not loaded."));
                        return 0;
                    }

                    Map<String, Object> estilistaData = ProfessionsPerksConfig.INSTANCE.perks.get("estilista");
                    if (estilistaData == null) {
                        //player.sendSystemMessage(Component.literal("§cErro: Stylist configs not found."));
                        return 0;
                    }

                    Set<String> categoriasPermitidas = new HashSet<>();
                    String currentRank = prof.rank.toLowerCase();

                    for (String rankLevel : RANK_ORDER) {
                        Object rankObj = estilistaData.get(rankLevel);

                        if (rankObj instanceof Map<?, ?> rankMap) {
                            Object categoriesObj = rankMap.get("categories");

                            if (categoriesObj instanceof Map<?, ?> categoriesMap) {
                                for (Object key : categoriesMap.keySet()) {
                                    categoriasPermitidas.add(key.toString());
                                }
                            }
                        }

                        if (rankLevel.equals(currentRank)) {
                            break;
                        }
                    }

                    if (categoriasPermitidas.isEmpty()) {
                        //player.sendSystemMessage(Component.literal("§cYou still don't have any craft."));
                        return 0;
                    }

                    Gson gson = new Gson();
                    String categoriesJson = gson.toJson(new ArrayList<>(categoriasPermitidas));

                    player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.opening_stylist"));
                    ServerPlayNetworking.send(player, new StylistPayloads.OpenMenuPayload(categoriesJson));

                    return 1;
                })
        );
    }
}