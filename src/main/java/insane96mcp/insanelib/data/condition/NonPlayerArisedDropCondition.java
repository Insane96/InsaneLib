package insane96mcp.insanelib.data.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

/**
 * Loot condition that passes if the drop was caused by a natural (non-player, non-explosion, non-tool) mob death.
 * Specifically, it requires:
 * <ul>
 *   <li>A {@link LootContextParams#THIS_ENTITY} that is a {@link net.minecraft.world.entity.LivingEntity} but not a {@link net.minecraft.world.entity.player.Player}</li>
 *   <li>No {@link LootContextParams#EXPLOSION_RADIUS} in the context</li>
 *   <li>No {@link LootContextParams#TOOL} in the context</li>
 *   <li>No {@link LootContextParams#LAST_DAMAGE_PLAYER} in the context</li>
 * </ul>
 * <p>
 * JSON example:
 * <pre>{@code
 * { "condition": "insanelib:non_player_arised_drop" }
 * }</pre>
 */
public record NonPlayerArisedDropCondition() implements LootItemCondition {

    public static final MapCodec<NonPlayerArisedDropCondition> MAP_CODEC = MapCodec.unit(NonPlayerArisedDropCondition::new);

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY, LootContextParams.LAST_DAMAGE_PLAYER, LootContextParams.EXPLOSION_RADIUS, LootContextParams.TOOL);
    }

    @Override
    public boolean test(LootContext context) {
        if (!(context.hasParameter(LootContextParams.THIS_ENTITY))
                || !(context.getParameter(LootContextParams.THIS_ENTITY) instanceof LivingEntity)
                || context.getParameter(LootContextParams.THIS_ENTITY) instanceof Player
                || context.hasParameter(LootContextParams.EXPLOSION_RADIUS)
                || context.hasParameter(LootContextParams.TOOL))
            return false;

        return !context.hasParameter(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public static LootItemCondition.Builder builder() {
        return NonPlayerArisedDropCondition::new;
    }
}
