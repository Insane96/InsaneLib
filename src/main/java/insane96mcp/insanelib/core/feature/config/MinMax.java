package insane96mcp.insanelib.core.feature.config;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.List;

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
        return Mth.nextDouble(random, this.min, this.max);
    }

    /**
     * Returns a random integer number between min and max (included)
     */
    public int getIntRandBetween(RandomSource random) {
        return Mth.nextInt(random, (int) this.min, (int) this.max);
    }

    public static class Config extends ConfigOption<MinMax> {

        private final ModConfigSpec.DoubleValue minConfig;
        private final ModConfigSpec.DoubleValue maxConfig;

        public Config(ModConfigSpec.Builder builder, String name, String description, MinMax defaultValue, double rangeMin, double rangeMax) {
            super(builder, name, description);
            List<String> split = ConfigUtils.split(name);
            builder.push(split);
            minConfig = builder.defineInRange("Minimum", defaultValue.min, rangeMin, rangeMax);
            maxConfig = builder.defineInRange("Maximum", defaultValue.max, rangeMin, rangeMax);
            builder.pop(split.size());
        }

        @Override
        public MinMax get() {
            return new MinMax(minConfig.get(), maxConfig.get());
        }

        @Override
        public void set(Object value) {
            MinMax minMax = (MinMax) value;
            this.minConfig.set(minMax.min);
            this.maxConfig.set(minMax.max);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return null;
        }
    }
}
