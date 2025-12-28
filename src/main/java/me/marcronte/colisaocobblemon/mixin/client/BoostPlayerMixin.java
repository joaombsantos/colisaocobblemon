package me.marcronte.colisaocobblemon.mixin.client;

import me.marcronte.colisaocobblemon.client.ColisaoCobblemonClient;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public class BoostPlayerMixin {

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V"))
    public void overwriteInputTick(Input instance, boolean slowDown, float f) {

        instance.tick(slowDown, f);

        if (ColisaoCobblemonClient.isPlayerBoosting) {
            instance.forwardImpulse = 0.0F;
            instance.leftImpulse = 0.0F;
            instance.jumping = false;
            instance.shiftKeyDown = false;

            instance.up = false;
            instance.down = false;
            instance.left = false;
            instance.right = false;
        }
    }
}