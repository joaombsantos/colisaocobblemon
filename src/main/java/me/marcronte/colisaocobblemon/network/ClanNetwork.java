package me.marcronte.colisaocobblemon.network;

import me.marcronte.colisaocobblemon.features.clans.Clan;
import me.marcronte.colisaocobblemon.features.clans.ClanSavedData;
import me.marcronte.colisaocobblemon.network.payloads.ClanPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ClanNetwork {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ClanPayloads.CreateClanPayload.ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayer player = context.player();

            boolean isValidName = payload.name().matches("^[a-zA-Z0-9_]+$");
            boolean isValidTag = payload.tag().matches("^[a-zA-Z0-9_]{3}$");
            boolean isValidTypes = !payload.type1().equals(payload.type2());

            if (!isValidName || !isValidTag || !isValidTypes) {
                player.sendSystemMessage(Component.literal("§cFalha na criação: Dados do clan inválidos ou contêm caracteres não permitidos."));
                return;
            }

            ClanSavedData data = ClanSavedData.get(player.serverLevel());

            if (data.getClanByPlayer(player.getUUID()) != null) {
                player.sendSystemMessage(Component.literal("§cVocê já possui um clan!"));
                return;
            }

            if (data.getClanByName(payload.name()) != null) {
                player.sendSystemMessage(Component.literal("§cJá existe um clan com este nome!"));
                return;
            }

            Clan newClan = new Clan(payload.name(), payload.tag(), payload.colorStr(), payload.type1(), payload.type2(), player.getUUID(), player.getScoreboardName());
            data.registerClan(newClan);
            ClanSavedData.refreshTabList(player);
            player.sendSystemMessage(Component.literal("§aClan §e" + payload.name() + " §acriado com sucesso!"));
        }));
    }
}