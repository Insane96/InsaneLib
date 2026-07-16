package insane96mcp.insanelib.data.lootmodifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Global loot modifier that strips enchantments from all items in the generated loot.
 * Enchanted books are converted back to plain books. Items in the optional blacklist tag are left untouched.
 * <p>
 * JSON example:
 * <pre>{@code
 * {
 *   "type": "insanelib:disenchant",
 *   "conditions": [...],
 *   "blacklisted_items_tag": "insanelib:disenchant_blacklist"
 * }
 * }</pre>
 *
 * @see Builder
 */
public class DisenchantModifier extends LootModifier {
    public static final MapCodec<DisenchantModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(
                    TagKey.codec(Registries.ITEM).optionalFieldOf("blacklisted_items_tag").forGetter(m -> m.blacklistedItemsTag)
            ).apply(inst, DisenchantModifier::new)
    );

    /** Items in this tag are not disenchanted. Enchanted books in the tag are also kept as enchanted books. */
    private Optional<TagKey<Item>> blacklistedItemsTag;

    public DisenchantModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn, DEFAULT_PRIORITY);
    }

    public DisenchantModifier(LootItemCondition[] conditionsIn, int priority, Optional<TagKey<Item>> blacklistedItemsTag) {
        super(conditionsIn, priority);
        this.blacklistedItemsTag = blacklistedItemsTag;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        List<ItemStack> toRemove = new ArrayList<>();
        generatedLoot.forEach(itemStack -> {
            if (blacklistedItemsTag.isPresent() && itemStack.is(blacklistedItemsTag.get()))
                return;

            if (!itemStack.is(Items.ENCHANTED_BOOK))
                itemStack.remove(DataComponents.ENCHANTMENTS);
            else
                toRemove.add(itemStack);
        });
        toRemove.forEach(stack -> {
            generatedLoot.remove(stack);
            generatedLoot.add(new ItemStack(Items.BOOK));
        });
        return generatedLoot;
    }

    @Override
    @NotNull
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    public static class Builder {
        final DisenchantModifier disenchantModifier;

        public Builder(LootItemCondition[] conditionsIn) {
            this.disenchantModifier = new DisenchantModifier(conditionsIn);
        }

        public Builder(Identifier lootTable) {
            this(new LootItemCondition[]{LootTableIdCondition.builder(lootTable).build()});
        }

        public DisenchantModifier build() {
            return this.disenchantModifier;
        }
    }
}
