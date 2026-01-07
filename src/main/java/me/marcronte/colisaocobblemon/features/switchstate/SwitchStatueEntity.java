package me.marcronte.colisaocobblemon.features.switchstate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SwitchStatueEntity extends BlockEntity {
    public SwitchStatueEntity(BlockPos pos, BlockState state) {
        super(SwitchStateRegistry.SWITCH_STATUE_BE, pos, state);
    }
}