package insane96mcp.insanelib.data.lootmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Global loot modifier that removes (or damages) items based on distance from world spawn.
 * Items have a higher chance of being purged the closer the loot generates to spawn, creating
 * a progressive difficulty curve where loot near spawn is worse.
 * <p>
 * The survival probability for an item at a given distance is linearly interpolated between
 * {@code (1 - multiplier_at_start)} at {@code start_range} and {@code 1.0} at {@code end_range}.
 * Beyond {@code end_range} items are never purged.
 * <p>
 * JSON example:
 * <pre>{@code
 * {
 *   "type": "insanelib:loot_purger",
 *   "conditions": [...],
 *   "end_range": 5000,
 *   "start_range": 0,
 *   "multiplier_at_start": 0.0,
 *   "apply_to_damageable": false,
 *   "blacklisted_items_tag": "insanelib:loot_purger_blacklist",
 *   "blacklisted_entity_type_tag": "insanelib:loot_purger_entity_blacklist"
 * }
 * }</pre>
 *
 * @see Builder
 */
public class LootPurgerModifier extends LootModifier {
    public static final MapCodec<LootPurgerModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(
                    inst.group(
                            Codec.INT.optionalFieldOf("start_range", 0).forGetter(m -> m.startRange),
                            Codec.INT.fieldOf("end_range").forGetter(m -> m.endRange),
                            Codec.FLOAT.optionalFieldOf("multiplier_at_start", 0f).forGetter(m -> m.multiplierAtStart),
                            Codec.BOOL.optionalFieldOf("apply_to_damageable", false).forGetter(m -> m.applyToDamageable),
                            TagKey.codec(Registries.ITEM).optionalFieldOf("blacklisted_items_tag").forGetter(m -> m.blacklistedItemsTag),
                            TagKey.codec(Registries.ENTITY_TYPE).optionalFieldOf("blacklisted_entity_type_tag").forGetter(m -> m.blacklistedEntityTypeTag)
                    )).apply(inst, LootPurgerModifier::new)
    );

    /** Distance from spawn at which purging begins. Items within this radius are always purged at {@code multiplier_at_start} rate. Default: {@code 0}. */
    private int startRange = 0;
    /** Distance from spawn beyond which no purging occurs. */
    private int endRange;
    /** Survival chance multiplier at {@code start_range}. {@code 0} means all items are removed; {@code 1} means nothing is removed. Default: {@code 0}. */
    private float multiplierAtStart = 0f;
    /** If {@code true}, damageable items are damaged proportionally instead of being removed. */
    private boolean applyToDamageable = false;
    /** Items in this tag are never purged or damaged. */
    private Optional<TagKey<Item>> blacklistedItemsTag;
    /** Entity types in this tag are never affected by this modifier. */
    private Optional<TagKey<EntityType<?>>> blacklistedEntityTypeTag;

    public LootPurgerModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn, DEFAULT_PRIORITY);
    }

    public LootPurgerModifier(LootItemCondition[] conditionsIn, int priority, int startRange, int endRange, float multiplierAtStart, boolean applyToDamageable, Optional<TagKey<Item>> blacklistedItemsTag, Optional<TagKey<EntityType<?>>> blacklistedEntityTypeTag) {
        super(conditionsIn, priority);
        this.startRange = startRange;
        this.endRange = endRange;
        this.multiplierAtStart = multiplierAtStart;
        this.applyToDamageable = applyToDamageable;
        this.blacklistedItemsTag = blacklistedItemsTag;
        this.blacklistedEntityTypeTag = blacklistedEntityTypeTag;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        if (context.getOptionalParameter(LootContextParams.ORIGIN) == null)
            return generatedLoot;
        if (context.getOptionalParameter(LootContextParams.THIS_ENTITY) != null
                && blacklistedEntityTypeTag.isPresent()
                && context.getParameter(LootContextParams.THIS_ENTITY).getType().builtInRegistryHolder().is(blacklistedEntityTypeTag.get())) {
            return generatedLoot;
        }

        int spawnX = context.getLevel().getLevelData().getRespawnData().pos().getX();
        int spawnZ = context.getLevel().getLevelData().getRespawnData().pos().getZ();
        int x = (int) context.getParameter(LootContextParams.ORIGIN).x;
        int z = (int) context.getParameter(LootContextParams.ORIGIN).z;
        int distanceFromSpawn = (int) Math.sqrt((x - spawnX) * (x - spawnX) + (z - spawnZ) * (z - spawnZ));
        int distanceFromStart = distanceFromSpawn - this.startRange;
        float multiplier;
        if (distanceFromStart <= 0)
            multiplier = 1f - this.multiplierAtStart;
        else
            multiplier = (this.endRange - distanceFromStart) / ((float) this.endRange - this.startRange) * (1f - this.multiplierAtStart);
        generatedLoot.removeIf(itemStack -> {
            if (blacklistedItemsTag.isPresent() && itemStack.is(blacklistedItemsTag.get()))
                return false;
            return context.getRandom().nextDouble() < multiplier;
        });
        if (this.applyToDamageable) {
            generatedLoot.forEach(itemStack -> {
                if (itemStack.isDamageableItem())
                    itemStack.setDamageValue((int) (itemStack.getMaxDamage() - ((itemStack.getMaxDamage() - itemStack.getDamageValue()) * (1f - multiplier))));
            });
        }
        return generatedLoot;
    }

    @Override
    @NotNull
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    public static class Builder {
        final LootPurgerModifier lootPurgerModifier;

        public Builder(LootItemCondition[] conditionsIn, int endRange) {
            this.lootPurgerModifier = new LootPurgerModifier(conditionsIn);
            this.lootPurgerModifier.endRange = endRange;
        }

        public Builder(Identifier lootTable, int endRange) {
            this(new LootItemCondition[]{LootTableIdCondition.builder(lootTable).build()}, endRange);
        }

        public Builder setStartRange(int startRange) {
            this.lootPurgerModifier.startRange = startRange;
            return this;
        }

        public Builder setMultiplierAtStart(float multiplierAtStart) {
            this.lootPurgerModifier.multiplierAtStart = multiplierAtStart;
            return this;
        }

        public Builder applyToDamageable() {
            this.lootPurgerModifier.applyToDamageable = true;
            return this;
        }

        public Builder blacklistedItemTag(TagKey<Item> tag) {
            this.lootPurgerModifier.blacklistedItemsTag = Optional.of(tag);
            return this;
        }

        public LootPurgerModifier build() {
            return this.lootPurgerModifier;
        }
    }
}
