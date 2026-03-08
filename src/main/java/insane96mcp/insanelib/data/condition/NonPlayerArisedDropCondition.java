package insane96mcp.insanelib.data.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.setup.ILConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

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

    public static final MapCodec<NonPlayerArisedDropCondition> CODEC = MapCodec.unit(NonPlayerArisedDropCondition::new);

    @Override
    public LootItemConditionType getType() {
        return ILConditions.NON_PLAYER_ARISED_DROP.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY, LootContextParams.LAST_DAMAGE_PLAYER, LootContextParams.EXPLOSION_RADIUS, LootContextParams.TOOL);
    }

    @Override
    public boolean test(LootContext context) {
        if (!(context.hasParam(LootContextParams.THIS_ENTITY))
                || !(context.getParam(LootContextParams.THIS_ENTITY) instanceof LivingEntity)
                || context.getParam(LootContextParams.THIS_ENTITY) instanceof Player
                || context.hasParam(LootContextParams.EXPLOSION_RADIUS)
                || context.hasParam(LootContextParams.TOOL))
            return false;

        return !context.hasParam(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public static LootItemCondition.Builder builder() {
        return NonPlayerArisedDropCondition::new;
    }
}
