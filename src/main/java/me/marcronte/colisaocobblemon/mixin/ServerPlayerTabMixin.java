package me.marcronte.colisaocobblemon.mixin;

import me.marcronte.colisaocobblemon.features.clans.Clan;
import me.marcronte.colisaocobblemon.features.clans.ClanSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerTabMixin {

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan != null) {
            Component original = cir.getReturnValue() != null ? cir.getReturnValue() : Component.literal(player.getGameProfile().getName());
            Component tag = Component.literal("§8[" + clan.getTagColor() + clan.getTag() + "§8] §r").append(original);
            cir.setReturnValue(tag);
        }
    }
}