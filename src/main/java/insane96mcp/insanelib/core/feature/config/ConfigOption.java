package insane96mcp.insanelib.core.feature.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ConfigOption<T> {

    String name;
    ModConfigSpec.Builder builder;

    public ConfigOption(ModConfigSpec.Builder builder, String name, String description) {
        this.builder = builder;
        this.name = name;
        if (!"".equals(description)) {
            builder.comment(description);
        }
    }

    public String getName() {
        return this.name;
    }

    public abstract T get();

    public abstract void set(Object value);

    /**
     * Return the path of the config if the config is simple with one ConfigValue
     */
    @Nullable
    public abstract List<String> getConfigPath();

    @Override
    public String toString() {
        return "ConfigOpt{name='%s'}".formatted(name);
    }

    public static class GenericOption extends ConfigOption<Object> {

        public final ModConfigSpec.ConfigValue<Object> valueConfig;

        public GenericOption(ModConfigSpec.Builder builder, String name, String description, Object defaultValue) {
            super(builder, name, description);
            valueConfig = builder.define(name, defaultValue);
        }

        @Override
        public Object get() {
            return this.valueConfig.get();
        }

        @Override
        public void set(Object value) {
            this.valueConfig.set(value);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return valueConfig.getPath();
        }
    }

    public static class DoubleOption extends ConfigOption<Double> {

        public final ModConfigSpec.DoubleValue valueConfig;

        public DoubleOption(ModConfigSpec.Builder builder, String name, String description, double defaultValue, double min, double max) {
            super(builder, name, description);
            valueConfig = builder.defineInRange(name, defaultValue, min, max);
        }

        public Double get() {
            return valueConfig.get();
        }

        @Override
        public void set(Object value) {
            this.valueConfig.set((Double) value);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return valueConfig.getPath();
        }
    }

    public static class IntOption extends ConfigOption<Integer> {

        public final ModConfigSpec.IntValue valueConfig;

        public IntOption(ModConfigSpec.Builder builder, String name, String description, int defaultValue, int min, int max) {
            super(builder, name, description);
            valueConfig = builder.defineInRange(name, defaultValue, min, max);
        }

        public Integer get() {
            return valueConfig.get();
        }

        @Override
        public void set(Object value) {
            this.valueConfig.set((Integer) value);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return valueConfig.getPath();
        }
    }

    public static class BoolOption extends ConfigOption<Boolean> {

        public final ModConfigSpec.BooleanValue valueConfig;

        public BoolOption(ModConfigSpec.Builder builder, String name, String description, boolean defaultValue) {
            super(builder, name, description);
            valueConfig = builder.define(name, defaultValue);
        }

        public Boolean get() {
            return valueConfig.get();
        }

        @Override
        public void set(Object value) {
            this.valueConfig.set((Boolean) value);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return valueConfig.getPath();
        }
    }

    public static class StringOption extends ConfigOption<String> {

        public final ModConfigSpec.ConfigValue<String> valueConfig;

        public StringOption(ModConfigSpec.Builder builder, String name, String description, String defaultValue) {
            super(builder, name, description);
            valueConfig = builder.define(this.name, defaultValue);
        }

        public String get() {
            return valueConfig.get();
        }

        @Override
        public void set(Object value) {
            this.valueConfig.set((String) value);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return valueConfig.getPath();
        }
    }

    public static class StringListOption extends ConfigOption<List<? extends String>> {

        public final ModConfigSpec.ConfigValue<List<? extends String>> valueConfig;

        public StringListOption(ModConfigSpec.Builder builder, String name, String description, List<String> defaultValue) {
            super(builder, name, description);
            valueConfig = builder.defineList(this.name, defaultValue, o -> o instanceof String);
        }

        public List<? extends String> get() {
            return valueConfig.get();
        }

        @Override
        public void set(Object value) {
            this.valueConfig.set((List<? extends String>) value);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return valueConfig.getPath();
        }
    }

    public static class EnumOption<T extends Enum<T>> extends ConfigOption<T> {

        public final ModConfigSpec.ConfigValue<T> valueConfig;

        public EnumOption(ModConfigSpec.Builder builder, String name, String description, T defaultValue) {
            super(builder, name, description);
            valueConfig = builder.defineEnum(name, defaultValue);
        }

        @Override
        public T get() {
            return valueConfig.get();
        }

        @Override
        public void set(Object value) {
            this.valueConfig.set((T) value);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return valueConfig.getPath();
        }
    }
}
