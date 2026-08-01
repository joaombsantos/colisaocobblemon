package me.marcronte.colisaocobblemon.features.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PokeLurerBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    // Nova propriedade para definir se é a parte de cima ou de baixo
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // --- HITBOXES DA METADE DE BAIXO (Y: 0 até 16) ---
    private static final VoxelShape LOWER_BASE = Block.box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0);
    private static final VoxelShape LOWER_PILLAR = Block.box(4.0, 2.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape LOWER_CORE = Shapes.or(LOWER_BASE, LOWER_PILLAR);

    private static final VoxelShape LOWER_NORTH = Shapes.or(LOWER_CORE, Block.box(5.0, 5.0, 2.0, 11.0, 8.0, 4.0));
    private static final VoxelShape LOWER_EAST = Shapes.or(LOWER_CORE, Block.box(12.0, 5.0, 5.0, 14.0, 8.0, 11.0));
    private static final VoxelShape LOWER_SOUTH = Shapes.or(LOWER_CORE, Block.box(5.0, 5.0, 12.0, 11.0, 8.0, 14.0));
    private static final VoxelShape LOWER_WEST = Shapes.or(LOWER_CORE, Block.box(2.0, 5.0, 5.0, 4.0, 8.0, 11.0));

    // --- HITBOXES DA METADE DE CIMA (Y: 16 até 32 subtraído por 16) ---
    // Como a parte de cima não tem painel, ela é simétrica em todas as direções!
    private static final VoxelShape UPPER_PILLAR = Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0); // O pilar continua
    private static final VoxelShape UPPER_TOP = Block.box(3.0, 14.0, 3.0, 13.0, 16.0, 13.0); // A tampa
    private static final VoxelShape UPPER_SHAPE = Shapes.or(UPPER_PILLAR, UPPER_TOP);

    public PokeLurerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)); // O bloco padrão é a parte de baixo
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    // Verifica se há espaço de 2 blocos de altura e coloca a metade inferior
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        // Se o bloco acima puder ser substituído (for ar, água, etc), permite a colocação
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null; // Cancela a colocação se não couber
    }

    // Assim que a parte inferior é colocada, "espawna" a parte superior automaticamente
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    // Quebra as duas metades juntas se uma for destruída
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        // Se o bloco vizinho que atualizou for na vertical (cima/baixo) em relação a nós...
        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            // Se o vizinho não for mais a nossa outra metade, nos autodestruímos (Air)
            return neighborState.is(this) && neighborState.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // Aplica as hitboxes baseadas na metade e na direção
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return UPPER_SHAPE;
        }
        return switch (state.getValue(FACING)) {
            case EAST -> LOWER_EAST;
            case SOUTH -> LOWER_SOUTH;
            case WEST -> LOWER_WEST;
            default -> LOWER_NORTH;
        };
    }
}