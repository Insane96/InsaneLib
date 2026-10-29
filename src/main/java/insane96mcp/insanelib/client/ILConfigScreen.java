package insane96mcp.insanelib.client;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import insane96mcp.insanelib.setup.ILModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NeoForge's {@link ConfigurationScreen} automatically registered for every mod that creates an {@link ILModConfig}.
 * Mods that register their own {@link IConfigScreenFactory} (in the constructor or in {@code FMLClientSetupEvent}) keep theirs.
 * <p>
 * InsaneLib configs have no translation keys, so when an entry has no translation its config name is shown instead
 * of the raw key. Translations can still be added with the {@code <modid>.configuration.<entry name>} keys.
 */
public class ILConfigScreen {
    private static final String SECTION = "neoforge.configuration.uitext.section";
    private static final String SECTION_TEXT = "neoforge.configuration.uitext.sectiontext";

    public static void registerForILMods(final FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            for (ModContainer container : ILModConfig.getModContainers()) {
                if (container.getCustomExtension(IConfigScreenFactory.class).isPresent())
                    continue;
                container.registerExtensionPoint(IConfigScreenFactory.class, ILConfigScreen::create);
            }
        });
    }

    public static Screen create(ModContainer container, Screen parent) {
        if (serverConfigsLoadedFor == null && canLoadServerConfigs(container)) {
            ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, FMLPaths.CONFIGDIR.get());
            serverConfigsLoadedFor = parent;
        }
        return new ConfigurationScreen(container, parent, SectionScreen::new);
    }

    /**
     * The screen to return to that unloads the SERVER configs loaded from the main menu
     */
    @Nullable
    private static Screen serverConfigsLoadedFor;

    /**
     * SERVER configs are only loaded while a world is loaded, so NeoForge doesn't allow editing them from the main menu.
     * When the config screen is opened with no world loaded, the SERVER configs are loaded from the global config folder
     * (the one used by worlds without a {@code serverconfig} override).
     */
    private static boolean canLoadServerConfigs(ModContainer container) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null || minecraft.hasSingleplayerServer() || minecraft.getConnection() != null)
            return false;
        Set<ModConfig> serverConfigs = ModConfigs.getConfigSet(ModConfig.Type.SERVER);
        return serverConfigs.stream().anyMatch(modConfig -> modConfig.getModId().equals(container.getModId()))
                && serverConfigs.stream().allMatch(modConfig -> modConfig.getLoadedConfig() == null);
    }

    /**
     * Unloads the SERVER configs loaded from the main menu once the config screen is closed (ConfigurationScreen is final,
     * so the screen it returns to is checked instead)
     */
    public static void onScreenOpening(final ScreenEvent.Opening event) {
        if (serverConfigsLoadedFor == null || event.getNewScreen() != serverConfigsLoadedFor)
            return;
        serverConfigsLoadedFor = null;
        ConfigTracker.INSTANCE.unloadConfigs(ModConfig.Type.SERVER);
    }

    public static class SectionScreen extends ConfigurationScreen.ConfigurationSectionScreen {
        /**
         * Only set for the top section of a SERVER config, where the file being edited is shown
         */
        @Nullable
        private final ModConfig serverConfig;

        public SectionScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
            super(parent, type, modConfig, title);
            this.serverConfig = type == ModConfig.Type.SERVER ? modConfig : null;
        }

        public SectionScreen(Context parentContext, Screen parent, Map<String, Object> valueSpecs, String key, Set<? extends UnmodifiableConfig.Entry> entrySet, Component title) {
            super(parentContext, parent, valueSpecs, key, entrySet, title);
            this.serverConfig = null;
        }

        @Override
        protected ConfigurationScreen.ConfigurationSectionScreen rebuild() {
            super.rebuild();
            if (this.list != null && this.serverConfig != null)
                this.addEditedFileRow(this.serverConfig);
            return this;
        }

        /**
         * Adds at the top a row showing if the global SERVER config file or the world's one is being edited
         */
        private void addEditedFileRow(ModConfig modConfig) {
            Path path;
            try {
                path = modConfig.getFullPath();
            }
            catch (IllegalStateException e) {
                // Not loaded from a file (e.g. synced from a server)
                return;
            }
            boolean global = path.toAbsolutePath().normalize().equals(FMLPaths.CONFIGDIR.get().resolve(modConfig.getFileName()).toAbsolutePath().normalize());
            Component value;
            Component description;
            if (global) {
                value = Component.literal("Global").withStyle(ChatFormatting.GREEN);
                description = Component.literal("Used by all the worlds without their own copy of this file in their \"serverconfig\" folder");
            }
            else {
                MinecraftServer server = minecraft.getSingleplayerServer();
                value = Component.literal(server != null ? "World (%s)".formatted(server.getWorldData().getLevelName()) : "World").withStyle(ChatFormatting.YELLOW);
                description = Component.literal("This world has its own copy of this file in its \"serverconfig\" folder, so the global file is not used");
            }
            Tooltip tooltip = Tooltip.create(Component.empty()
                    .append(description)
                    .append("\n\n")
                    .append(Component.literal("File: \"%s\"".formatted(FMLPaths.GAMEDIR.get().toAbsolutePath().relativize(path.toAbsolutePath()))).withStyle(ChatFormatting.GRAY)));
            StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.literal("Editing config file"), font).alignLeft();
            label.setTooltip(tooltip);
            StringWidget valueWidget = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, value, font);
            valueWidget.setTooltip(tooltip);
            this.list.addSmall(label, valueWidget);
            moveLastToFirst(this.list.children());
        }

        private static <T> void moveLastToFirst(List<T> list) {
            list.addFirst(list.removeLast());
        }

        @Override
        protected MutableComponent getTranslationComponent(final String key) {
            return Component.translatableWithFallback(getTranslationKey(key), key);
        }

        /**
         * Same as super but creates sub-sections as {@link SectionScreen} so the fallback names apply to them too
         */
        @Override
        @Nullable
        protected Element createSection(final String key, final UnmodifiableConfig subconfig, final UnmodifiableConfig subsection) {
            if (subconfig.isEmpty())
                return null;
            String buttonKey = getTranslationKey(key) + ".button";
            return new Element(Component.translatable(SECTION, getTranslationComponent(key)), getTooltipComponent(key, null),
                    Button.builder(Component.translatable(SECTION, Component.translatable(I18n.exists(buttonKey) ? buttonKey : SECTION_TEXT)),
                                    button -> minecraft.setScreen(sectionCache.computeIfAbsent(key,
                                            k -> new SectionScreen(context, this, subconfig.valueMap(), key, subsection.entrySet(), getTranslationComponent(key)).rebuild())))
                            .tooltip(Tooltip.create(getTooltipComponent(key, null)))
                            .width(Button.DEFAULT_WIDTH)
                            .build(),
                    false);
        }
    }
}
