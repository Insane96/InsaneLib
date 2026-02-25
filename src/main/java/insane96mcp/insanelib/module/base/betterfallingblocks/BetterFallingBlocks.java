package insane96mcp.insanelib.module.base.betterfallingblocks;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

@LoadFeature(
		description = "Changes the behaviour of Falling Blocks to be smarter by preventing them from getting stuck when landing on a block but their center has air below, or try to stack or move them when inside a block.",
		canBeDisabled = false
)
public class BetterFallingBlocks extends Feature {
	public static final TagKey<Block> BLACKLISTED_BLOCKS = TagKey.create(Registries.BLOCK, InsaneLib.location("blacklisted_better_falling_blocks"));

    @Config(description = "If true, falling blocks will break blocks that are instabreakable and place instead of dropping. If the falling block ends in a non-insta-break block, it will move to the side instead.")
    public static Boolean breakInstabreakBlocks = true;
    @Config(description = "Fix dupe exploit through dimensions.")
    public static Boolean fixDupeExploit = true;
}
