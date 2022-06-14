package insane96mcp.insanelib.util;


import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IdTagMatcher {
    public Type type;
    public ResourceLocation location;
    @Nullable
    public ResourceLocation dimension;

    public IdTagMatcher(Type type, ResourceLocation location, @Nullable ResourceLocation dimension) {
        Objects.requireNonNull(type, "type can't be null");
        Objects.requireNonNull(location, "location can't be null");

        this.type = type;
        this.location = location;
        this.dimension = dimension;
    }

    public IdTagMatcher(Type type, ResourceLocation location) {
        this(type, location, null);
    }

    /**
     * Returns null if can't parse the line
     */
    @Nullable
    public static IdTagMatcher parseLine(String line) {
        String[] split = line.split(",");
        if (split.length < 1 || split.length > 2) {
            LogHelper.warn("Invalid line \"%s\". Format must be modid:entry_or_tag,modid:dimension", line);
            return null;
        }
        ResourceLocation dimension = null;
        if (split.length == 2) {
            dimension = ResourceLocation.tryParse(split[1]);
            if (dimension == null) {
                LogHelper.warn(String.format("Invalid dimension. Ignoring it. '%s'", line));
            }
        }
        if (split[0].startsWith("#")) {
            ResourceLocation tag = ResourceLocation.tryParse(split[0].substring(1));
            if (tag == null) {
                LogHelper.warn("Tag is not valid. '%s'", line);
                return null;
            }
            return new IdTagMatcher(Type.TAG, tag, dimension);
        }
        else {
            ResourceLocation id = ResourceLocation.tryParse(split[0]);
            if (id == null) {
                LogHelper.warn("Id is not valid. '%s'", line);
                return null;
            }
            return new IdTagMatcher(Type.ID, id, dimension);
        }
    }

    public static ArrayList<? extends IdTagMatcher> parseStringList(List<? extends String> list) {
        ArrayList<IdTagMatcher> idTagMatchers = new ArrayList<>();
        for (String line : list) {
            IdTagMatcher idTagMatcher = IdTagMatcher.parseLine(line);
            if (idTagMatcher != null)
                idTagMatchers.add(idTagMatcher);
        }
        return idTagMatchers;
    }

    public boolean matchesBlock(Block block) {
        return matchesBlock(block, null);
    }

    public boolean matchesBlock(Block block, @Nullable ResourceLocation dimensionId) {
        if (this.type == Type.TAG) {
            TagKey<Block> tagKey = TagKey.create(Registry.BLOCK_REGISTRY, this.location);
            ITag<Block> tag = ForgeRegistries.BLOCKS.tags().getTag(tagKey);
            if (!tag.contains(block))
                return false;
            return dimensionId == null || dimensionId.equals(this.dimension);
        }
        else {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id != null && id.equals(this.location))
                return dimensionId == null || dimensionId.equals(this.dimension);
        }
        return false;
    }

    public boolean matchesItem(Item item) {
        return matchesItem(item, null);
    }

    public boolean matchesItem(Item item, @Nullable ResourceLocation dimensionId) {
        if (this.type == Type.TAG) {
            TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, this.location);
            ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(tagKey);
            if (!tag.contains(item))
                return false;
            return dimensionId == null || dimensionId.equals(this.dimension);
        }
        else {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null && id.equals(this.location))
                return dimensionId == null || dimensionId.equals(this.dimension);
        }
        return false;
    }

    public boolean matchesFluid(Fluid fluid) {
        return matchesFluid(fluid, null);
    }

    public boolean matchesFluid(Fluid fluid, @Nullable ResourceLocation dimensionId) {
        if (this.type == Type.TAG) {
            TagKey<Fluid> tagKey = TagKey.create(Registry.FLUID_REGISTRY, this.location);
            ITag<Fluid> fluidTag = ForgeRegistries.FLUIDS.tags().getTag(tagKey);
            if (!fluidTag.contains(fluid))
                return false;
            return dimensionId == null || dimensionId.equals(this.dimension);
        }
        else {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
            if (id != null && id.equals(this.location))
                return dimensionId == null || dimensionId.equals(this.dimension);
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
        if (this.type == Type.TAG) {
            TagKey<EntityType<?>> tagKey = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, this.location);
            ITag<EntityType<?>> tag = ForgeRegistries.ENTITIES.tags().getTag(tagKey);
            if (!tag.contains(entityType))
                return false;
            return dimensionId == null || dimensionId.equals(this.dimension);
        }
        else {
            ResourceLocation id = ForgeRegistries.ENTITIES.getKey(entityType);
            if (id != null && id.equals(this.location))
                return dimensionId == null || dimensionId.equals(this.dimension);
        }
        return false;
    }

    public List<Block> getAllBlocks() {
        List<Block> blocks = new ArrayList<>();
        if (this.type == Type.ID) {
            Block block = ForgeRegistries.BLOCKS.getValue(this.location);
            if (block != null)
                blocks.add(block);
        }
        else {
            TagKey<Block> tagKey = TagKey.create(Registry.BLOCK_REGISTRY, this.location);
            ITag<Block> blockTag = ForgeRegistries.BLOCKS.tags().getTag(tagKey);
            blocks.addAll(blockTag.stream().toList());
        }
        return blocks;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        if (this.type == Type.ID) {
            Item item = ForgeRegistries.ITEMS.getValue(this.location);
            if (item != null)
                items.add(item);
        }
        else {
            TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, this.location);
            ITag<Item> itemTag = ForgeRegistries.ITEMS.tags().getTag(tagKey);
            items.addAll(itemTag.stream().toList());
        }
        return items;
    }

    public enum Type {
        ID,
        TAG
    }
}