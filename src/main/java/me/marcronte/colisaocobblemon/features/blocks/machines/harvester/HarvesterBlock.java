package me.marcronte.colisaocobblemon.features.blocks.machines.harvester;

import me.marcronte.colisaocobblemon.features.blocks.machines.PokemonWorkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class HarvesterBlock extends PokemonWorkBlock {

    public HarvesterBlock(Properties properties) {
        super(properties);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HarvesterBlockEntity(pos, state);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HarvesterBlockEntity harvester) {
                player.openMenu(harvester);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }

        return blockEntityType == me.marcronte.colisaocobblemon.ModBlocks.HARVESTER_BE ?
                (lvl, pos, st, blockEntity) -> {
                    if (blockEntity instanceof HarvesterBlockEntity harvester) {
                        harvester.tick(lvl, pos);
                    }
                } : null;
    }
}