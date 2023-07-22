package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemistCauldronBlock extends BaseEntityBlock {
    public AlchemistCauldronBlock() {
        super(Properties.copy(Blocks.CAULDRON));
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(LEVEL, 0));

    }

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int MAX_LEVELS = 4;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MAX_LEVELS);

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTicker(pLevel, pBlockEntityType, BlockRegistry.ALCHEMIST_CAULDRON_TILE.get());
    }

    @javax.annotation.Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level pLevel, BlockEntityType<T> pServerType, BlockEntityType<? extends AlchemistCauldronTile> pClientType) {
        return pLevel.isClientSide ? null : createTickerHelper(pServerType, pClientType, AlchemistCauldronTile::serverTick);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, LEVEL);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHit) {
        if (level.getBlockEntity(pos) instanceof AlchemistCauldronTile tile) {
            return tile.handleUse(blockState, level, pos, player, hand);
        }
        return super.use(blockState, level, pos, player, hand, blockHit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemistCauldronTile(pos, state);
    }

    @Override
    @SuppressWarnings({"all"})
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborstate, LevelAccessor level, BlockPos pos, BlockPos pNeighborPos) {
        if (direction.equals(Direction.DOWN)) {
            level.setBlock(pos, state.setValue(LIT, isFireSource(neighborstate)), 11);
        }
        return super.updateShape(state, direction, neighborstate, level, pos, pNeighborPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        LevelAccessor levelaccessor = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos().below();
        boolean flag = isFireSource(levelaccessor.getBlockState(blockpos));
        return this.defaultBlockState().setValue(LIT, flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.MODEL;
    }

    public boolean isFireSource(BlockState blockState) {
        //TODO: its a magic cauldron. why does it need a fire source?
        return true;//CampfireBlock.isLitCampfire(blockState);
    }

    public static boolean isLit(BlockState blockState) {
        return blockState.hasProperty(LIT) && blockState.getValue(LIT);
    }

    public static int getLevel(BlockState blockState) {
        return blockState.hasProperty(LEVEL) ? blockState.getValue(LEVEL) : 0;
    }

    public static boolean isBoiling(BlockState blockState) {
        return isLit(blockState) && getLevel(blockState) > 0;
    }

}
