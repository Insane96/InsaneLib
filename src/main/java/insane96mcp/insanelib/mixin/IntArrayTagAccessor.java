package insane96mcp.insanelib.mixin;

import net.minecraft.nbt.IntArrayTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(IntArrayTag.class)
public interface IntArrayTagAccessor {
	@Invoker("toArray")
	static int[] toArray(List<Integer> intList) {
		throw new AssertionError();
	}
}
