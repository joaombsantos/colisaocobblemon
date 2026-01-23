package me.marcronte.colisaocobblemon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity")
public interface PokeRodAccessor {

    @Accessor(value = "hookCountdown", remap = false)
    int getHookCountdown();

    @Accessor(value = "hookCountdown", remap = false)
    void setHookCountdown(int value);
}