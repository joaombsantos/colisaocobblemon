package me.marcronte.colisaocobblemon.mixin;

import me.marcronte.colisaocobblemon.features.clans.Clan;
import me.marcronte.colisaocobblemon.features.clans.ClanSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerTagMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Component> cir) {
        Player player = (Player) (Object) this;

        if (player.level() instanceof ServerLevel serverLevel) {
            ClanSavedData data = ClanSavedData.get(serverLevel);
            Clan clan = data.getClanByPlayer(player.getUUID());

            if (clan != null) {
                Component original = cir.getReturnValue();
                Component tag = Component.literal("§8[" + clan.getTagColor() + clan.getTag() + "§8] §r").append(original);
                cir.setReturnValue(tag);
            }
        }
    }
}