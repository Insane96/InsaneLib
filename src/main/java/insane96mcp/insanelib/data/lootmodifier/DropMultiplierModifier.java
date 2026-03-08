package insane96mcp.insanelib.data.lootmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanelib.util.MathHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Global loot modifier that multiplies the count of matching items in the generated loot.
 * Matching can be done by item, item tag, or both. Stacks of the same item are merged before
 * the multiplier is applied, and fractional results are resolved probabilistically.
 * <p>
 * JSON example:
 * <pre>{@code
 * {
 *   "type": "insanelib:drop_multiplier",
 *   "conditions": [...],
 *   "item": "minecraft:wheat",
 *   "tag": "minecraft:crops",
 *   "multiplier": 2.0,
 *   "amount_to_keep": 0,
 *   "ignore_unstackable": true
 * }
 * }</pre>
 *
 * @see Builder
 */
public class DropMultiplierModifier extends LootModifier {

    public static final MapCodec<DropMultiplierModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(
                    inst.group(
                            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item").forGetter(m -> m.item),
                            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(m -> m.tag),
                            Codec.floatRange(0f, 1024f).fieldOf("multiplier").forGetter(m -> m.multiplier),
                            Codec.intRange(0, 256).optionalFieldOf("amount_to_keep", 0).forGetter(m -> m.amountToKeep),
                            Codec.BOOL.optionalFieldOf("ignore_unstackable", true).forGetter(m -> m.ignoreUnstackable)
                    )).apply(inst, DropMultiplierModifier::new)
    );

    /** If present, only stacks of this item are multiplied. */
    private Optional<Item> item;
    /** If present, only stacks matching this tag are multiplied. */
    private Optional<TagKey<Item>> tag;
    /** Multiplier applied to the matched item count. */
    private final float multiplier;
    /** Amount subtracted from the stack count before applying the multiplier (i.e. always kept as-is). */
    private int amountToKeep;
    /** If {@code true}, non-stackable items are skipped. */
    private boolean ignoreUnstackable;

    public DropMultiplierModifier(LootItemCondition[] conditionsIn, Optional<Item> item, Optional<TagKey<Item>> tag, float multiplier, int amountToKeep, boolean ignoreUnstackable) {
        super(conditionsIn);
        this.item = item;
        this.tag = tag;
        this.multiplier = multiplier;
        this.amountToKeep = amountToKeep;
        this.ignoreUnstackable = ignoreUnstackable;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        if (multiplier == 1)
            return generatedLoot;
        Predicate<ItemStack> MATCHES_ITEM_OR_TAG = stack -> {
            if (item.isPresent())
                return stack.is(item.get());
            else if (tag.isPresent())
                return stack.is(tag.get());
            return true;
        };
        List<ItemStack> filteredStacks = generatedLoot.stream().filter(MATCHES_ITEM_OR_TAG).toList();
        if (filteredStacks.isEmpty())
            return generatedLoot;

        List<ItemStack> newStacks = new ArrayList<>();
        for (ItemStack stack : filteredStacks) {
            if (!stack.isStackable() && this.ignoreUnstackable)
                continue;
            ItemStack existingStack = null;
            for (ItemStack newStack : newStacks) {
                if (ItemStack.isSameItemSameComponents(stack, newStack)) {
                    existingStack = newStack;
                }
            }
            if (existingStack == null) {
                existingStack = stack.copy();
                newStacks.add(existingStack);
            } else {
                existingStack.setCount(existingStack.getCount() + stack.getCount());
            }
        }
        generatedLoot.removeIf(MATCHES_ITEM_OR_TAG);
        for (ItemStack newStack : newStacks) {
            int count = MathHelper.getAmountWithDecimalChance(context.getRandom(), (newStack.getCount() - amountToKeep) * multiplier) + amountToKeep;
            if (count > 0) {
                newStack.setCount(count);
                generatedLoot.add(newStack);
            }
        }
        //TODO Fix unstacking stuff

        return generatedLoot;
    }

    public static DropMultiplierModifier newItem(LootItemCondition[] conditionsIn, Optional<Item> item, float multiplier) {
        return new DropMultiplierModifier(conditionsIn, item, Optional.empty(), multiplier, 0, true);
    }

    public static DropMultiplierModifier newTag(LootItemCondition[] conditionsIn, Optional<TagKey<Item>> tag, float multiplier) {
        return new DropMultiplierModifier(conditionsIn, Optional.empty(), tag, multiplier, 0, true);
    }

    @Override
    @NotNull
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    public static class Builder {
        final DropMultiplierModifier dropMultiplierModifier;

        public Builder(LootItemCondition[] conditionsIn, Item item, float multiplier) {
            this.dropMultiplierModifier = DropMultiplierModifier.newItem(conditionsIn, Optional.of(item), multiplier);
        }

        public Builder(LootItemCondition[] conditionsIn, TagKey<Item> tag, float multiplier) {
            this.dropMultiplierModifier = DropMultiplierModifier.newTag(conditionsIn, Optional.of(tag), multiplier);
        }

        public Builder(Item item, float multiplier) {
            this.dropMultiplierModifier = DropMultiplierModifier.newItem(new LootItemCondition[0], Optional.of(item), multiplier);
        }

        public Builder(TagKey<Item> tag, float multiplier) {
            this.dropMultiplierModifier = DropMultiplierModifier.newTag(new LootItemCondition[0], Optional.of(tag), multiplier);
        }

        public Builder(EntityType<?> entityType, Item item, float multiplier) {
            this.dropMultiplierModifier = DropMultiplierModifier.newItem(new LootItemCondition[]{LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, new EntityPredicate.Builder().entityType(EntityTypePredicate.of(entityType)).build()).build()}, Optional.of(item), multiplier);
        }

        public Builder(EntityType<?> entityType, TagKey<Item> tag, float multiplier) {
            this.dropMultiplierModifier = DropMultiplierModifier.newTag(new LootItemCondition[]{LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, new EntityPredicate.Builder().entityType(EntityTypePredicate.of(entityType)).build()).build()}, Optional.of(tag), multiplier);
        }

        public Builder(Block block, Item item, float multiplier) {
            this.dropMultiplierModifier = DropMultiplierModifier.newItem(new LootItemCondition[]{new LootItemBlockStatePropertyCondition.Builder(block).build()}, Optional.of(item), multiplier);
        }

        public Builder(Block block, TagKey<Item> tag, float multiplier) {
            this.dropMultiplierModifier = DropMultiplierModifier.newTag(new LootItemCondition[]{new LootItemBlockStatePropertyCondition.Builder(block).build()}, Optional.of(tag), multiplier);
        }

        public Builder keepAmount(int amount) {
            this.dropMultiplierModifier.amountToKeep = amount;
            return this;
        }

        public Builder ignoreUnstackable(boolean ignoreUnstackable) {
            this.dropMultiplierModifier.ignoreUnstackable = ignoreUnstackable;
            return this;
        }

        public DropMultiplierModifier build() {
            return this.dropMultiplierModifier;
        }
    }
}
