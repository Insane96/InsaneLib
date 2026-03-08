package insane96mcp.insanelib.data.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.setup.ILConditions;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

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
public record KillerHasAdvancementCondition(ResourceLocation advancement) implements LootItemCondition {

    public static final MapCodec<KillerHasAdvancementCondition> CODEC = ResourceLocation.CODEC
            .fieldOf("advancement")
            .xmap(KillerHasAdvancementCondition::new, KillerHasAdvancementCondition::advancement);

    @Override
    public LootItemConditionType getType() {
        return ILConditions.KILLER_HAS_ADVANCEMENT.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    @Override
    public boolean test(LootContext context) {
        if (!context.hasParam(LootContextParams.LAST_DAMAGE_PLAYER))
            return false;
        ServerPlayer player = (ServerPlayer) context.getParam(LootContextParams.LAST_DAMAGE_PLAYER);
        return MCUtils.isAdvancementDone(player, this.advancement);
    }

    public static LootItemCondition.Builder advancementCompleted(ResourceLocation advancement) {
        return () -> new KillerHasAdvancementCondition(advancement);
    }
}
