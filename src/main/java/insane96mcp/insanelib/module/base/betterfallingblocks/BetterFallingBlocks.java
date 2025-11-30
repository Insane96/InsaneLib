package insane96mcp.insanelib.module.base.betterfallingblocks;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;

@LoadFeature(
		module = "insanelib:base",
		description = "Changes the behaviour of Falling Blocks to be smarter by preventing them from getting stuck when landing on a block but their center has air below, or try to stack or move them when inside a block.",
		canBeDisabled = false
)
public class BetterFallingBlocks extends Feature {
    @Config(description = "If true, falling blocks will break blocks that are instabreakable and place instead of dropping.")
    public static Boolean breakInstabreakBlocks = true;
    @Config(description = "Fix dupe exploit through dimensions.")
    public static Boolean fixDupeExploit = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
	}
}
