package me.marcronte.colisaocobblemon.features.hms;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SurfHandler {

    private static final Map<UUID, Vec3d> LAST_STRICT_SAFE_POS = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(SurfHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {

            if (player.isCreative() || player.isSpectator()) continue;

            UUID playerId = player.getUuid();
            boolean isInWater = player.isTouchingWater();

            // --- JOGADOR NA ÁGUA ---
            if (isInWater) {
                if (!hasSurfItem(player)) {
                    Vec3d respawnPos;

                    if (LAST_STRICT_SAFE_POS.containsKey(playerId)) {
                        respawnPos = LAST_STRICT_SAFE_POS.get(playerId);
                    } else {
                        // Fallback de emergência (Terra mais próxima)
                        respawnPos = findNearestLand(world, player.getBlockPos());
                        if (respawnPos == null) respawnPos = player.getPos().add(0, 1, 0);
                    }

                    // 1. ZERA A VELOCIDADE (Crucial para parar o "andar")
                    player.setVelocity(Vec3d.ZERO);
                    player.velocityModified = true;

                    // 2. TELEPORTA
                    // Adicionei +0.5 no Y. Isso faz o jogador cair um pouquinho ao chegar.
                    // Esse impacto no chão ajuda a frear o movimento do cliente.
                    player.requestTeleport(respawnPos.x, respawnPos.y + 0.5, respawnPos.z);

                    player.sendMessage(
                            Text.translatable("message.colisao-cobblemon.need_surf").formatted(Formatting.RED),
                            true
                    );
                }
            }
            // --- JOGADOR NA TERRA ---
            else if (player.isOnGround()) {
                BlockPos currentPos = player.getBlockPos();

                // Só salva se for ESTRITAMENTE seguro (longe de barrancos)
                if (isStrictlySafe(world, currentPos)) {
                    LAST_STRICT_SAFE_POS.put(playerId, new Vec3d(
                            currentPos.getX() + 0.5,
                            player.getY(),
                            currentPos.getZ() + 0.5
                    ));
                }
            }
        }
    }

    // --- O CORAÇÃO DA CORREÇÃO ---
    private static boolean isStrictlySafe(ServerWorld world, BlockPos pos) {
        // 1. O próprio bloco é água? (Pés molhados)
        if (isWater(world, pos)) return false;

        // 2. O bloco ABAIXO é água? (Ponte falsa/Gelo fino)
        if (isWater(world, pos.down())) return false;

        // 3. Verificação de VIZINHOS (Detector de Barranco)
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos neighbor = pos.offset(dir);

            // Tem água no mesmo nível? (Entrando na praia)
            if (isWater(world, neighbor)) return false;

            // Tem água no vizinho DE BAIXO? (Beirada de rio/barranco)
            // É AQUI que o código falhava antes!
            if (isWater(world, neighbor.down())) return false;
        }
        return true;
    }

    private static boolean isWater(ServerWorld world, BlockPos pos) {
        return world.getFluidState(pos).isIn(FluidTags.WATER);
    }

    private static Vec3d findNearestLand(ServerWorld world, BlockPos center) {
        BlockPos bestPos = null;
        double bestDist = Double.MAX_VALUE;

        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = center.add(x, y, z);

                    if (world.getBlockState(checkPos).isSolidBlock(world, checkPos) &&
                            !isWater(world, checkPos.up()) &&
                            !isWater(world, checkPos) && // Garante que não é água
                            world.getBlockState(checkPos.up()).isAir()) {

                        double dist = center.getSquaredDistance(checkPos);
                        if (dist < bestDist) {
                            bestDist = dist;
                            bestPos = checkPos;
                        }
                    }
                }
            }
        }
        if (bestPos != null) {
            return new Vec3d(bestPos.getX() + 0.5, bestPos.getY() + 1, bestPos.getZ() + 0.5);
        }
        return null;
    }

    private static boolean hasSurfItem(ServerPlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        return inv.contains(HmManager.SURF.getDefaultStack());
    }
}