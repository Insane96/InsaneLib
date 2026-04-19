package insane96mcp.insanelib.data.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BlockBrokenTrigger extends SimpleCriterionTrigger<BlockBrokenTrigger.TriggerInstance> {

    @Override
    @NotNull
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockState state) {
        this.trigger(player, instance -> instance.matches(state));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<TagKey<Block>> tag
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                TagKey.codec(Registries.BLOCK).optionalFieldOf("tag").forGetter(TriggerInstance::tag)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(BlockState state) {
            return tag.isEmpty() || state.is(tag.get());
        }
    }
}
