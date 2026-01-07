package me.marcronte.colisaocobblemon.features.switchstate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StateBlockEntity extends BlockEntity {
    public StateBlockEntity(BlockPos pos, BlockState blockState) {
        super(SwitchStateRegistry.STATE_BLOCK_BE, pos, blockState);
    }
}