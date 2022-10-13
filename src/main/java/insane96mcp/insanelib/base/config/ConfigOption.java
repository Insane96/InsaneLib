package insane96mcp.insanelib.base.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public abstract class ConfigOption<T> {

    String name;
    ForgeConfigSpec.Builder builder;

    public ConfigOption(ForgeConfigSpec.Builder builder, String name, String description) {
        this.builder = builder;
        this.name = name;
        if (!description.equals("")) {
            builder.comment(description);
        }
    }

    public abstract T get();

    @Override
    public String toString() {
        return "ConfigOpt{name='%s'}".formatted(name);
    }

    public static class DoubleOption extends ConfigOption<java.lang.Double> {

        public final ForgeConfigSpec.DoubleValue valueConfig;

        public DoubleOption(ForgeConfigSpec.Builder builder, String name, String description, double defaultValue, double min, double max) {
            super(builder, name, description);
            valueConfig = builder.defineInRange(name, defaultValue, min, max);
        }

        public java.lang.Double get() {
            return valueConfig.get();
        }
    }

    public static class IntOption extends ConfigOption<Integer> {

        public final ForgeConfigSpec.IntValue valueConfig;

        public IntOption(ForgeConfigSpec.Builder builder, String name, String description, int defaultValue, int min, int max) {
            super(builder, name, description);
            valueConfig = builder.defineInRange(name, defaultValue, min, max);
        }

        public Integer get() {
            return valueConfig.get();
        }
    }

    public static class BoolOption extends ConfigOption<Boolean> {

        public final ForgeConfigSpec.BooleanValue valueConfig;

        public BoolOption(ForgeConfigSpec.Builder builder, String name, String description, boolean defaultValue) {
            super(builder, name, description);
            valueConfig = builder.define(name, defaultValue);
        }

        public Boolean get() {
            return valueConfig.get();
        }
    }

    public static class StringOption extends ConfigOption<String> {

        public final ForgeConfigSpec.ConfigValue<String> valueConfig;

        public StringOption(ForgeConfigSpec.Builder builder, String name, String description, String defaultValue) {
            super(builder, name, description);
            valueConfig = builder.define(this.name, defaultValue);
        }

        public String get() {
            return valueConfig.get();
        }
    }

    public static class StringListOption extends ConfigOption<List<? extends String>> {

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> valueConfig;

        public StringListOption(ForgeConfigSpec.Builder builder, String name, String description, List<String> defaultValue) {
            super(builder, name, description);
            valueConfig = builder.defineList(this.name, defaultValue, o -> o instanceof String);
        }

        public List<? extends String> get() {
            return valueConfig.get();
        }
    }

    public static class EnumOption extends ConfigOption<Enum<?>> {

        public final ForgeConfigSpec.EnumValue<?> valueConfig;

        public EnumOption(ForgeConfigSpec.Builder builder, String name, String description, Enum<?> defaultValue) {
            super(builder, name, description);
            valueConfig = builder.defineEnum(this.name, defaultValue);
        }

        public Enum<?> get() {
            return valueConfig.get();
        }
    }
}
