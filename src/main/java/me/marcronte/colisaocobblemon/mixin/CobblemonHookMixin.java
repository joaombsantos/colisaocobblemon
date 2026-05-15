package me.marcronte.colisaocobblemon.mixin;

import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.interactive.PokerodItem;
import me.marcronte.colisaocobblemon.features.routes.RouteFishingHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokerodItem.class)
public class CobblemonHookMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

        if (level.isClientSide) return;

        if (player.fishing != null) {
            Entity bobber = player.fishing;

            boolean isNearNpc = level.getEntities(bobber, bobber.getBoundingBox().inflate(2.0))
                    .stream().anyMatch(e -> e.getTags().stream().anyMatch(tag -> tag.startsWith("colisao_npc:")));

            if (isNearNpc) {
                bobber.discard();
                cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
                return;
            }

            if (bobber instanceof PokeRodFishingBobberEntity cobbleBobber) {

                PokeRodAccessor accessor = (PokeRodAccessor) (Object) cobbleBobber;
                int countdown = accessor.getHookCountdown();

                if (countdown > 0) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        PokemonEntity spawnedPokemon = RouteFishingHandler.tryFish(cobbleBobber, serverPlayer);

                        if (spawnedPokemon != null) {
                            accessor.setHookCountdown(0);
                            spawnedPokemon.forceBattle(serverPlayer);
                            cobbleBobber.discard();
                            cir.setReturnValue(InteractionResultHolder.success(player.getItemInHand(hand)));
                        }
                    }
                }
            }
        }
    }
}