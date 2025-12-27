package me.marcronte.colisaocobblemon.features.pokeloot;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PokeLootBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {

    public final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            PokeLootBlockEntity.this.setChanged();
        }
    };

    private final Set<UUID> lootedPlayers = new HashSet<>();

    public PokeLootBlockEntity(BlockPos pos, BlockState blockState) {
        super(PokeLootRegistry.POKE_LOOT_BE, pos, blockState);
    }


    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    public void tryLoot(ServerPlayer player) {
        ItemStack loot = inventory.getItem(0);

        if (loot.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.colisao-cobblemon.poke_loot_empty").withStyle(ChatFormatting.RED), true);
            return;
        }

        if (lootedPlayers.contains(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.colisao-cobblemon.poke_loot_already_got").withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        ItemStack toGive = loot.copy();
        toGive.setCount(1);

        if (player.getInventory().add(toGive)) {
            lootedPlayers.add(player.getUUID());
            player.displayClientMessage(
                    Component.translatable(
                            "message.colisao-cobblemon.poke_loot_get_item",
                            loot.getDisplayName()
                    ).withStyle(ChatFormatting.GREEN),
                    true
            );
            setChanged();
        } else {
            player.displayClientMessage(Component.translatable("message.colisao-cobblemon.inventory_is_full").withStyle(ChatFormatting.RED), true);
        }
    }

    public void toggleVisibility() {
        if (level != null) {
            boolean current = getBlockState().getValue(PokeLootBlock.VISIBLE);
            level.setBlock(worldPosition, getBlockState().setValue(PokeLootBlock.VISIBLE, !current), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!inventory.getItem(0).isEmpty()) {
            tag.put("LootItem", inventory.getItem(0).save(registries));
        }
        ListTag playerList = new ListTag();
        for (UUID uuid : lootedPlayers) {
            playerList.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("LootedPlayers", playerList);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("LootItem")) {
            inventory.setItem(0, ItemStack.parseOptional(registries, tag.getCompound("LootItem")));
        }
        lootedPlayers.clear();
        if (tag.contains("LootedPlayers")) {
            ListTag list = tag.getList("LootedPlayers", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                try {
                    lootedPlayers.add(UUID.fromString(list.getString(i)));
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Poke Loot");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new PokeLootMenu(syncId, playerInventory, this.inventory, this);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}