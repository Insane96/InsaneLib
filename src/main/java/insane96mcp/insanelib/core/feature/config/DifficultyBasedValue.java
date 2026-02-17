package insane96mcp.insanelib.core.feature.config;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.List;

public class DifficultyBasedValue {
    public double easy, normal, hard;

    public DifficultyBasedValue(double easy, double normal, double hard) {
        this.easy = easy;
        this.normal = normal;
        this.hard = hard;
    }

    public double getByDifficulty(Level level) {
        return switch (level.getDifficulty()) {
            case PEACEFUL, EASY -> this.easy;
            case NORMAL -> this.normal;
            case HARD -> this.hard;
        };
    }

    public static class Config extends ConfigOption<DifficultyBasedValue> {

        private final ModConfigSpec.DoubleValue easyConfig;
        private final ModConfigSpec.DoubleValue normalConfig;
        private final ModConfigSpec.DoubleValue hardConfig;

        public Config(ModConfigSpec.Builder builder, String name, String description, DifficultyBasedValue defaultValue, double rangeMin, double rangeMax) {
            super(builder, name, description);
            List<String> split = ConfigUtils.split(name);
            builder.push(split);
            this.easyConfig = builder.defineInRange("Easy/Peaceful", defaultValue.easy, rangeMin, rangeMax);
            this.normalConfig = builder.defineInRange("Normal", defaultValue.normal, rangeMin, rangeMax);
            this.hardConfig = builder.defineInRange("Hard", defaultValue.hard, rangeMin, rangeMax);
            builder.pop(split.size());
        }

        @Override
        public DifficultyBasedValue get() {
            return new DifficultyBasedValue(easyConfig.get(), normalConfig.get(), hardConfig.get());
        }

        @Override
        public void set(Object value) {
            DifficultyBasedValue difficultyBasedValue = (DifficultyBasedValue) value;
            this.easyConfig.set(difficultyBasedValue.easy);
            this.normalConfig.set(difficultyBasedValue.normal);
            this.hardConfig.set(difficultyBasedValue.hard);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return null;
        }
    }
}
