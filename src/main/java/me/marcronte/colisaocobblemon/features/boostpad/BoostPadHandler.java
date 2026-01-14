package me.marcronte.colisaocobblemon.features.boostpad;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.network.BoostNetwork;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoostPadHandler {

    private static final Map<UUID, BoostData> BOOSTING_PLAYERS = new HashMap<>();

    // Settings
    private static final int MAX_DISTANCE = 20;
    private static final double SPEED = 0.65;

    private static final ResourceLocation NO_JUMP_ID = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "no_jump_boost_pad");

    private static final AttributeModifier NO_JUMP_MODIFIER = new AttributeModifier(
            NO_JUMP_ID,
            -1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    private static class BoostData {
        Direction direction;
        int blocksTraveled;
        boolean justStarted;

        public BoostData(Direction direction) {
            this.direction = direction;
            this.blocksTraveled = 0;
            this.justStarted = true;
        }
    }

    public static void startBoosting(Player player, Direction direction) {
        if (BOOSTING_PLAYERS.containsKey(player.getUUID())) {
            BoostData data = BOOSTING_PLAYERS.get(player.getUUID());
            if (data.direction == direction) return;
        }

        alignPlayerCenter(player);
        player.setDeltaMovement(0, player.getDeltaMovement().y, 0);

        BOOSTING_PLAYERS.put(player.getUUID(), new BoostData(direction));

        applyZeroJumpAttribute(player);

        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new BoostNetwork.BoostStatePayload(true));
        }
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (Player player : server.getPlayerList().getPlayers()) {
                if (BOOSTING_PLAYERS.containsKey(player.getUUID())) {
                    handlePlayerTick(player);
                } else {
                    checkForTriggers(player);
                }
            }
        });
    }

    private static void checkForTriggers(Player player) {
        Level level = player.level();
        BlockPos currentPos = player.blockPosition();
        BlockPos belowPos = currentPos.below();

        BlockState stateCurrent = level.getBlockState(currentPos);
        BlockState stateBelow = level.getBlockState(belowPos);

        if (stateCurrent.getBlock() instanceof BoostPadBlock) {
            if (isNearCenter(player, currentPos)) {
                startBoosting(player, stateCurrent.getValue(BoostPadBlock.FACING));
            }
            return;
        }

        if (stateBelow.getBlock() instanceof BoostPadBlock) {
            if (isNearCenter(player, belowPos)) {
                startBoosting(player, stateBelow.getValue(BoostPadBlock.FACING));
            }
        }
    }

    private static boolean isNearCenter(Player player, BlockPos pos) {
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        return player.distanceToSqr(centerX, player.getY(), centerZ) < 0.25; // Tolerance 0.5
    }

    private static void handlePlayerTick(Player player) {
        UUID uuid = player.getUUID();
        BoostData data = BOOSTING_PLAYERS.get(uuid);

        Level level = player.level();
        BlockPos posBelow = player.blockPosition();
        BlockState stateBelow = level.getBlockState(posBelow);

        if (stateBelow.isAir() || !(stateBelow.getBlock() instanceof BoostPadBlock)) {
            BlockPos checkBelow = posBelow.below();
            if (level.getBlockState(checkBelow).getBlock() instanceof BoostPadBlock) {
                posBelow = checkBelow;
                stateBelow = level.getBlockState(posBelow);
            }
        }

        if (data.blocksTraveled >= MAX_DISTANCE) {
            stopBoosting(player);
            return;
        }

        if (stateBelow.is(Blocks.YELLOW_CARPET)) {
            stopBoosting(player);
            return;
        }

        if (!data.justStarted && stateBelow.getBlock() instanceof BoostPadBlock) {
            Direction newDir = stateBelow.getValue(BoostPadBlock.FACING);
            if (newDir != data.direction) {
                data.direction = newDir;
                alignPlayerCenter(player);
                player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
            }
        }

        BlockPos posAhead = player.blockPosition().relative(data.direction);
        if (level.getBlockState(posAhead).isSolidRender(level, posAhead)) {
            stopBoosting(player);
            return;
        }

        double velY = Math.min(0, player.getDeltaMovement().y);
        Vec3 motion = new Vec3(data.direction.getStepX() * SPEED, velY, data.direction.getStepZ() * SPEED);
        player.setDeltaMovement(motion);
        player.hurtMarked = true;

        if (player.tickCount % 2 == 0) {
            data.blocksTraveled++;
        }

        data.justStarted = false;
    }

    private static void stopBoosting(Player player) {
        BOOSTING_PLAYERS.remove(player.getUUID());
        player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
        player.hurtMarked = true;

        removeZeroJumpAttribute(player);
        alignPlayerCenter(player);

        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new BoostNetwork.BoostStatePayload(false));
        }
    }

    private static void alignPlayerCenter(Player player) {
        player.moveTo(player.blockPosition().getX() + 0.5, player.getY(), player.blockPosition().getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    private static void applyZeroJumpAttribute(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (attribute != null && !attribute.hasModifier(NO_JUMP_ID)) {
            attribute.addTransientModifier(NO_JUMP_MODIFIER);
        }
    }

    private static void removeZeroJumpAttribute(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (attribute != null) {
            attribute.removeModifier(NO_JUMP_ID);
        }
    }
}