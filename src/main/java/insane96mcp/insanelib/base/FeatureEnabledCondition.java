package insane96mcp.insanelib.base;

import com.google.gson.JsonObject;
import insane96mcp.insanelib.InsaneLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class FeatureEnabledCondition implements ICondition {
	private static final ResourceLocation ID = InsaneLib.location("feature_enabled");
	private final String featureName;

	public FeatureEnabledCondition(String featureName) {
		this.featureName = featureName;
	}

	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public boolean test(IContext context) {
		return Feature.isEnabled(featureName);
	}

	@Override
	public String toString()
	{
		return "feature_enabled(\"" + featureName + "\")";
	}

	public static class Serializer implements IConditionSerializer<FeatureEnabledCondition>
	{
		public static final FeatureEnabledCondition.Serializer INSTANCE = new FeatureEnabledCondition.Serializer();

		@Override
		public void write(JsonObject json, FeatureEnabledCondition value)
		{
			json.addProperty("feature", value.featureName);
		}

		@Override
		public FeatureEnabledCondition read(JsonObject json)
		{
			return new FeatureEnabledCondition(GsonHelper.getAsString(json, "feature"));
		}

		@Override
		public ResourceLocation getID()
		{
			return FeatureEnabledCondition.ID;
		}
	}
}
