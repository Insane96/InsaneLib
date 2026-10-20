package insane96mcp.insanelib.client;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import insane96mcp.insanelib.setup.ILModConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.Nullable;

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
        return new ConfigurationScreen(container, parent, SectionScreen::new);
    }

    public static class SectionScreen extends ConfigurationScreen.ConfigurationSectionScreen {
        public SectionScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
            super(parent, type, modConfig, title);
        }

        public SectionScreen(Context parentContext, Screen parent, Map<String, Object> valueSpecs, String key, Set<? extends UnmodifiableConfig.Entry> entrySet, Component title) {
            super(parentContext, parent, valueSpecs, key, entrySet, title);
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
