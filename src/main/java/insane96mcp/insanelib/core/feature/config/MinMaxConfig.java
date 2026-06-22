package insane96mcp.insanelib.core.feature.config;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.List;

public class MinMaxConfig {
    public double min, max;

    public MinMaxConfig(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public MinMaxConfig(double value) {
        this.min = value;
        this.max = value;
    }

    /**
     * Returns a random number between min (included) and max (excluded)
     */
    public double getRandBetween(RandomSource random) {
        return Mth.nextDouble(random, this.min, this.max);
    }

    /**
     * Returns a random integer number between min and max (included)
     */
    public int getIntRandBetween(RandomSource random) {
        return Mth.nextInt(random, (int) this.min, (int) this.max);
    }

    public boolean isZero() {
        return this.min == 0 && this.max == 0;
    }

    public static class COption extends ConfigOption<MinMaxConfig> {

        private final ModConfigSpec.DoubleValue minConfig;
        private final ModConfigSpec.DoubleValue maxConfig;

        public COption(ModConfigSpec.Builder builder, String name, String description, MinMaxConfig defaultValue, double rangeMin, double rangeMax) {
            super(builder, name, description);
            List<String> split = ConfigUtils.split(name);
            builder.push(split);
            minConfig = builder.defineInRange("Minimum", defaultValue.min, rangeMin, rangeMax);
            maxConfig = builder.defineInRange("Maximum", defaultValue.max, rangeMin, rangeMax);
            builder.pop(split.size());
        }

        @Override
        public MinMaxConfig get() {
            return new MinMaxConfig(minConfig.get(), maxConfig.get());
        }

        @Override
        public void set(Object value) {
            MinMaxConfig minMaxConfig = (MinMaxConfig) value;
            this.minConfig.set(minMaxConfig.min);
            this.maxConfig.set(minMaxConfig.max);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return null;
        }
    }
}
