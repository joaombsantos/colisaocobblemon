package me.marcronte.colisaocobblemon.features.eventblock;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class BlockadeInteractionHandler {

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide || hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(ItemStack.EMPTY);

            ServerPlayer serverPlayer = (ServerPlayer) player;
            ItemStack heldItem = player.getItemInHand(hand);

            double range = 10.0;
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 traceEnd = eyePos.add(lookVec.scale(range));

            BlockPos playerPos = player.blockPosition();
            int r = (int) range;

            PokemonBlockadeEntity targetBlockade = null;
            double closestDist = Double.MAX_VALUE;

            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos checkPos = playerPos.offset(x, y, z);

                        if (world.isLoaded(checkPos)) {
                            BlockEntity be = world.getBlockEntity(checkPos);

                            if (be instanceof PokemonBlockadeEntity blockade) {
                                AABB hitbox = blockade.getHitboxAABB();

                                Optional<Vec3> hit = hitbox.clip(eyePos, traceEnd);

                                if (hit.isPresent()) {
                                    double dist = hit.get().distanceToSqr(eyePos);
                                    if (dist < closestDist) {
                                        closestDist = dist;
                                        targetBlockade = blockade;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (targetBlockade != null) {
                ItemStack requiredKey = targetBlockade.getRequiredKeyItem();

                if (!requiredKey.isEmpty() && heldItem.is(requiredKey.getItem())) {
                    targetBlockade.activateBattle(serverPlayer);
                    return InteractionResultHolder.success(heldItem);
                }
            }

            return InteractionResultHolder.pass(ItemStack.EMPTY);
        });
    }
}