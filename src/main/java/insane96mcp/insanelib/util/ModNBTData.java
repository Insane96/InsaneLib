package insane96mcp.insanelib.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ModNBTData {
    public static CompoundTag getModData(Entity entity, String modId) {
        return getModData(entity.getPersistentData(), modId);
    }

    public static CompoundTag getModData(Entity entity, ResourceLocation location) {
        return getModData(entity, location.getNamespace());
    }

    public static CompoundTag getModData(ItemStack stack, String modId) {
        return getModData(stack.getOrCreateTag(), modId);
    }

    public static CompoundTag getModData(ItemStack stack, ResourceLocation location) {
        return getModData(stack, location.getNamespace());
    }

    public static CompoundTag getModData(CompoundTag compound, String modId) {
        if (compound.contains(modId))
            return compound.getCompound(modId);
        CompoundTag tag = new CompoundTag();
        compound.put(modId, tag);
        return tag;
    }

    public static <T> T get(Entity entity, ResourceLocation loc, Class<T> type) {
        return get(entity.getPersistentData(), loc, type);
    }

    public static <T> T get(ItemStack stack, ResourceLocation loc, Class<T> type) {
        return get(stack.getOrCreateTag(), loc, type);
    }

    public static <T> T get(CompoundTag tag, ResourceLocation loc, Class<T> type) {
        CompoundTag modData = getNestedCompounds(loc.getPath(), getModData(tag, loc.getNamespace()));
        String key = getNestedKey(loc.getPath());

        if (type == Byte.class) return type.cast(modData.getByte(key));
        if (type == Short.class) return type.cast(modData.getShort(key));
        if (type == Integer.class) return type.cast(modData.getInt(key));
        if (type == Float.class) return type.cast(modData.getFloat(key));
        if (type == Double.class) return type.cast(modData.getDouble(key));
        if (type == Boolean.class) return type.cast(modData.getBoolean(key));
        if (type == String.class) return type.cast(modData.getString(key));
        if (type == UUID.class) return type.cast(modData.getUUID(key));
        if (type == CompoundTag.class) return type.cast(modData.getCompound(key));
        if (type == ListTag.class) throw new IllegalArgumentException("Use getList overload");

        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    public static void put(Entity entity, ResourceLocation loc, Object value) {
        put(entity.getPersistentData(), loc, value);
    }

    public static void put(ItemStack stack, ResourceLocation loc, Object value) {
        put(stack.getOrCreateTag(), loc, value);
    }

    public static void put(CompoundTag tag, ResourceLocation loc, Object value) {
        CompoundTag modData = getNestedCompounds(loc.getPath(), getModData(tag, loc.getNamespace()));
        String key = getNestedKey(loc.getPath());

        if (value instanceof Byte b) modData.putByte(key, b);
        else if (value instanceof Short s) modData.putShort(key, s);
        else if (value instanceof Integer i) modData.putInt(key, i);
        else if (value instanceof Float f) modData.putFloat(key, f);
        else if (value instanceof Double d) modData.putDouble(key, d);
        else if (value instanceof Boolean bool) modData.putBoolean(key, bool);
        else if (value instanceof String str) modData.putString(key, str);
        else if (value instanceof UUID uuid) modData.putUUID(key, uuid);
        else if (value instanceof CompoundTag compound) modData.put(key, compound);
        else if (value instanceof ListTag list) modData.put(key, list);
        else throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
    }

    private static String getNestedKey(String path) {
        String key = path;
        if (key.contains("/")) {
            String[] parts = path.split("/");
            key = parts[parts.length - 1];
        }
        return key;
    }

    public static CompoundTag getNestedCompounds(String path, CompoundTag modData) {
        if (path.contains("/")) {
            String[] parts = path.split("/");
            for (int i = 0; i < parts.length - 1; i++)
                modData = getModData(modData, parts[i]);
        }
        return modData;
    }

    public static ListTag getList(Entity entity, ResourceLocation loc, int type) {
        return getList(entity.getPersistentData(), loc, type);
    }

    public static ListTag getList(ItemStack stack, ResourceLocation loc, int type) {
        return getList(stack.getOrCreateTag(), loc, type);
    }

    public static ListTag getList(CompoundTag tag, ResourceLocation loc, int type) {
        CompoundTag modData = getNestedCompounds(loc.getPath(), getModData(tag, loc.getNamespace()));
        String key = getNestedKey(loc.getPath());
        return modData.getList(loc.getPath(), type);
    }

    public static boolean modDataContains(Entity entity, ResourceLocation loc) {
        return getModData(entity.getPersistentData(), loc.getNamespace()).contains(loc.getPath());
    }

    public static boolean modDataContains(ItemStack stack, ResourceLocation loc) {
        return getModData(stack.getOrCreateTag(), loc.getNamespace()).contains(loc.getPath());
    }
}
