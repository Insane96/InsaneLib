package insane96mcp.insanelib.data;

import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.util.LogHelper;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class JsonFeatureDataReloadListener extends SimplePreparableReloadListener<Void> {
    public static final JsonFeatureDataReloadListener INSTANCE;
    public static ICondition.IContext reloadContext;

    final ArrayList<JsonFeature> JSON_CONFIG_FEATURES = new ArrayList<>();

    static {
        INSTANCE = new JsonFeatureDataReloadListener();
    }

    @Override
    protected @NotNull Void prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        return null;
    }

    @Override
    protected void apply(@NotNull Void v, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        LogHelper.info("Reloading InsaneLib JsonFeature Json");

        for (JsonFeature feature : JSON_CONFIG_FEATURES) {
            feature.loadJsonConfigs();
        }
    }

    public void registerJsonConfigFeature(JsonFeature feature) {
        this.JSON_CONFIG_FEATURES.add(feature);
    }
}
