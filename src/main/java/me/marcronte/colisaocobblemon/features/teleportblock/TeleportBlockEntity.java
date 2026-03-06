package me.marcronte.colisaocobblemon.features.teleportblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
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

    public static final Map<String, GlobalPos> HUBS = new HashMap<>();
    public static final Map<UUID, GlobalPos> RETURNS = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    public TeleportType type = TeleportType.STATIC;
    public String linkId = "";

    private BlockPos targetPos;
    private float targetYaw;
    private float targetPitch;
    private String targetDimension;

    public TeleportBlockEntity(BlockPos pos, BlockState blockState) {
        super(TeleportRegistry.TELEPORT_BLOCK_BE, pos, blockState);
        if (blockState.getBlock() instanceof TeleportBlock tb) {
            this.type = tb.getType();
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        registerHub();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Type")) this.type = TeleportType.valueOf(tag.getString("Type"));
        if (tag.contains("LinkId")) this.linkId = tag.getString("LinkId");
        if (tag.contains("TargetPos")) {
            this.targetPos = NbtUtils.readBlockPos(tag, "TargetPos").orElse(null);
            this.targetYaw = tag.getFloat("TargetYaw");
            this.targetPitch = tag.getFloat("TargetPitch");
            this.targetDimension = tag.getString("TargetDim");
        }
        registerHub();
    }

    private void registerHub() {
        if (this.level != null && !this.level.isClientSide && this.type == TeleportType.HUB && this.linkId != null && !this.linkId.isEmpty()) {
            HUBS.put(this.linkId, GlobalPos.of(this.level.dimension(), this.getBlockPos()));
        }
    }

    public void setDestination(BlockPos pos, float yaw, float pitch, Level level) {
        this.targetPos = pos;
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.targetDimension = level.dimension().location().toString();
        this.setChanged();
    }

    public void setLinkId(String id) {
        this.linkId = id;
        if (this.type == TeleportType.HUB && !this.level.isClientSide) {
            HUBS.put(id, GlobalPos.of(this.level.dimension(), this.getBlockPos()));
        }
        this.setChanged();
    }

    public void tryTeleport(net.minecraft.world.entity.Entity entity) {
        if (entity.level().isClientSide) return;

        long now = entity.level().getGameTime();
        UUID uuid = entity.getUUID();

        if (now % 1200 == 0) COOLDOWNS.entrySet().removeIf(entry -> now - entry.getValue() > 100);
        if (COOLDOWNS.containsKey(uuid) && now - COOLDOWNS.get(uuid) < 40) return;
        if (entity instanceof net.minecraft.world.entity.player.Player p && p.isCreative() && p.isCrouching()) return;

        if (entity.level() instanceof ServerLevel serverLevel) {

            if (this.type == TeleportType.HUB) {
                GlobalPos returnPos = RETURNS.get(uuid);
                if (returnPos != null) {
                    ServerLevel destLevel = serverLevel.getServer().getLevel(returnPos.dimension());
                    if (destLevel != null) {
                        COOLDOWNS.put(uuid, now);
                        RETURNS.remove(uuid);
                        entity.teleportTo(destLevel, returnPos.pos().getX() + 0.5, returnPos.pos().getY() + 1, returnPos.pos().getZ() + 0.5, Set.of(), entity.getYRot(), entity.getXRot());
                    }
                }
            }

            else if (this.type == TeleportType.SPOKE) {
                GlobalPos hubPos = HUBS.get(this.linkId);
                if (hubPos != null) {
                    ServerLevel destLevel = serverLevel.getServer().getLevel(hubPos.dimension());
                    if (destLevel != null) {
                        COOLDOWNS.put(uuid, now);
                        RETURNS.put(uuid, GlobalPos.of(serverLevel.dimension(), this.getBlockPos()));
                        entity.teleportTo(destLevel, hubPos.pos().getX() + 0.5, hubPos.pos().getY() + 1, hubPos.pos().getZ() + 0.5, Set.of(), entity.getYRot(), entity.getXRot());
                    }
                }
            }

            else if (this.type == TeleportType.STATIC && targetPos != null) {
                ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(targetDimension));
                ServerLevel destLevel = serverLevel.getServer().getLevel(dimKey);
                if (destLevel != null) {
                    COOLDOWNS.put(uuid, now);
                    entity.teleportTo(destLevel, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, Set.of(), targetYaw, targetPitch);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Type", type.name());
        tag.putString("LinkId", linkId);
        if (targetPos != null) {
            tag.put("TargetPos", NbtUtils.writeBlockPos(targetPos));
            tag.putFloat("TargetYaw", targetYaw);
            tag.putFloat("TargetPitch", targetPitch);
            tag.putString("TargetDim", targetDimension);
        }
    }
}