package me.marcronte.colisaocobblemon.features.teleportblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeleportBlockEntity extends BlockEntity {

    private BlockPos targetPos;
    private float targetYaw;
    private float targetPitch;
    private String targetDimension;

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    public TeleportBlockEntity(BlockPos pos, BlockState blockState) {
        super(TeleportRegistry.TELEPORT_BLOCK_BE, pos, blockState);
    }

    public void setDestination(BlockPos pos, float yaw, float pitch, Level level) {
        this.targetPos = pos;
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.targetDimension = level.dimension().location().toString();
        this.setChanged();
    }

    public void tryTeleport(net.minecraft.world.entity.Entity entity) {
        if (targetPos == null || entity.level().isClientSide) return;

        long now = entity.level().getGameTime();
        UUID uuid = entity.getUUID();

        if (now % 1200 == 0) {
            COOLDOWNS.entrySet().removeIf(entry -> now - entry.getValue() > 100);
        }

        if (COOLDOWNS.containsKey(uuid)) {
            long lastTime = COOLDOWNS.get(uuid);
            if (now - lastTime < 40) {
                return;
            }
        }

        if (entity instanceof net.minecraft.world.entity.player.Player p && p.isCreative() && p.isCrouching()) return;

        if (entity.level() instanceof ServerLevel serverLevel) {
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(targetDimension));
            ServerLevel destLevel = serverLevel.getServer().getLevel(dimKey);

            if (destLevel != null) {
                COOLDOWNS.put(uuid, now);

                entity.teleportTo(
                        destLevel,
                        targetPos.getX() + 0.5,
                        targetPos.getY(),
                        targetPos.getZ() + 0.5,
                        Set.of(),
                        targetYaw,
                        targetPitch
                );
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (targetPos != null) {
            tag.put("TargetPos", NbtUtils.writeBlockPos(targetPos));
            tag.putFloat("TargetYaw", targetYaw);
            tag.putFloat("TargetPitch", targetPitch);
            tag.putString("TargetDim", targetDimension);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("TargetPos")) {
            this.targetPos = NbtUtils.readBlockPos(tag, "TargetPos").orElse(null);
            this.targetYaw = tag.getFloat("TargetYaw");
            this.targetPitch = tag.getFloat("TargetPitch");
            this.targetDimension = tag.getString("TargetDim");
        }
    }
}