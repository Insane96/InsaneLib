package insane96mcp.insanelib.config;

import insane96mcp.insanelib.base.config.ConfigOpt;
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

    public static class Config extends ConfigOpt<MinMax> {

        private ForgeConfigSpec.DoubleValue minConfig;
        private ForgeConfigSpec.DoubleValue maxConfig;

        public Config(ForgeConfigSpec.Builder builder, String name, String description, MinMax defaultValue, double rangeMin, double rangeMax) {
            super(builder, name, description);
            builder.push(name);
            minConfig = builder.defineInRange("Minimum", defaultValue.min, rangeMin, rangeMax);
            maxConfig = builder.defineInRange("Maximum", defaultValue.max, rangeMin, rangeMax);
            builder.pop();
        }

        @Override
        public MinMax get() {
            return new MinMax(minConfig.get(), maxConfig.get());
        }
    }
}
