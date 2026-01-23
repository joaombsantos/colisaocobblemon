package me.marcronte.colisaocobblemon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.marcronte.colisaocobblemon.config.*;
import me.marcronte.colisaocobblemon.features.routes.RouteCache;
import me.marcronte.colisaocobblemon.features.routes.RouteRegionData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ColisaoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("colisao")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("reload")

                                .executes(ColisaoCommand::reloadAll)

                                .then(Commands.literal("route")
                                        .executes(ctx -> reloadSpecific(ctx, "route"))
                                )

                                .then(Commands.literal("generation")
                                        .executes(ctx -> reloadSpecific(ctx, "generation"))
                                )

                                .then(Commands.literal("elitefour")
                                        .executes(ctx -> reloadSpecific(ctx, "elitefour"))
                                )

                                .then(Commands.literal("levelcap")
                                        .executes(ctx -> reloadSpecific(ctx, "levelcap"))
                                )
                                .then(Commands.literal("npcconfig")
                                        .executes(ctx -> reloadSpecific(ctx, "npcconfig"))
                                )
                        )

                        .then(Commands.literal("delete")
                                .executes(ColisaoCommand::deleteRouteAtPlayer)
                        )
        );
    }

    private static int reloadAll(CommandContext<CommandSourceStack> context) {
        try {
            ColisaoSettingsManager.reload(context.getSource().getServer());
            context.getSource().sendSuccess(() -> Component.literal("§a[Colisao] §lTODAS§r§a as configurações foram recarregadas!"), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cErro fatal ao recarregar tudo. Verifique o console."));
            e.printStackTrace();
            return 0;
        }
    }

    private static int reloadSpecific(CommandContext<CommandSourceStack> context, String type) {
        try {
            var server = context.getSource().getServer();

            switch (type) {
                case "route" -> {
                    RouteConfig.load(server);

                    if (server.overworld() != null) {
                        RouteCache.buildCache(server.overworld());
                    }

                    context.getSource().sendSuccess(() -> Component.literal("§b[Colisao] §fRotas e Cache recarregados!"), true);
                }
                case "generation" -> {
                    GenerationConfig.load(server);
                    context.getSource().sendSuccess(() -> Component.literal("§b[Colisao] §fGeração (gen_limit) recarregada!"), true);
                }
                case "elitefour" -> {
                    EliteFourConfig.load(server);
                    context.getSource().sendSuccess(() -> Component.literal("§b[Colisao] §fElite Four recarregada!"), true);
                }
                case "levelcap" -> {
                    LevelCapConfig.load(server);
                    context.getSource().sendSuccess(() -> Component.literal("§b[Colisao] §fLevel Cap recarregado!"), true);
                }
                case "npcconfig" -> {
                    NpcConfig.load(server);
                    context.getSource().sendSuccess(() -> Component.literal("§b[Colisao] §fNPCs recarregado!"), true);
                }
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cErro ao recarregar " + type + ": " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int deleteRouteAtPlayer(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            RouteRegionData data = RouteRegionData.get(player.serverLevel());

            String removedRouteName = data.removeRegionAt(player.blockPosition());

            if (removedRouteName != null) {
                if (player.serverLevel() != null) {
                    RouteCache.buildCache(player.serverLevel());
                }

                context.getSource().sendSuccess(() -> Component.literal("§c[Colisao] Região da rota '" + removedRouteName + "' deletada com sucesso!"), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("§eVocê não está dentro de nenhuma rota registrada."));
                return 0;
            }

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cErro ao deletar rota: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}