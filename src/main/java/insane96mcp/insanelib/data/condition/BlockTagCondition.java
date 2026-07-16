package insane96mcp.insanelib.data.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

/**
 * Loot condition that passes if the block being broken is in the specified block tag.
 * If no {@link LootContextParams#BLOCK_STATE} is present in the context (e.g. entity loot), the condition returns {@code true}.
 * <p>
 * JSON example:
 * <pre>{@code
 * { "condition": "insanelib:block_tag_match", "block_tag": "minecraft:logs" }
 * }</pre>
 *
 * @param blockTag The block tag to match against.
 */
public record BlockTagCondition(TagKey<Block> blockTag) implements LootItemCondition {

    public static final MapCodec<BlockTagCondition> MAP_CODEC = TagKey.codec(Registries.BLOCK)
            .fieldOf("block_tag")
            .xmap(BlockTagCondition::new, BlockTagCondition::blockTag);

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    @Override
    public boolean test(LootContext context) {
        if (context.hasParameter(LootContextParams.BLOCK_STATE))
            return context.getParameter(LootContextParams.BLOCK_STATE).is(this.blockTag);
        return true;
    }

    public static LootItemCondition.Builder builder(Identifier blockTag) {
        return () -> new BlockTagCondition(TagKey.create(Registries.BLOCK, blockTag));
    }
}
