package insane96mcp.insanelib.data.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

/**
 * Loot condition that passes if the player who dealt the killing blow has completed the specified advancement.
 * Fails if no {@link LootContextParams#LAST_DAMAGE_PLAYER} is present in the context.
 * <p>
 * JSON example:
 * <pre>{@code
 * { "condition": "insanelib:killer_has_advancement", "advancement": "minecraft:story/mine_diamond" }
 * }</pre>
 *
 * @param advancement The resource location of the advancement to check.
 */
public record KillerHasAdvancementCondition(Identifier advancement) implements LootItemCondition {

    public static final MapCodec<KillerHasAdvancementCondition> MAP_CODEC = Identifier.CODEC
            .fieldOf("advancement")
            .xmap(KillerHasAdvancementCondition::new, KillerHasAdvancementCondition::advancement);

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    @Override
    public boolean test(LootContext context) {
        if (!context.hasParameter(LootContextParams.LAST_DAMAGE_PLAYER))
            return false;
        ServerPlayer player = (ServerPlayer) context.getParameter(LootContextParams.LAST_DAMAGE_PLAYER);
        return MCUtils.isAdvancementDone(player, this.advancement);
    }

    public static LootItemCondition.Builder advancementCompleted(Identifier advancement) {
        return () -> new KillerHasAdvancementCondition(advancement);
    }
}
