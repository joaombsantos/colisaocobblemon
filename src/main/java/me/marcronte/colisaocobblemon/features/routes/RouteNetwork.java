package me.marcronte.colisaocobblemon.features.routes;

import me.marcronte.colisaocobblemon.network.payloads.OpenRouteScreenPayload;
import me.marcronte.colisaocobblemon.network.payloads.SaveRoutePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class RouteNetwork {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenRouteScreenPayload.ID, OpenRouteScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SaveRoutePayload.ID, SaveRoutePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SaveRoutePayload.ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayer player = context.player();

            if (!player.hasPermissions(2)) return;

            RouteToolItem.Selection sel = RouteToolItem.SELECTIONS.get(player.getUUID());

            if (sel != null && sel.pos1 != null && sel.pos2 != null) {
                RouteRegionData data = RouteRegionData.get(player.serverLevel());
                data.addRegion(payload.routeName(), sel.pos1, sel.pos2);

                player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.route_created",payload.routeName()));
                RouteToolItem.SELECTIONS.remove(player.getUUID());
            } else {
                player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.route_created_error"));
            }
        }));
    }

    public static void openRouteScreen(ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, OpenRouteScreenPayload.ID)) {
            ServerPlayNetworking.send(player, new OpenRouteScreenPayload());
        }
    }
}