package insane96mcp.insanelib.data.lootmodifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;

/**
 * Global loot modifier that appends the contents of another loot table into the current loot roll.
 * <p>
 * JSON example:
 * <pre>{@code
 * {
 *   "type": "insanelib:inject_loot_table",
 *   "conditions": [...],
 *   "loot_table": "minecraft:chests/simple_dungeon"
 * }
 * }</pre>
 */
public class InjectLootTableModifier extends LootModifier {
    public static final MapCodec<InjectLootTableModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(
                    ResourceLocation.CODEC.fieldOf("loot_table").forGetter(m -> m.lootTable)
            ).apply(inst, InjectLootTableModifier::new)
    );

    private final ResourceLocation lootTable;

    public InjectLootTableModifier(LootItemCondition[] conditionsIn, ResourceLocation lootTable) {
        super(conditionsIn);
        this.lootTable = lootTable;
    }

    public InjectLootTableModifier(ResourceLocation lootTableToInjectTo, ResourceLocation lootTable) {
        super(new LootItemCondition[]{new LootTableIdCondition.Builder(lootTableToInjectTo).build()});
        this.lootTable = lootTable;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, this.lootTable);
        LootTable lootTable = context.getLevel().getServer().reloadableRegistries().getLootTable(key);
        lootTable.getRandomItemsRaw(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    @NotNull
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
