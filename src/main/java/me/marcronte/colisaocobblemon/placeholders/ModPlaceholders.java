package me.marcronte.colisaocobblemon.placeholders;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.features.clans.Clan;
import me.marcronte.colisaocobblemon.features.clans.ClanSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ModPlaceholders {

    public static void register() {
        Placeholders.register(ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "clan_tag"), (context, argument) -> {

            if (context.hasPlayer() && context.player() instanceof ServerPlayer player) {

                ClanSavedData data = ClanSavedData.get(player.serverLevel());
                Clan clan = data.getClanByPlayer(player.getUUID());

                if (clan != null) {
                    Component tag = Component.literal("§7[" + clan.getTagColor() + clan.getTag() + "§7] ");
                    return PlaceholderResult.value(tag);
                }
            }

            return PlaceholderResult.value("");
        });
    }
}