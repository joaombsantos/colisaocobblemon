package me.marcronte.colisaocobblemon.features.routes;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RouteTracker {

    private static final Map<UUID, String> LAST_KNOWN_ROUTE = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 10 == 0) {
                server.getPlayerList().getPlayers().forEach(RouteTracker::checkPlayer);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST_KNOWN_ROUTE.remove(handler.player.getUUID()));
    }

    private static void checkPlayer(ServerPlayer player) {
        if (player.level().isClientSide) return;

        RouteRegionData data = RouteRegionData.get(player.serverLevel());
        String currentRoute = data.getRouteAt(player.blockPosition());
        String lastRoute = LAST_KNOWN_ROUTE.get(player.getUUID());

        if (currentRoute != null && !currentRoute.equals(lastRoute)) {
            player.displayClientMessage(
                    Component.translatable("message.colisao-cobblemon.route_entered", currentRoute).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );
            LAST_KNOWN_ROUTE.put(player.getUUID(), currentRoute);
        }
        else if (currentRoute == null && lastRoute != null) {
            player.displayClientMessage(
                    Component.translatable("message.colisao-cobblemon.route_leaving", lastRoute).withStyle(ChatFormatting.GRAY),
                    true
            );
            LAST_KNOWN_ROUTE.put(player.getUUID(), null);
        }
        else if (currentRoute != null && lastRoute != null && !currentRoute.equals(lastRoute)) {
            player.displayClientMessage(
                    Component.translatable("message.colisao-cobblemon.route_entered", currentRoute).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );
            LAST_KNOWN_ROUTE.put(player.getUUID(), currentRoute);
        }
    }
}