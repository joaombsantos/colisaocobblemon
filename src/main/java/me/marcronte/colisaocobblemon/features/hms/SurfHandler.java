package me.marcronte.colisaocobblemon.features.hms;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting; // Mudou de Formatting
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component; // Mudou de Text
import net.minecraft.server.level.ServerLevel; // Mudou de ServerWorld
import net.minecraft.server.level.ServerPlayer; // Mudou de ServerPlayerEntity
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Inventory; // Mudou de PlayerInventory
import net.minecraft.world.phys.Vec3; // Mudou de Vec3d

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SurfHandler {

    private static final Map<UUID, Vec3> LAST_STRICT_SAFE_POS = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(SurfHandler::onWorldTick);
    }

    private static void onWorldTick(ServerLevel world) {
        for (ServerPlayer player : world.players()) { // getPlayers -> players

            if (player.isCreative() || player.isSpectator()) continue;

            UUID playerId = player.getUUID();
            boolean isInWater = player.isInWater(); // isTouchingWater -> isInWater

            // --- JOGADOR NA ÁGUA ---
            if (isInWater) {
                if (!hasSurfItem(player)) {
                    Vec3 respawnPos;

                    if (LAST_STRICT_SAFE_POS.containsKey(playerId)) {
                        respawnPos = LAST_STRICT_SAFE_POS.get(playerId);
                    } else {
                        // Fallback de emergência (Terra mais próxima)
                        respawnPos = findNearestLand(world, player.blockPosition()); // getBlockPos -> blockPosition
                        if (respawnPos == null) respawnPos = player.position().add(0, 1, 0); // getPos -> position
                    }

                    // 1. ZERA A VELOCIDADE
                    player.setDeltaMovement(Vec3.ZERO); // setVelocity -> setDeltaMovement
                    player.hurtMarked = true; // velocityModified -> hurtMarked (aproximação)

                    // 2. TELEPORTA
                    player.teleportTo(respawnPos.x, respawnPos.y + 0.5, respawnPos.z); // requestTeleport -> teleportTo

                    player.displayClientMessage(
                            Component.translatable("message.colisao-cobblemon.need_surf")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
            }
            // --- JOGADOR NA TERRA ---
            else if (player.onGround()) {
                BlockPos currentPos = player.blockPosition();

                // Só salva se for ESTRITAMENTE seguro (longe de barrancos)
                if (isStrictlySafe(world, currentPos)) {
                    LAST_STRICT_SAFE_POS.put(playerId, new Vec3(
                            currentPos.getX() + 0.5,
                            player.getY(),
                            currentPos.getZ() + 0.5
                    ));
                }
            }
        }
    }

    // --- O CORAÇÃO DA CORREÇÃO ---
    private static boolean isStrictlySafe(ServerLevel world, BlockPos pos) {
        // 1. O próprio bloco é água? (Pés molhados)
        if (isWater(world, pos)) return false;

        // 2. O bloco ABAIXO é água? (Ponte falsa/Gelo fino)
        if (isWater(world, pos.below())) return false;

        // 3. Verificação de VIZINHOS (Detector de Barranco)
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(dir);

            // Tem água no mesmo nível? (Entrando na praia)
            if (isWater(world, neighbor)) return false;

            // Tem água no vizinho DE BAIXO? (Beirada de rio/barranco)
            if (isWater(world, neighbor.below())) return false;
        }
        return true;
    }

    private static boolean isWater(ServerLevel world, BlockPos pos) {
        return world.getFluidState(pos).is(FluidTags.WATER);
    }

    private static Vec3 findNearestLand(ServerLevel world, BlockPos center) {
        BlockPos bestPos = null;
        double bestDist = Double.MAX_VALUE;

        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = center.offset(x, y, z);

                    if (world.getBlockState(checkPos).isSolidRender(world, checkPos) &&
                            !isWater(world, checkPos.above()) &&
                            !isWater(world, checkPos) &&
                            world.getBlockState(checkPos.above()).isAir()) {

                        double dist = center.distSqr(checkPos);
                        if (dist < bestDist) {
                            bestDist = dist;
                            bestPos = checkPos;
                        }
                    }
                }
            }
        }
        if (bestPos != null) {
            return new Vec3(bestPos.getX() + 0.5, bestPos.getY() + 1, bestPos.getZ() + 0.5);
        }
        return null;
    }

    private static boolean hasSurfItem(ServerPlayer player) {
        Inventory inv = player.getInventory();
        return inv.contains(HmManager.SURF.getDefaultInstance());
    }
}