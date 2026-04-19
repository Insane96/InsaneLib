package insane96mcp.insanelib.data.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanelib.setup.ILLootFunctions;
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
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * Loot function that applies a single random treasure enchantment (tag {@code minecraft:treasure})
 * to the item. Supports books.
 * <p>
 * JSON example:
 * <pre>{@code
 * {
 *   "function": "insanelib:enchant_with_treasure",
 *   "conditions": [...],
 *   "allow_curses": true,
 *   "allow_non_curse_treasure": true
 * }
 * }</pre>
 */
public class EnchantWithTreasureFunction extends LootItemConditionalFunction {

    public static final MapCodec<EnchantWithTreasureFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).and(
                    inst.group(
                            Codec.BOOL.optionalFieldOf("allow_curses", true).forGetter(f -> f.allowCurses),
                            Codec.BOOL.optionalFieldOf("allow_non_curse_treasure", true).forGetter(f -> f.allowNonCurseTreasure)
                    )
            ).apply(inst, EnchantWithTreasureFunction::new)
    );

    /** If {@code true}, curse enchantments (tag {@code minecraft:curse}) are included in the pool. */
    final boolean allowCurses;
    /** If {@code true}, non-curse treasure enchantments are included in the pool. */
    final boolean allowNonCurseTreasure;

    protected EnchantWithTreasureFunction(List<LootItemCondition> conditions, boolean allowCurses, boolean allowNonCurseTreasure) {
        super(conditions);
        this.allowCurses = allowCurses;
        this.allowNonCurseTreasure = allowNonCurseTreasure;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        HolderLookup.RegistryLookup<Enchantment> lookup = context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<Holder.Reference<Enchantment>> list = lookup.listElements()
                .filter(holder -> stack.supportsEnchantment(holder)
                        && holder.is(EnchantmentTags.TREASURE)
                        && (!holder.is(EnchantmentTags.CURSE) || this.allowCurses)
                        && (holder.is(EnchantmentTags.CURSE) || this.allowNonCurseTreasure))
                .toList();

        if (list.isEmpty())
            return stack;

        Holder.Reference<Enchantment> chosen = list.get(random.nextInt(list.size()));
        return enchantItem(stack, chosen, random);
    }

    private static ItemStack enchantItem(ItemStack stack, Holder<Enchantment> enchantment, RandomSource random) {
        int lvl = Mth.nextInt(random, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel());
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
    public LootItemFunctionType<EnchantWithTreasureFunction> getType() {
        return ILLootFunctions.ENCHANT_WITH_TREASURE.get();
    }
}
