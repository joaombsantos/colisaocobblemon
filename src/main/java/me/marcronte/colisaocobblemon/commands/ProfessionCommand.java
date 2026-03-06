package me.marcronte.colisaocobblemon.commands;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.marcronte.colisaocobblemon.config.ProfessionsCraftsConfig;
import me.marcronte.colisaocobblemon.data.ProfessionPlayerData;
import me.marcronte.colisaocobblemon.network.payloads.ProfessionCraftPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ProfessionCommand {
    private static final List<String> PROFESSIONS = List.of("aventureiro", "engenheiro", "estilista", "professor");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("profissao")

                // ==========================================
                // COMAND: /profissao escolher <nick> <profession>
                // Restrict to OP Level 2 (Admins, Console or NPCs)
                // ==========================================
                .then(Commands.literal("escolher")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("alvo", EntityArgument.player())
                                .then(Commands.argument("nome_profissao", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "alvo");

                                            ProfessionPlayerData data = ProfessionPlayerData.get(target.serverLevel());
                                            ProfessionPlayerData.PlayerProf prof = data.getPlayer(target.getUUID());

                                            if (!"nenhuma".equalsIgnoreCase(prof.profession)) {
                                                context.getSource().sendFailure(Component.translatable("message.colisao-cobblemon.player_already_has_profession"));
                                                return 0;
                                            }

                                            String profName = StringArgumentType.getString(context, "nome_profissao").toLowerCase();

                                            if (!PROFESSIONS.contains(profName)) {
                                                context.getSource().sendFailure(Component.translatable("message.colisao-cobblemon.invalid_profession"));
                                                return 0;
                                            }

                                            prof.profession = profName;
                                            prof.rank = "rank_e";
                                            prof.progress = 0;
                                            prof.cooldowns.clear();
                                            data.setDirty();

                                            target.sendSystemMessage(Component.translatable("message.colisao-cobblemon.player_chosed_profession", profName));

                                            return 1;
                                        })
                                )
                        )
                )

                // ==========================================
                // COMAND: /profissao abandonar <nick>
                // Restrict to OP Level 2 (Admins, Console or NPCs)
                // ==========================================
                .then(Commands.literal("abandonar")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("alvo", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "alvo");
                                    ProfessionPlayerData data = ProfessionPlayerData.get(target.serverLevel());

                                    data.resetPlayer(target.getUUID());

                                    target.sendSystemMessage(Component.translatable("message.colisao-cobblemon.player_abandoned_profession"));

                                    return 1;
                                })
                        )
                )

                // ==========================================
                // COMAND: /profissao
                // ==========================================
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ProfessionPlayerData data = ProfessionPlayerData.get(player.serverLevel());
                    ProfessionPlayerData.PlayerProf prof = data.getPlayer(player.getUUID());

                    if (prof.profession.equals("nenhuma")) {
                        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.player_dont_have_profession"));
                        return 0;
                    }

                    if (ProfessionsCraftsConfig.INSTANCE == null) {
                        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.player_error_loading_profession"));
                        return 0;
                    }

                    Gson gson = new Gson();
                    String recipesJson = gson.toJson(ProfessionsCraftsConfig.INSTANCE.crafts.get(prof.profession));

                    ServerPlayNetworking.send(player, new ProfessionCraftPayloads.OpenMenuPayload(
                            prof.profession, prof.rank, prof.progress, recipesJson
                    ));

                    return 1;
                })
        );
    }
}