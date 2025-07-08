package insane96mcp.insanelib.base;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import insane96mcp.insanelib.InsaneLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class FeatureEnabledLootCondition implements LootItemCondition {
	public static final LootItemConditionType TYPE = new LootItemConditionType(new FeatureEnabledLootCondition.Serializer());
	private static final ResourceLocation ID = InsaneLib.location("feature_enabled");
	private final String featureName;

	public FeatureEnabledLootCondition(String featureName) {
		this.featureName = featureName;
	}

	@Override
	public LootItemConditionType getType() {
		return TYPE;
	}

	@Override
	public boolean test(LootContext lootContext) {
		return Feature.isEnabled(featureName);
	}

	public static FeatureEnabledLootCondition.Builder builder(final String featureName)
	{
		return new FeatureEnabledLootCondition.Builder(featureName);
	}

	public static class Builder implements LootItemCondition.Builder
	{
		private final String featureName;

		public Builder(String featureName)
		{
			if (featureName == null) throw new IllegalArgumentException("featureName must not be null");
			this.featureName = featureName;
		}

		@Override
		public LootItemCondition build()
		{
			return new FeatureEnabledLootCondition(this.featureName);
		}
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<FeatureEnabledLootCondition>
	{
		@Override
		public void serialize(JsonObject object, FeatureEnabledLootCondition instance, JsonSerializationContext ctx)
		{
			object.addProperty("feature", instance.featureName);
		}

		@Override
		public FeatureEnabledLootCondition deserialize(JsonObject object, JsonDeserializationContext ctx)
		{
			String feature = GsonHelper.getAsString(object, "feature");
			if (Module.getFeature(feature).isEmpty())
				throw new JsonSyntaxException("Unknown feature: " + feature);
			return new FeatureEnabledLootCondition(feature);
		}
	}
}
