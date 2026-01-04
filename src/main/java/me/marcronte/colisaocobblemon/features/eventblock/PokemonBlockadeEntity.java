package me.marcronte.colisaocobblemon.features.eventblock;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.PoseType;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PokemonBlockadeEntity extends BlockEntity implements ExtendedScreenHandlerFactory<PokemonBlockadeEntity.OpeningData> {

    private String pokemonProperties = "snorlax level=30";
    private String eventId = "eventID";
    private boolean catchable = true;
    private String checkMessage = "A Pokemon blocks the way.";
    private String wakeMessage = "The Pokemon woke up angry!";
    private int hitboxSize = 1;

    public final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            PokemonBlockadeEntity.this.setChanged();
        }
    };

    public PokemonBlockadeEntity(BlockPos pos, BlockState blockState) {
        super(PokemonBlockade.ENTITY_TYPE, pos, blockState);
    }

    public record OpeningData(BlockPos pos, String props, String eventId, boolean catchable, String checkMessage, String wakeMessage, int hitboxSize) {

        public static final StreamCodec<RegistryFriendlyByteBuf, OpeningData> CODEC = StreamCodec.of(
                // Encoder
                (buf, data) -> {
                    buf.writeBlockPos(data.pos);
                    buf.writeUtf(data.props);
                    buf.writeUtf(data.eventId);
                    buf.writeBoolean(data.catchable);
                    buf.writeUtf(data.checkMessage);
                    buf.writeUtf(data.wakeMessage);
                    buf.writeInt(data.hitboxSize);
                },
                // Decoder
                buf -> new OpeningData(
                        buf.readBlockPos(),
                        buf.readUtf(),
                        buf.readUtf(),
                        buf.readBoolean(),
                        buf.readUtf(),
                        buf.readUtf(),
                        buf.readInt()
                )
        );
    }

    // Getters
    public String getPokemonProperties() { return pokemonProperties; }
    public String getEventId() { return eventId; }
    public boolean isCatchable() { return catchable; }
    public String getCheckMessage() { return checkMessage; }
    public String getWakeMessage() { return wakeMessage; }
    public int getHitboxSize() { return hitboxSize; } // Novo Getter
    public ItemStack getRequiredKeyItem() { return inventory.getItem(0); }

    public void setConfig(String props, String id, boolean catchable, String checkMsg, String wakeMsg, int size) {
        this.pokemonProperties = props;
        this.eventId = id;
        this.catchable = catchable;
        this.checkMessage = checkMsg;
        this.wakeMessage = wakeMsg;
        this.hitboxSize = Math.max(1, Math.min(5, size));


        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public OpeningData getScreenOpeningData(ServerPlayer player) {
        return new OpeningData(worldPosition, pokemonProperties, eventId, catchable, checkMessage, wakeMessage, hitboxSize);
    }

    @Override
    public @NotNull Component getDisplayName() { return Component.translatable("message.colisao-cobblemon.event_settings"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new PokemonBlockadeMenu(syncId, playerInventory, this.inventory, getScreenOpeningData((ServerPlayer) player));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("PokemonProps", pokemonProperties);
        tag.putString("EventId", eventId);
        tag.putBoolean("Catchable", catchable);
        tag.putString("CheckMessage", checkMessage);
        tag.putString("WakeMessage", wakeMessage);
        tag.putInt("HitboxSize", hitboxSize);
        if (!inventory.getItem(0).isEmpty()) {
            tag.put("KeyItem", inventory.getItem(0).save(registries));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.pokemonProperties = tag.getString("PokemonProps");
        this.eventId = tag.getString("EventId");
        if (tag.contains("Catchable")) this.catchable = tag.getBoolean("Catchable");
        if (tag.contains("CheckMessage")) this.checkMessage = tag.getString("CheckMessage");
        if (tag.contains("WakeMessage")) this.wakeMessage = tag.getString("WakeMessage");
        if (tag.contains("HitboxSize")) this.hitboxSize = tag.getInt("HitboxSize");
        if (tag.contains("KeyItem")) {
            inventory.setItem(0, ItemStack.parseOptional(registries, tag.getCompound("KeyItem")));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public AABB getHitboxAABB() {
        double offset = (hitboxSize - 1) / 2.0;
        return new AABB(
                worldPosition.getX() - offset, worldPosition.getY(), worldPosition.getZ() - offset,
                worldPosition.getX() + 1.0 + offset, worldPosition.getY() + hitboxSize, worldPosition.getZ() + 1.0 + offset
        );
    }

    public void activateBattle(ServerPlayer player) {
        ServerLevel level = (ServerLevel) this.level;
        if (level == null) return;

        if (EventBlockData.get(level).isCompleted(player.getUUID(), this.eventId)) {
            player.connection.send(new ClientboundBlockUpdatePacket(this.worldPosition, Blocks.AIR.defaultBlockState()));
            return;
        }

        try {
            AABB searchBox = new AABB(this.worldPosition).inflate(10);

            List<PokemonEntity> existingEntities = level.getEntitiesOfClass(PokemonEntity.class, searchBox, entity -> {
                CompoundTag data = entity.getPokemon().getPersistentData();
                return data.getLong("event_pos") == this.worldPosition.asLong()
                        && data.hasUUID("event_owner")
                        && data.getUUID("event_owner").equals(player.getUUID());
            });

            if (!existingEntities.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.colisao-cobblemon.pokemon_already_active").withStyle(ChatFormatting.RED), true);
                return;
            }

            PokemonEntity pokeEntity = PokemonProperties.Companion.parse(this.pokemonProperties, " ", "=").createEntity(level);
            pokeEntity.setPos(this.worldPosition.getX() + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5);

            pokeEntity.getPokemon().getPersistentData().putString("event_id", this.eventId);
            pokeEntity.getPokemon().getPersistentData().putLong("event_pos", this.worldPosition.asLong());

            pokeEntity.getPokemon().getPersistentData().putUUID("event_owner", player.getUUID());

            if (!this.catchable) {
                pokeEntity.getPokemon().getPersistentData().putBoolean("uncatchable", true);
            }

            level.addFreshEntity(pokeEntity);
            player.displayClientMessage(Component.literal(this.wakeMessage).withStyle(ChatFormatting.GOLD), true);

            boolean started = pokeEntity.forceBattle(player);

            if (!started) {
                player.displayClientMessage(Component.translatable("message.colisao-cobblemon.not_able_to_battle").withStyle(ChatFormatting.RED), true);
                pokeEntity.discard();
            } else {
                if (player.getServer() != null) {
                    player.getServer().execute(() -> {
                        player.connection.send(new ClientboundBlockUpdatePacket(this.worldPosition, Blocks.AIR.defaultBlockState()));
                    });
                }
            }

        } catch (Exception e) {
            ColisaoCobblemon.LOGGER.error("Error when start a battle on {}", this.worldPosition, e);
            player.displayClientMessage(Component.literal("Internal Error.").withStyle(ChatFormatting.RED), true);
        }
    }

    // --- TICKER (Force field) ---
    public static void serverTick(Level level, BlockPos pos, BlockState state, PokemonBlockadeEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int size = entity.getHitboxSize();
        if (size <= 1) return;

        AABB box = entity.getHitboxAABB().inflate(0.1);
        List<Player> players = level.getEntitiesOfClass(Player.class, box);

        for (Player player : players) {
            if (player.isCreative() && player.isCrouching()) continue;
            if (EventBlockData.get(serverLevel).isCompleted(player.getUUID(), entity.getEventId())) continue;


            Vec3 center = pos.getCenter();
            Vec3 playerPos = player.position();
            Vec3 pushDir = playerPos.subtract(center).normalize();

            if (pushDir.lengthSqr() < 0.0001) pushDir = new Vec3(1, 0, 0);


            double strength = 0.1;
            player.push(pushDir.x * strength, strength, pushDir.z * strength);
            player.hurtMarked = true;

            if (player.tickCount % 20 == 0) {
                player.displayClientMessage(Component.literal(entity.getCheckMessage()).withStyle(ChatFormatting.YELLOW), true);
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PokemonBlockadeEntity entity) {
        if (entity.getClientFakeEntity() != null) {
            entity.clientFakeEntity.tick();
        }
    }

    private PokemonEntity clientFakeEntity;
    private String lastCheckedProps = "";


    @Nullable
    public PokemonEntity getClientFakeEntity() {
        if (this.level == null || this.pokemonProperties == null) return null;

        if (!this.pokemonProperties.equals(lastCheckedProps)) {
            this.lastCheckedProps = this.pokemonProperties;

            try {
                String[] parts = this.pokemonProperties.split("[ =]");
                String speciesName = parts.length > 0 ? parts[0].toLowerCase() : "";

                if (PokemonSpecies.getByName(speciesName) == null) {
                    this.clientFakeEntity = null;
                } else {
                    this.clientFakeEntity = PokemonProperties.Companion.parse(this.pokemonProperties, " ", "=").createEntity(this.level);

                    if (this.clientFakeEntity != null) {
                        this.clientFakeEntity.setNoAi(true);
                        this.clientFakeEntity.setSilent(true);
                        this.clientFakeEntity.setNoGravity(true);

                        if (speciesName.equals("snorlax")) {
                            this.clientFakeEntity.getEntityData().set(PokemonEntity.getPOSE_TYPE(), PoseType.SLEEP);
                        }
                    }
                }
            } catch (Exception e) {
                this.clientFakeEntity = null;
            }
        }
        return this.clientFakeEntity;
    }
}