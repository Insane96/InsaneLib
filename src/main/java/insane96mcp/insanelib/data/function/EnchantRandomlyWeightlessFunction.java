package insane96mcp.insanelib.data.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.ArrayList;
import java.util.List;

/**
 * Loot function that applies a given number of random enchantments to an item, each chosen with equal
 * probability regardless of enchantment weight (unlike vanilla's enchanting table). Incompatible
 * enchantments are never applied together. Supports books.
 * <p>
 * JSON example:
 * <pre>{@code
 * {
 *   "function": "insanelib:enchant_randomly_weightless",
 *   "conditions": [...],
 *   "count": 2,
 *   "max_lvl": false,
 *   "treasure": false
 * }
 * }</pre>
 */
public class EnchantRandomlyWeightlessFunction extends LootItemConditionalFunction {

    public static final MapCodec<EnchantRandomlyWeightlessFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).and(
                    inst.group(
                            NumberProviders.CODEC.fieldOf("count").forGetter(f -> f.count),
                            Codec.BOOL.optionalFieldOf("max_lvl", false).forGetter(f -> f.maxLvl),
                            Codec.BOOL.optionalFieldOf("treasure", false).forGetter(f -> f.treasure)
                    )
            ).apply(inst, EnchantRandomlyWeightlessFunction::new)
    );

    /** Number of enchantments to apply. Supports all number providers (constant, uniform, etc.). */
    final NumberProvider count;
    /** If {@code true}, all enchantments are applied at their maximum level. */
    final boolean maxLvl;
    /** If {@code true}, treasure enchantments (tag {@code minecraft:treasure}) are included in the pool. */
    final boolean treasure;

    protected EnchantRandomlyWeightlessFunction(List<LootItemCondition> conditions, NumberProvider count, boolean maxLvl, boolean treasure) {
        super(conditions);
        this.count = count;
        this.maxLvl = maxLvl;
        this.treasure = treasure;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        int amount = this.count.getInt(context);
        HolderLookup.RegistryLookup<Enchantment> lookup = context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<Holder.Reference<Enchantment>> eligibleEnchantments = new ArrayList<>(lookup.listElements()
                .filter(holder -> stack.supportsEnchantment(holder)
                        && (!holder.is(EnchantmentTags.TREASURE) || this.treasure))
                .toList());

        if (eligibleEnchantments.isEmpty())
            return stack;

        List<Holder.Reference<Enchantment>> applied = new ArrayList<>();
        ItemStack newStack = stack.copy();
        for (int i = 0; i < amount; i++) {
            List<Holder.Reference<Enchantment>> compatible = eligibleEnchantments.stream()
                    .filter(e -> applied.stream().allMatch(a -> Enchantment.areCompatible(e, a)))
                    .toList();
            if (compatible.isEmpty())
                break;

            Holder.Reference<Enchantment> chosen = compatible.get(random.nextInt(compatible.size()));
            newStack = applyEnchantment(newStack, chosen, random);
            applied.add(chosen);
            eligibleEnchantments.remove(chosen);
            if (eligibleEnchantments.isEmpty())
                break;
        }
        return newStack;
    }

    private ItemStack applyEnchantment(ItemStack stack, Holder<Enchantment> enchantment, RandomSource random) {
        int lvl = Mth.nextInt(random, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel());
        if (this.maxLvl)
            lvl = enchantment.value().getMaxLevel();
        if (stack.is(Items.BOOK))
            stack = new ItemStack(Items.ENCHANTED_BOOK);
        if (stack.is(Items.ENCHANTED_BOOK)) {
            final int finalLvl = lvl;
            stack.update(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY, stored -> {
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(stored);
                mutable.set(enchantment, finalLvl);
                return mutable.toImmutable();
            });
        } else {
            stack.enchant(enchantment, lvl);
        }
        return stack;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return MAP_CODEC;
    }
}
