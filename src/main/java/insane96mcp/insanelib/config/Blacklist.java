package insane96mcp.insanelib.config;

import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * IdTagMatcher Black/Whitelist
 */
public class Blacklist {
	public ArrayList<IdTagMatcher> blacklist;
	public boolean blacklistAsWhitelist;

	public Blacklist(ArrayList<IdTagMatcher> blacklist, boolean blacklistAsWhitelist) {
		this.blacklist = blacklist;
		this.blacklistAsWhitelist = blacklistAsWhitelist;
	}

	/**
	 * Returns true if the entity is in the blacklist or, if the list is a whitelist, is not in the whitelist
	 */
	public boolean isEntityListed(Entity entity) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesEntity(entity)) {
				if (!this.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && this.blacklistAsWhitelist);
	}

	/**
	 * Returns true if the block is in the blacklist or, if the list is a whitelist, is not in the whitelist
	 */
	public boolean isBlockListed(Block block) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesBlock(block)) {
				if (!this.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && this.blacklistAsWhitelist);
	}

	/**
	 * Returns true if the item is in the blacklist or, if the list is a whitelist, is not in the whitelist
	 */
	public boolean isItemListed(Item item) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesItem(item)) {
				if (!this.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && this.blacklistAsWhitelist);
	}

	/**
	 * Returns true if the fluid is in the blacklist or, if the list is a whitelist, is not in the whitelist
	 */
	public boolean isItemListed(Fluid fluid) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesFluid(fluid)) {
				if (!this.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && this.blacklistAsWhitelist);
	}

	/**
	 * Returns true if the ForgeRegistryEntry (item, block, etc) is in the blacklist or, if the list is a whitelist, is not in the whitelist
	 */
	public <T extends IForgeRegistryEntry<T>> boolean isBlackWhiteListed(ForgeRegistryEntry<T> entry) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesGeneric(entry)) {
				if (!this.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && this.blacklistAsWhitelist);
	}

	public static class Config {
		private final ForgeConfigSpec.Builder builder;

		public ForgeConfigSpec.ConfigValue<List<? extends String>> listConfig;
		public ForgeConfigSpec.ConfigValue<Boolean> listAsWhitelistConfig;

		public Config(ForgeConfigSpec.Builder builder, String optionName, String description) {
			this.builder = builder;
			builder.comment(description).push(optionName);
		}

		public Blacklist.Config setDefaultList(List<String> defaultValue) {
			listConfig = builder.defineList("Blacklist", defaultValue, o -> o instanceof String);
			return this;
		}

		public Blacklist.Config setIsDefaultWhitelist(boolean isDefaultWhitelist) {
			listAsWhitelistConfig = builder.comment("If true the list will be treated as a whitelist instead of blacklist").define("List as Whitelist", isDefaultWhitelist);
			return this;
		}

		public Blacklist.Config build() {
			builder.pop();
			return this;
		}

		public Blacklist get() {
			return new Blacklist((ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.listConfig.get()), this.listAsWhitelistConfig.get());
		}
	}
}
