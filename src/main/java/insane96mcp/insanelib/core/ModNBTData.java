package insane96mcp.insanelib.core;

import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;
import java.util.UUID;

public class ModNBTData {
    private static final CompoundTag EMPTY_TAG = new CompoundTag();
    /**
     * Returns the compound NBT from the modId of the given player persisted data
     */
    public static CompoundTag getPersistedModData(Player player, String modId) {
        return getModData(MCUtils.getOrCreatePersistedData(player), modId);
    }

    /**
     * Returns the compound NBT from the modId of the given player data
     */
    public static CompoundTag getModData(Entity entity, String modId) {
        return getModData(entity.getPersistentData(), modId);
    }

    /**
     * Returns the compound NBT from the modId of the given stack data
     */
    public static CompoundTag getModData(ItemStack stack, String modId) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return getModData(tag, modId);
    }

    /**
     * Returns the compound NBT from the modId of the given compound
     */
    public static CompoundTag getModData(CompoundTag compound, String modId) {
        Optional<CompoundTag> existing = compound.getCompound(modId);
        if (existing.isPresent())
            return existing.get();
        CompoundTag tag = new CompoundTag();
        compound.put(modId, tag);
        return tag;
    }

    /**
     * Returns the data of the entity from the given location, or the type default if absent (null for reference types)
     */
    public static <T> T get(Entity entity, Identifier loc, Class<T> type) {
        return get(entity.getPersistentData(), loc, type);
    }

    /**
     * Returns the data of the entity's persisted data from the given location, or the type default if absent (null for reference types)
     */
    public static <T> T getPersisted(Player player, Identifier loc, Class<T> type) {
        return get(MCUtils.getOrCreatePersistedData(player), loc, type);
    }

    /**
     * Returns the data of the stack from the given location, or the type default if absent (null for reference types)
     */
    public static <T> T get(ItemStack stack, Identifier loc, Class<T> type) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty())
            return null;
        return get(customData.copyTag(), loc, type);
    }

    /**
     * Returns the data of the tag from the given location, or the type default if absent
     */
    public static <T> T get(CompoundTag tag, Identifier loc, Class<T> type) {
        CompoundTag nsData = getModDataReadOnly(tag, loc.getNamespace());
        CompoundTag modData = nsData != null ? getNestedCompoundsReadOnly(loc.getPath(), nsData) : null;
        if (modData == null) modData = EMPTY_TAG;
        String key = getNestedKey(loc.getPath());

        if (type == Byte.class) return type.cast(modData.getByteOr(key, (byte) 0));
        if (type == Short.class) return type.cast(modData.getShortOr(key, (short) 0));
        if (type == Integer.class) return type.cast(modData.getIntOr(key, 0));
        if (type == Long.class) return type.cast(modData.getLongOr(key, 0L));
        if (type == Float.class) return type.cast(modData.getFloatOr(key, 0f));
        if (type == Double.class) return type.cast(modData.getDoubleOr(key, 0d));
        if (type == Boolean.class) return type.cast(modData.getBooleanOr(key, false));
        if (type == String.class) return type.cast(modData.getStringOr(key, ""));
        if (type == UUID.class) return type.cast(modData.read(key, UUIDUtil.CODEC).orElse(null));
        if (type == CompoundTag.class) return type.cast(modData.getCompoundOrEmpty(key));
        if (type == int[].class) return type.cast(modData.getIntArray(key).orElse(new int[0]));
        if (type == ListTag.class) throw new IllegalArgumentException("Use getList overload");

        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    public static void put(Entity entity, Identifier loc, Object value) {
        put(entity.getPersistentData(), loc, value);
    }

    public static void putPersisted(Player player, Identifier loc, Object value) {
        put(MCUtils.getOrCreatePersistedData(player), loc, value);
    }

    public static void put(ItemStack stack, Identifier loc, Object value) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        put(tag, loc, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void put(CompoundTag tag, Identifier loc, Object value) {
        CompoundTag modData = getNestedCompounds(loc.getPath(), getModData(tag, loc.getNamespace()));
        String key = getNestedKey(loc.getPath());

        if (value instanceof Byte b) modData.putByte(key, b);
        else if (value instanceof Short s) modData.putShort(key, s);
        else if (value instanceof Integer i) modData.putInt(key, i);
        else if (value instanceof Long l) modData.putLong(key, l);
        else if (value instanceof Float f) modData.putFloat(key, f);
        else if (value instanceof Double d) modData.putDouble(key, d);
        else if (value instanceof Boolean bool) modData.putBoolean(key, bool);
        else if (value instanceof String str) modData.putString(key, str);
        else if (value instanceof UUID uuid) modData.store(key, UUIDUtil.CODEC, uuid);
        else if (value instanceof CompoundTag compound) modData.put(key, compound);
        else if (value instanceof int[] intArray) modData.put(key, new IntArrayTag(intArray));
        else if (value instanceof ListTag list) modData.put(key, list);
        else throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
    }

    private static String getNestedKey(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash < 0 ? path : path.substring(lastSlash + 1);
    }

    private static CompoundTag getNestedCompounds(String path, CompoundTag modData) {
        int start = 0;
        int end;
        while ((end = path.indexOf('/', start)) >= 0) {
            modData = getModData(modData, path.substring(start, end));
            start = end + 1;
        }
        return modData;
    }

    // Returns null instead of creating missing entries — for read-only paths
    private static CompoundTag getModDataReadOnly(CompoundTag compound, String modId) {
        return compound.getCompound(modId).orElse(null);
    }

    private static CompoundTag getNestedCompoundsReadOnly(String path, CompoundTag modData) {
        int start = 0;
        int end;
        while ((end = path.indexOf('/', start)) >= 0) {
            modData = getModDataReadOnly(modData, path.substring(start, end));
            if (modData == null)
                return null;
            start = end + 1;
        }
        return modData;
    }

    public static ListTag getList(Entity entity, Identifier loc) {
        return getList(entity.getPersistentData(), loc);
    }

    public static ListTag getListPersisted(Player player, Identifier loc) {
        return getList(MCUtils.getOrCreatePersistedData(player), loc);
    }

    public static ListTag getList(ItemStack stack, Identifier loc) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return getList(customData.copyTag(), loc);
    }

    public static ListTag getList(CompoundTag tag, Identifier loc) {
        CompoundTag nsData = getModDataReadOnly(tag, loc.getNamespace());
        if (nsData == null) return new ListTag();
        CompoundTag modData = getNestedCompoundsReadOnly(loc.getPath(), nsData);
        if (modData == null) return new ListTag();
        String key = getNestedKey(loc.getPath());
        return modData.getListOrEmpty(key);
    }

    public static boolean contains(Entity entity, Identifier loc) {
        return contains(entity.getPersistentData(), loc);
    }

    public static boolean contains(Player player, Identifier loc) {
        return contains(MCUtils.getOrCreatePersistedData(player), loc);
    }

    public static boolean contains(ItemStack stack, Identifier loc) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty())
            return false;
        return contains(customData.copyTag(), loc);
    }

    public static boolean contains(CompoundTag tag, Identifier loc) {
        CompoundTag nsData = getModDataReadOnly(tag, loc.getNamespace());
        if (nsData == null) return false;
        CompoundTag modData = getNestedCompoundsReadOnly(loc.getPath(), nsData);
        if (modData == null) return false;
        String key = getNestedKey(loc.getPath());
        return modData.contains(key);
    }

    /**
     * Removes the specified nbt data from the entity data
     */
    public static void remove(Entity entity, Identifier loc) {
        remove(entity.getPersistentData(), loc);
    }

    /**
     * Removes the specified nbt data from the player's persisted data
     */
    public static void removePersisted(Player player, Identifier loc) {
        remove(MCUtils.getOrCreatePersistedData(player), loc);
    }

    /**
     * Removes the specified nbt data from the stack's data
     */
    public static void remove(ItemStack stack, Identifier loc) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty())
            return;
        CompoundTag tag = customData.copyTag();
        remove(tag, loc);
        // Rimuovi il component se il tag è vuoto, altrimenti aggiornalo
        if (tag.isEmpty())
            stack.remove(DataComponents.CUSTOM_DATA);
        else
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Removes the specified nbt data from the compound tag
     */
    public static void remove(CompoundTag tag, Identifier loc) {
        CompoundTag modData = getNestedCompounds(loc.getPath(), getModData(tag, loc.getNamespace()));
        String key = getNestedKey(loc.getPath());
        modData.remove(key);
    }

    public static int classToNBTType(Class<?> type) {
        if (type == Byte.class || type == Boolean.class) return Tag.TAG_BYTE;
        if (type == Short.class) return Tag.TAG_SHORT;
        if (type == Integer.class) return Tag.TAG_INT;
        if (type == Long.class) return Tag.TAG_LONG;
        if (type == Float.class) return Tag.TAG_FLOAT;
        if (type == Double.class) return Tag.TAG_DOUBLE;
        if (type == byte[].class || type == Byte[].class) return Tag.TAG_BYTE_ARRAY;
        if (type == String.class) return Tag.TAG_STRING;
        if (type == ListTag.class) return Tag.TAG_LIST;
        if (type == CompoundTag.class) return Tag.TAG_COMPOUND;
        if (type == UUID.class || type == int[].class || type == Integer[].class) return Tag.TAG_INT_ARRAY;
        if (type == long[].class || type == Long[].class) return Tag.TAG_LONG_ARRAY;
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    public static Class<?> nbtTypeToClass(int type) {
        if (type == Tag.TAG_BYTE) return Byte.class;
        if (type == Tag.TAG_SHORT) return Short.class;
        if (type == Tag.TAG_INT) return Integer.class;
        if (type == Tag.TAG_LONG) return Long.class;
        if (type == Tag.TAG_FLOAT) return Float.class;
        if (type == Tag.TAG_DOUBLE) return Double.class;
        if (type == Tag.TAG_BYTE_ARRAY) return byte[].class;
        if (type == Tag.TAG_STRING) return String.class;
        if (type == Tag.TAG_LIST) return ListTag.class;
        if (type == Tag.TAG_COMPOUND) return CompoundTag.class;
        if (type == Tag.TAG_INT_ARRAY) return int[].class;
        if (type == Tag.TAG_LONG_ARRAY) return long[].class;
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
}
