package insane96mcp.insanelib.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class BlacklistConfig {
	public ForgeConfigSpec.ConfigValue<List<? extends String>> listConfig;
	public ForgeConfigSpec.ConfigValue<Boolean> listAsWhitelistConfig;

	/**
	 * Creates a new Black/Whitelist config, containing the list, and if the list should be treated as a whitelist
	 * @param builder Config Builder
	 * @param listName Name of the list
	 * @param description Description for the list
	 * @param defaultList The default list
	 * @param isDefaultWhitelist If true, the list will default to whitelist instead of blacklist
	 */
	public BlacklistConfig(ForgeConfigSpec.Builder builder, String listName, String description, List<String> defaultList, boolean isDefaultWhitelist) {
		builder.comment(description).push(listName);
		listConfig = builder
				.defineList("Blacklist", defaultList, o -> o instanceof String);
		listAsWhitelistConfig = builder
				.comment("If true the list will be treated as a whitelist instead of blacklist")
				.define("List as Whitelist", isDefaultWhitelist);
		builder.pop();
	}
}
