package insane96mcp.insanelib.config;

import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeConfigSpec;

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

	public boolean isBlockBlackOrNotWhiteListed(Block entry) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesBlock(entry)) {
				if (!this.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && this.blacklistAsWhitelist);
	}

	public boolean isItemBlackOrNotWhiteListed(Item entry) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesItem(entry)) {
				if (!this.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && this.blacklistAsWhitelist);
	}

	public boolean isFluidBlackOrNotWhiteListed(Fluid entry) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesFluid(entry)) {
				if (!this.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && this.blacklistAsWhitelist);
	}

	public boolean isEntityBlackOrNotWhitelist(Entity entry) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesEntity(entry)) {
				if (!this.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && this.blacklistAsWhitelist);
	}

	public boolean isEntityBlackOrNotWhitelist(EntityType<?> entry) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.blacklist) {
			if (blacklistEntry.matchesEntity(entry)) {
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
