package insane96mcp.insanelib.core.feature.config;

import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic black/whitelist backed by {@link ObjTag}.
 * Works with any registry type (Block, Item, EntityType, Fluid, etc.).
 *
 * @param <T> the registry object type
 */
public class Blacklist<T> {
    public List<ObjTag<T>> entries;
    public boolean asWhitelist;
    private final ResourceKey<Registry<T>> registryKey;

    public Blacklist(List<ObjTag<T>> entries, boolean asWhitelist, ResourceKey<Registry<T>> registryKey) {
        this.entries = entries;
        this.asWhitelist = asWhitelist;
        this.registryKey = registryKey;
    }

    public Blacklist(ResourceKey<Registry<T>> registryKey) {
        this(Collections.emptyList(), false, registryKey);
    }

    public ResourceKey<Registry<T>> getRegistryKey() {
        return registryKey;
    }

    /**
     * Returns true if the object is blacklisted (or not whitelisted, if operating in whitelist mode).
     */
    public boolean isBlackOrNotWhiteListed(T obj) {
        boolean found = false;
        for (ObjTag<T> entry : this.entries) {
            if (entry.matches(obj)) {
                found = true;
                break;
            }
        }

        if (this.asWhitelist)
            return !found;
        else
            return found;
    }

    /**
     * Returns the entries as a list of strings for config serialization.
     */
    public List<String> getAsStringList() {
        List<String> list = new ArrayList<>();
        for (ObjTag<T> entry : this.entries) {
            list.add(entry.serialize().getAsString());
        }
        return list;
    }

    /**
     * Parses a list of strings into ObjTag entries.
     */
    public static <T> List<ObjTag<T>> parseStringList(List<? extends String> strings, ResourceKey<Registry<T>> registryKey) {
        List<ObjTag<T>> list = new ArrayList<>();
        for (String s : strings) {
            list.add(ObjTag.of(s, registryKey));
        }
        return list;
    }

    /**
     * Config option for Blacklist, storing entries as a string list + whitelist toggle.
     */
    public static class COption<T> extends ConfigOption<Blacklist<T>> {
        private final ModConfigSpec.ConfigValue<List<? extends String>> listConfig;
        private final ModConfigSpec.ConfigValue<Boolean> listAsWhitelistConfig;
        private final ResourceKey<Registry<T>> registryKey;

        public COption(ModConfigSpec.Builder builder, String name, String description, Blacklist<T> defaultValue) {
            super(builder, name, description);
            this.registryKey = defaultValue.getRegistryKey();
            List<String> split = ConfigUtils.split(name);
            builder.push(split);
            listConfig = builder.defineList("Blacklist", defaultValue.getAsStringList(), o -> o instanceof String);
            listAsWhitelistConfig = builder.comment("If true the list will be treated as a whitelist instead of blacklist").define("List as Whitelist", defaultValue.asWhitelist);
            builder.pop(split.size());
        }

        @Override
        public Blacklist<T> get() {
            List<ObjTag<T>> entries = Blacklist.parseStringList(this.listConfig.get(), this.registryKey);
            return new Blacklist<>(entries, this.listAsWhitelistConfig.get(), this.registryKey);
        }

        @Override
        public void set(Object value) {
            @SuppressWarnings("unchecked")
            Blacklist<T> blacklist = (Blacklist<T>) value;
            this.listConfig.set(blacklist.getAsStringList());
            this.listAsWhitelistConfig.set(blacklist.asWhitelist);
        }

        @Nullable
        @Override
        public List<String> getConfigPath() {
            return null;
        }
    }
}
