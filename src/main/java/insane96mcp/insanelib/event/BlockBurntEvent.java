package insane96mcp.insanelib.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Fired when a block is about to be burnt by fire.
 */

public class BlockBurntEvent extends BlockEvent {

    public BlockBurntEvent(LevelAccessor level, BlockPos pos, BlockState state) {
        super(level, pos, state);
    }
}
