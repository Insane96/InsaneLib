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

    public static class Double extends ConfigOption<java.lang.Double> {

        public final ForgeConfigSpec.DoubleValue valueConfig;

        public Double(ForgeConfigSpec.Builder builder, String name, String description, double defaultValue, double min, double max) {
            super(builder, name, description);
            valueConfig = builder.defineInRange(name, defaultValue, min, max);
        }

        public java.lang.Double get() {
            return valueConfig.get();
        }
    }

    public static class Int extends ConfigOption<Integer> {

        public final ForgeConfigSpec.IntValue valueConfig;

        public Int(ForgeConfigSpec.Builder builder, String name, String description, int defaultValue, int min, int max) {
            super(builder, name, description);
            valueConfig = builder.defineInRange(name, defaultValue, min, max);
        }

        public Integer get() {
            return valueConfig.get();
        }
    }

    public static class Bool extends ConfigOption<Boolean> {

        public final ForgeConfigSpec.BooleanValue valueConfig;

        public Bool(ForgeConfigSpec.Builder builder, String name, String description, boolean defaultValue) {
            super(builder, name, description);
            valueConfig = builder.define(name, defaultValue);
        }

        public Boolean get() {
            return valueConfig.get();
        }
    }

    public static class StringList extends ConfigOption<List<? extends String>> {

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> valueConfig;

        public StringList(ForgeConfigSpec.Builder builder, String name, String description, List<String> defaultValue) {
            super(builder, name, description);
            valueConfig = builder.defineList(this.name, defaultValue, o -> o instanceof String);
        }

        public List<? extends String> get() {
            return valueConfig.get();
        }
    }
}
