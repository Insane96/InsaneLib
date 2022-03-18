package insane96mcp.insanelib.utils;


import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.tags.ITag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class IdTagMatcher {
    public ResourceLocation id;
    public ResourceLocation tag;
    public ResourceLocation dimension;

    public IdTagMatcher(@Nullable ResourceLocation id, @Nullable ResourceLocation tag, ResourceLocation dimension) {
        if (id == null && tag == null)
            throw new NullPointerException("'id' and 'tag' can't be both null");

        this.id = id;
        this.tag = tag;
        this.dimension = dimension;
    }

    public IdTagMatcher(@Nullable ResourceLocation id, @Nullable ResourceLocation tag) {
        this(id, tag, AnyRL);
    }

    @Nullable
    public static IdTagMatcher parseLine(String line) {
        String[] split = line.split(",");
        if (split.length < 1 || split.length > 2) {
            LogHelper.warn("Invalid line \"%s\". Format must be modid:item_or_block_id,modid:dimension", line);
            return null;
        }
        ResourceLocation dimension = AnyRL;
        if (split.length == 2) {
            dimension = ResourceLocation.tryParse(split[1]);
            if (dimension == null) {
                LogHelper.warn(String.format("Invalid dimension \"%s\". Ignoring it", split[1]));
                dimension = AnyRL;
            }
        }
        if (split[0].startsWith("#")) {
            String replaced = split[0].replace("#", "");
            ResourceLocation tag = ResourceLocation.tryParse(replaced);
            if (tag == null) {
                LogHelper.warn("%s tag is not valid", replaced);
                return null;
            }
            return new IdTagMatcher(null, tag, dimension);
        }
        else {
            ResourceLocation id = ResourceLocation.tryParse(split[0]);
            if (id == null) {
                LogHelper.warn("%s id is not valid", line);
                return null;
            }
            if (ForgeRegistries.BLOCKS.containsKey(id) || ForgeRegistries.ITEMS.containsKey(id) || ForgeRegistries.ENTITIES.containsKey(id)) {
                return new IdTagMatcher(id, null, dimension);
            }
            else {
                LogHelper.warn(String.format("%s id seems to not exist", line));
                return null;
            }
        }
    }

    public static ArrayList<? extends IdTagMatcher> parseStringList(List<? extends String> list) {
        ArrayList<IdTagMatcher> commonTagBlock = new ArrayList<>();
        for (String line : list) {
            IdTagMatcher idTagMatcher = IdTagMatcher.parseLine(line);
            if (idTagMatcher != null)
                commonTagBlock.add(idTagMatcher);
        }
        return commonTagBlock;
    }

    public static final ResourceLocation AnyRL = new ResourceLocation("any");

    public boolean matchesFluid(Fluid fluid) {
        return matchesFluid(fluid, null);
    }

    public boolean matchesFluid(Fluid fluid, @Nullable ResourceLocation dimensionId) {
        if (dimensionId == null)
            dimensionId = AnyRL;
        if (this.tag != null) {
            TagKey<Fluid> tagKey = TagKey.create(Registry.FLUID_REGISTRY, this.tag);
            ITag<Fluid> fluidTag = ForgeRegistries.FLUIDS.tags().getTag(tagKey);
            if (!fluidTag.contains(fluid))
                return false;
            return this.dimension.equals(AnyRL) || this.dimension.equals(dimensionId);
        }
        else {
            ResourceLocation fluidId = fluid.getRegistryName();
            if (fluidId.equals(this.id))
                return this.dimension.equals(AnyRL) || this.dimension.equals(dimensionId);
        }
        return false;
    }

    public boolean matchesBlock(Block block) {
        return matchesBlock(block, null);
    }

    public boolean matchesBlock(Block block, @Nullable ResourceLocation dimensionId) {
        if (dimensionId == null)
            dimensionId = AnyRL;
        ResourceLocation blockId = block.getRegistryName();
        if (this.tag != null) {
            TagKey<Block> tagKey = TagKey.create(Registry.BLOCK_REGISTRY, this.tag);
            ITag<Block> blockTag = ForgeRegistries.BLOCKS.tags().getTag(tagKey);
            if (!blockTag.contains(block))
                return false;
            return this.dimension.equals(AnyRL) || this.dimension.equals(dimensionId);
        }
        else {
            if (blockId.equals(this.id))
                return this.dimension.equals(AnyRL) || this.dimension.equals(dimensionId);
        }
        return false;
    }

    public boolean matchesItem(Item item) {
        return matchesItem(item, null);
    }

    public boolean matchesItem(Item item, @Nullable ResourceLocation dimensionId) {
        if (dimensionId == null)
            dimensionId = AnyRL;
        ResourceLocation itemId = item.getRegistryName();
        if (this.tag != null) {
            TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, this.tag);
            ITag<Item> itemTag = ForgeRegistries.ITEMS.tags().getTag(tagKey);
            if (!itemTag.contains(item))
                return false;

            return this.dimension.equals(AnyRL) || this.dimension.equals(dimensionId);
        }
        else {
            if (itemId.equals(this.id))
                return this.dimension.equals(AnyRL) || this.dimension.equals(dimensionId);
        }
        return false;
    }

    public boolean matchesEntity(Entity entity) {
        return matchesEntity(entity.getType(), null);
    }
    public boolean matchesEntity(Entity entity, @Nullable ResourceLocation dimensionId) {
        return matchesEntity(entity.getType(), dimensionId);
    }
    public boolean matchesEntity(EntityType<?> entityType) {
        return matchesEntity(entityType, null);
    }

    public boolean matchesEntity(EntityType<?> entityType, @Nullable ResourceLocation dimensionId) {
        if (dimensionId == null)
            dimensionId = AnyRL;
        ResourceLocation entityId = entityType.getRegistryName();
        if (this.tag != null) {
            TagKey<EntityType<?>> tagKey = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, this.tag);
            ITag<EntityType<?>> entityTypeTag = ForgeRegistries.ENTITIES.tags().getTag(tagKey);
            if (!entityTypeTag.contains(entityType))
                return false;
            return this.dimension.equals(AnyRL) || this.dimension.equals(dimensionId);
        }
        else {
            if (entityId.equals(this.id))
                return this.dimension.equals(AnyRL) || this.dimension.equals(dimensionId);
        }
        return false;
    }

    /**
     * Checks if the registry entry (either potion, enchantment, etc) is in the IdTagMatcher
     *
     * @return true if entry's registry name matches the tag's itemId
     */
    public <T extends IForgeRegistryEntry<T>> boolean matchesGeneric(ForgeRegistryEntry<T> entry) {
        return matchesGeneric(entry, null);
    }

    /**
     * Checks if the registry entry (either potion, enchantment, etc) is in the IdTagMatcher
     *
     * @return true if entry's registry name matches the tag's itemId and if the dimension matches
     */
    public <T extends IForgeRegistryEntry<T>> boolean matchesGeneric(ForgeRegistryEntry<T> entry, @Nullable ResourceLocation dimensionId) {
        if (dimensionId == null)
            dimensionId = AnyRL;
        ResourceLocation itemId = entry.getRegistryName();
        if (itemId.equals(this.id))
            return this.dimension.equals(AnyRL) || this.dimension.equals(dimensionId);
        return false;
    }

    public List<Block> getAllBlocks() {
        List<Block> blocks = new ArrayList<>();
        if (this.id != null) {
            Block block = ForgeRegistries.BLOCKS.getValue(this.id);
            if (block != null)
                blocks.add(block);
        }
        else {
            TagKey<Block> tagKey = TagKey.create(Registry.BLOCK_REGISTRY, this.tag);
            ITag<Block> blockTag = ForgeRegistries.BLOCKS.tags().getTag(tagKey);
            blocks.addAll(blockTag.stream().toList());
        }
        return blocks;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        if (this.id != null) {
            Item item = ForgeRegistries.ITEMS.getValue(this.id);
            if (item != null)
                items.add(item);
        }
        else {
            TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, this.tag);
            ITag<Item> itemTag = ForgeRegistries.ITEMS.tags().getTag(tagKey);
            items.addAll(itemTag.stream().toList());
        }
        return items;
    }
}