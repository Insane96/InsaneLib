package insane96mcp.insanelib.mixin.accessor;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerLevel.class)
public interface ServerLevelAccessor {
	@Accessor
	@Mutable
	void setTickTime(boolean tickTime);
}
