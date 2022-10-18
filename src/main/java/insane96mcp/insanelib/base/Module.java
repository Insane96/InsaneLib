package insane96mcp.insanelib.base;

import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.setup.Config;
import insane96mcp.insanelib.util.LogHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public enum Module implements IExtensibleEnum {
    BASE(Config.builder, "Base", "", true, false);

    private final ForgeConfigSpec.ConfigValue<Boolean> enabledConfig;

    final ForgeConfigSpec.Builder builder;

    private boolean enabled;

    private final boolean canBeDisabled;

    private final String name;
    private final String description;

    private final List<Feature> features = new ArrayList<>();

    Module(final ForgeConfigSpec.Builder builder, String moduleName, String description, boolean enabledByDefault, boolean canBeDisabled) {
        this.builder = builder;
        this.name = moduleName;
        this.description = description;
        this.canBeDisabled = canBeDisabled;
        if (canBeDisabled)
            if (!description.equals(""))
                enabledConfig = this.builder.comment(description).define("Enable " + this.name + " module", enabledByDefault);
            else
                enabledConfig = this.builder.define("Enable " + this.name, enabledByDefault);
        else
            enabledConfig = null;

        MinecraftForge.EVENT_BUS.register(this);
    }

    Module(final ForgeConfigSpec.Builder builder, String moduleName, String description, boolean enabledByDefault) {
        this(builder, moduleName, description, enabledByDefault, true);
    }

    Module(final ForgeConfigSpec.Builder builder, String moduleName, String description) {
        this(builder, moduleName, description, true);
    }

    Module(final ForgeConfigSpec.Builder builder, String moduleName) {
        this(builder, moduleName, "", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return this.name;
    }

    public List<Feature> getFeatures() {
        return this.features;
    }

    @SubscribeEvent
    public void loadConfig(final ModConfigEvent event) {
        if (canBeDisabled)
            this.enabled = enabledConfig.get();
        else
            this.enabled = true;
        this.features.forEach(feature -> feature.loadConfig(event));
    }

    public void pushConfig() {
        if (description.equals("")) {
            this.builder.push(this.getName());
        }
        else {
            this.builder.comment(this.description).push(this.getName());
        }
    }

    public void popConfig() {
        this.builder.pop();
    }

    private static final Type LOAD_FEATURE_TYPE = Type.getType(LoadFeature.class);
    private static final ArrayList<Module> MODULES = new ArrayList<>();

    public static void loadFeatures(String modId, ClassLoader classLoader) {
        MODULES.clear();
        ModFileScanData modFileScanData = ModList.get().getModFileById(modId).getFile().getScanResult();
        modFileScanData.getAnnotations().stream()
                .filter(annotationData -> LOAD_FEATURE_TYPE.equals(annotationData.annotationType()))
                .sorted(Comparator.comparing(d -> d.getClass().getName()))
                .forEach(annotationData -> {
                    try {
                        Type type = annotationData.clazz();
                        Class<?> clazz = Class.forName(type.getClassName(), false, classLoader);
                        LogHelper.info("Found InsaneLib Feature class " + type.getClassName());

                        Map<String, Object> vals = annotationData.annotationData();
                        Module module = Module.valueOf(((ModAnnotation.EnumHolder) vals.get("module")).getValue());

                        boolean enabledByDefault = true;
                        if (vals.containsKey("enabledByDefault"))
                            enabledByDefault = (Boolean) vals.get("enabledByDefault");

                        boolean canBeDisabled = true;
                        if (vals.containsKey("canBeDisabled"))
                            canBeDisabled = (Boolean) vals.get("canBeDisabled");

                        Feature feature = (Feature) clazz.getDeclaredConstructor(Module.class, boolean.class, boolean.class).newInstance(module, enabledByDefault, canBeDisabled);
                        module.features.add(feature);
                        if (!MODULES.contains(module))
                            MODULES.add(module);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Failed to load Module %s".formatted(annotationData), e);
                    }
                });
        MODULES.forEach(m -> {
            m.pushConfig();
            m.getFeatures().forEach(Feature::loadConfigOptions);
            m.popConfig();
        });
    }

    public static Module create(String name, final ForgeConfigSpec.Builder builder, String moduleName)
    {
        throw new IllegalStateException("Enum not extended");
    }

    public static Module create(String name, final ForgeConfigSpec.Builder builder, String moduleName, String description)
    {
        throw new IllegalStateException("Enum not extended");
    }

    public static Module create(String name, final ForgeConfigSpec.Builder builder, String moduleName, String description, boolean enabledByDefault)
    {
        throw new IllegalStateException("Enum not extended");
    }

    public static Module create(String name, final ForgeConfigSpec.Builder builder, String moduleName, String description, boolean enabledByDefault, boolean canBeDisabled)
    {
        throw new IllegalStateException("Enum not extended");
    }
}
