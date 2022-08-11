package insane96mcp.insanelib.config;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.common.ForgeConfigSpec;

public class MinMax {
    public double min, max;

    public MinMax(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public MinMax(double value) {
        this.min = value;
        this.max = value;
    }

    /**
     * Returns a random number between min (included) and max (excluded)
     */
    public double getRandBetween(RandomSource random) {
        return Mth.nextDouble(random, this.min, this.max - 1);
    }

    /**
     * Returns an integer random number between min (included) and max (excluded)
     */
    public int getIntRandBetween(RandomSource random) {
        return Mth.nextInt(random, (int) this.min, (int) this.max - 1);
    }

    public static class Config {
        private final ForgeConfigSpec.Builder builder;

        private ForgeConfigSpec.ConfigValue<Double> minConfig;
        private ForgeConfigSpec.ConfigValue<Double> maxConfig;

        public Config(ForgeConfigSpec.Builder builder, String optionName, String description) {
            this.builder = builder;
            builder.comment(description).push(optionName);
        }

        public Config setMin(double rangeMin, double rangeMax, double defaultValue) {
            minConfig = builder.defineInRange("Minimum", defaultValue, rangeMin, rangeMax);
            return this;
        }

        public Config setMax(double rangeMin, double rangeMax, double defaultValue) {
            maxConfig = builder.defineInRange("Maximum", defaultValue, rangeMin, rangeMax);
            return this;
        }

        public Config setMinMax(double rangeMin, double rangeMax, MinMax defaultValue) {
            minConfig = builder.defineInRange("Minimum", defaultValue.min, rangeMin, rangeMax);
            maxConfig = builder.defineInRange("Maximum", defaultValue.max, rangeMin, rangeMax);
            return this;
        }

        public Config build() {
            builder.pop();
            return this;
        }

        public MinMax get() {
            return new MinMax(minConfig.get(), maxConfig.get());
        }
    }
}
