package insane96mcp.insanelib.base.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public abstract class ConfigOpt<T> {

    String name, description;
    ForgeConfigSpec.Builder builder;

    public ConfigOpt(ForgeConfigSpec.Builder builder, String name, String description) {
        this.builder = builder;
        this.name = name;
        this.description = description;
        if (!this.description.equals("")) {
            builder.comment(this.description);
        }
    }

    public abstract T get();

    @Override
    public String toString() {
        return "ConfigOpt{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public static class Double extends ConfigOpt<java.lang.Double> {

        public ForgeConfigSpec.DoubleValue valueConfig;

        public Double(ForgeConfigSpec.Builder builder, String name, String description, double defaultValue, double min, double max) {
            super(builder, name, description);
            valueConfig = builder.defineInRange(name, defaultValue, min, max);
        }

        public java.lang.Double get() {
            return valueConfig.get();
        }
    }

    public static class Int extends ConfigOpt<Integer> {

        public ForgeConfigSpec.IntValue valueConfig;

        public Int(ForgeConfigSpec.Builder builder, String name, String description, int defaultValue, int min, int max) {
            super(builder, name, description);
            valueConfig = builder.defineInRange(name, defaultValue, min, max);
        }

        public Integer get() {
            return valueConfig.get();
        }
    }

    public static class Bool extends ConfigOpt<Boolean> {

        public ForgeConfigSpec.BooleanValue valueConfig;

        public Bool(ForgeConfigSpec.Builder builder, String name, String description, boolean defaultValue) {
            super(builder, name, description);
            valueConfig = builder.define(name, defaultValue);
        }

        public Boolean get() {
            return valueConfig.get();
        }
    }

    public static class StringList extends ConfigOpt<List<? extends String>> {

        public ForgeConfigSpec.ConfigValue<List<? extends String>> valueConfig;

        public StringList(ForgeConfigSpec.Builder builder, String name, String description, List<String> defaultValue) {
            super(builder, name, description);
            valueConfig = builder.defineList(this.name, defaultValue, o -> o instanceof String);
        }

        public List<? extends String> get() {
            return valueConfig.get();
        }
    }
}
