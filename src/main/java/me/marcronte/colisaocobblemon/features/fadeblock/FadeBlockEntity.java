package me.marcronte.colisaocobblemon.features.fadeblock;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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

public class FadeBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {

    public final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            FadeBlockEntity.this.setChanged();
        }
    };

    public FadeBlockEntity(BlockPos pos, BlockState blockState) {
        super(FadeBlock.ENTITY_TYPE, pos, blockState);
    }

    public ItemStack getKeyItem() {
        return inventory.getItem(0);
    }

    public void toggleVisibility() {
        if (level != null) {
            boolean current = getBlockState().getValue(FadeBlock.VISIBLE);
            level.setBlock(worldPosition, getBlockState().setValue(FadeBlock.VISIBLE, !current), 3);
        }
    }


    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("message.colisao-cobblemon.config_fade_block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new FadeBlockMenu(syncId, playerInventory, this.inventory, this.worldPosition);
    }

    // --- Save ---
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!inventory.getItem(0).isEmpty()) {
            tag.put("KeyItem", inventory.getItem(0).save(registries));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
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
}