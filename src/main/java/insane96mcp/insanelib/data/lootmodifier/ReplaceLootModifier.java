package insane96mcp.insanelib.data.lootmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanelib.util.MathHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Global loot modifier that replaces occurrences of one item with another in the generated loot.
 * <p>
 * Supports fortune-dependent chance and count multipliers, partial replacement via {@code amount_to_replace},
 * durability preservation, and restriction to chest loot tables.
 * <p>
 * JSON example:
 * <pre>{@code
 * {
 *   "type": "insanelib:replace_loot",
 *   "conditions": [...],
 *   "item_to_replace": "minecraft:iron_sword",
 *   "new_item": "minecraft:diamond_sword",
 *   "amount_to_replace": -1,
 *   "chances": [0.5, 0.75, 1.0],
 *   "multipliers": [1.0, 1.5, 2.0],
 *   "keep_durability": false,
 *   "chests_only": false
 * }
 * }</pre>
 *
 * @see Builder
 */
public class ReplaceLootModifier extends LootModifier {
    public static final MapCodec<ReplaceLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(
                    inst.group(
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item_to_replace").forGetter(m -> m.itemToReplace),
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("new_item").forGetter(m -> m.newItem),
                            Codec.INT.optionalFieldOf("amount_to_replace", -1).forGetter(m -> m.amountToReplace),
                            Codec.list(Codec.FLOAT).optionalFieldOf("chances", List.of(1f)).forGetter(m -> m.chances),
                            Codec.list(Codec.FLOAT).optionalFieldOf("multipliers", List.of(1f)).forGetter(m -> m.multipliers),
                            Codec.BOOL.optionalFieldOf("keep_durability", false).forGetter(m -> m.keepDurability),
                            Codec.BOOL.optionalFieldOf("chests_only", false).forGetter(m -> m.chestsOnly)
                    )).apply(inst, ReplaceLootModifier::new)
    );

    /** Item to replace. */
    private final Item itemToReplace;
    /** Item to replace with. */
    private final Item newItem;
    /** Number of items to replace. {@code -1} replaces the entire stack. */
    private int amountToReplace;
    /**
     * Per-fortune-level replacement chances. Index 0 = no fortune, index 1 = Fortune I, etc.
     * The last element is used for all higher fortune levels.
     */
    private List<Float> chances;
    /**
     * Per-fortune-level count multipliers applied after replacement. Index 0 = no fortune, index 1 = Fortune I, etc.
     * The last element is used for all higher fortune levels.
     */
    private List<Float> multipliers;
    /**
     * If {@code true}, the durability percentage of the original item is transferred to the replacement.
     * Only effective when {@code amountToReplace} is {@code -1} and both items are damageable.
     */
    private boolean keepDurability;
    /** If {@code true}, only applies to loot tables whose path contains {@code "chests/"}. */
    private boolean chestsOnly;

    public ReplaceLootModifier(LootItemCondition[] conditionsIn, Item itemToReplace, Item newItem) {
        this(conditionsIn, itemToReplace, newItem, -1, List.of(1f), List.of(1f), false, false);
    }

    public ReplaceLootModifier(LootItemCondition[] conditionsIn, Item itemToReplace, Item newItem, int amountToReplace, List<Float> chances, List<Float> multipliers, boolean keepDurability, boolean chestsOnly) {
        super(conditionsIn);
        this.itemToReplace = itemToReplace;
        this.newItem = newItem;
        this.amountToReplace = amountToReplace;
        this.chances = chances;
        this.multipliers = multipliers;
        this.keepDurability = keepDurability;
        this.chestsOnly = chestsOnly;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        if (this.chestsOnly && !context.getQueriedLootTableId().getPath().contains("chests/"))
            return generatedLoot;

        Holder<Enchantment> fortune = context.getLevel().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FORTUNE);

        List<ItemStack> toRemove = new ArrayList<>();
        List<ItemStack> toAdd = new ArrayList<>();
        generatedLoot.stream().filter(stack -> stack.getItem().equals(itemToReplace))
                .forEach(stack -> {
                    ItemStack toolStack = context.getParamOrNull(LootContextParams.TOOL);
                    int fortuneLvl = toolStack != null ? toolStack.getEnchantmentLevel(fortune) : 0;
                    float chance = this.chances.get(Math.min(fortuneLvl, this.chances.size() - 1));
                    if (context.getRandom().nextDouble() >= chance)
                        return;

                    float multiplier = this.multipliers.get(Math.min(fortuneLvl, this.multipliers.size() - 1));
                    boolean keepDurability = this.keepDurability && itemToReplace.components().has(DataComponents.MAX_DAMAGE) && newItem.components().has(DataComponents.MAX_DAMAGE);
                    float percentageDurability = keepDurability
                            ? (float) stack.getDamageValue() / stack.getMaxDamage()
                            : 0f;

                    toRemove.add(stack);
                    if (amountToReplace == -1) {
                        int newAmount = MathHelper.getAmountWithDecimalChance(context.getRandom(), stack.getCount() * multiplier);
                        ItemStack newStack = new ItemStack(newItem, newAmount);
                        copyComponents(stack, newStack, keepDurability, percentageDurability);
                        toAdd.add(newStack);
                    } else {
                        int newAmount = MathHelper.getAmountWithDecimalChance(context.getRandom(), Math.min(stack.getCount(), amountToReplace) * multiplier);
                        ItemStack newStack = new ItemStack(newItem, newAmount);
                        copyComponents(stack, newStack, false, 0f);
                        toAdd.add(newStack);
                        if (amountToReplace < stack.getCount())
                            toAdd.add(new ItemStack(itemToReplace, stack.getCount() - amountToReplace));
                    }
                });

        generatedLoot.removeAll(toRemove);
        generatedLoot.addAll(toAdd);
        return generatedLoot;
    }

    /**
     * Copies the component patch from {@code source} to {@code dest}, excluding damage.
     * If {@code keepDurability} is true, sets the damage on {@code dest} proportionally.
     */
    private static void copyComponents(ItemStack source, ItemStack dest, boolean keepDurability, float percentageDurability) {
        dest.applyComponents(source.getComponentsPatch());
        if (keepDurability)
            dest.setDamageValue((int) (dest.getMaxDamage() * percentageDurability));
        else
            dest.remove(DataComponents.DAMAGE);
    }

    @Override
    @NotNull
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    public static class Builder {
        final ReplaceLootModifier replaceLootModifier;

        public Builder(Item itemToReplace, Item newItem) {
            this(new LootItemCondition[0], itemToReplace, newItem);
        }

        public Builder(LootItemCondition[] conditionsIn, Item itemToReplace, Item newItem) {
            replaceLootModifier = new ReplaceLootModifier(conditionsIn, itemToReplace, newItem);
        }

        public Builder(EntityType<?> entityType, Item itemToReplace, Item newItem) {
            replaceLootModifier = new ReplaceLootModifier(
                    new LootItemCondition[]{LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, new EntityPredicate.Builder().entityType(EntityTypePredicate.of(entityType)).build()).build()},
                    itemToReplace, newItem);
        }

        public Builder setAmountToReplace(int amount) {
            replaceLootModifier.amountToReplace = amount;
            return this;
        }

        public Builder setChances(List<Float> chances) {
            replaceLootModifier.chances = chances;
            return this;
        }

        public Builder setMultipliers(List<Float> multipliers) {
            replaceLootModifier.multipliers = multipliers;
            return this;
        }

        public Builder applyToChestsOnly() {
            replaceLootModifier.chestsOnly = true;
            return this;
        }

        public Builder keepDurability() {
            replaceLootModifier.keepDurability = true;
            return this;
        }

        public ReplaceLootModifier build() {
            return replaceLootModifier;
        }
    }
}