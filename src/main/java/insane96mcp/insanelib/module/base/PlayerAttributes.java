package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.data.SerializableAttributeModifier;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(description = "Apply attribute modifiers to players. Attributes can be added in the config folder of this feature's folder (requires a world to be opened at least once). Updating attribute modifiers of players them to rejoin")
public class PlayerAttributes extends JsonFeature {
	public static final Identifier MOVEMENT_SPEED_REDUCTION_ID = InsaneLib.id("player_movement_speed_reduction");
	public static final Identifier BLOCK_REACH_REDUCTION_ID = InsaneLib.id("player_block_reach_reduction");
	//@Config(description = "In vanilla, if you attack as soon as you just attacked you already deal 20% of the full damage. This changes that to 0%.")
	//public static Boolean noDamageWhenSpamming = true;

	public static final ArrayList<SerializableAttributeModifier> ATTRIBUTE_MODIFIERS_DEFAULT = new ArrayList<>(List.of(

	));
	public static final ArrayList<SerializableAttributeModifier> attributeModifiers = new ArrayList<>();

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		this.getJsonConfigs().add(new JsonConfig<>("players_attribute_modifiers.json", attributeModifiers, ATTRIBUTE_MODIFIERS_DEFAULT, SerializableAttributeModifier.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneLib.CONFIG_FOLDER;
	}

	/*public static boolean noDamageWhenSpamming() {
		return isEnabled(PlayerAttributes.class) && noDamageWhenSpamming;
	}*/

	@SubscribeEvent
	public void onPlayerJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ServerPlayer player))
			return;

		for (SerializableAttributeModifier modifier : attributeModifiers)
			MCUtils.applyModifier(player, modifier.attribute(), modifier.id(), modifier.amount(), modifier.operation(), false);
	}

}