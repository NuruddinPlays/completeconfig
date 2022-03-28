package me.lortseam.completeconfig.gui;

import lombok.NonNull;
import me.lortseam.completeconfig.data.Config;
import me.lortseam.completeconfig.data.Entry;
import me.lortseam.completeconfig.text.TranslationKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public abstract class ConfigScreenBuilder<T> {

    private static final Map<String, Supplier<ConfigScreenBuilder<?>>> suppliers = new HashMap<>();

    public static void setMain(@NonNull String modId, @NonNull Supplier<ConfigScreenBuilder<?>> screenBuilderSupplier) {
        suppliers.put(modId, screenBuilderSupplier);
    }

    /**
     * Sets the main screen builder for a mod. The main screen builder will be used to build the config screen if no
     * custom builder was specified.
     *
     * @param modId the mod's ID
     * @param screenBuilder the screen builder
     */
    public static void setMain(@NonNull String modId, @NonNull ConfigScreenBuilder<?> screenBuilder) {
        setMain(modId, () -> screenBuilder);
    }

    public static Optional<Supplier<ConfigScreenBuilder<?>>> getMainSupplier(String modId) {
        return Optional.ofNullable(suppliers.get(modId));
    }

    private final List<GuiProvider<T>> providers = new ArrayList<>();
    protected Identifier background = DrawableHelper.OPTIONS_BACKGROUND_TEXTURE;

    protected ConfigScreenBuilder(List<GuiProvider<T>> globalProviders) {
        providers.addAll(globalProviders);
    }

    /**
     * Registers a custom GUI provider.
     *
     * @param provider the custom GUI provider
     */
    public final void register(GuiProvider<T> provider) {
        providers.add(provider);
    }

    /**
     * Registers custom GUI providers.
     *
     * @param providers the custom GUI providers
     */
    public final void register(Collection<GuiProvider<T>> providers) {
        this.providers.addAll(providers);
    }

    protected final Text getTitle(Config config) {
        TranslationKey customTitle = config.getTranslation(true).append("title");
        if (customTitle.exists()) {
            return customTitle.toText();
        }
        return new TranslatableText("completeconfig.gui.defaultTitle", config.getMod().getName());
    }

    public final ConfigScreenBuilder<?> setBackground(Identifier background) {
        this.background = background;
        return this;
    }

    /**
     * Builds a screen based on a config.
     *
     * @param parentScreen the parent screen
     * @param config the config to build the screen of
     * @return the built screen
     */
    public abstract Screen build(Screen parentScreen, Config config);

    protected T createEntry(Entry<?> entry) {
        return providers.stream().filter(provider -> provider.test(entry)).findFirst().map(provider -> {
            return (EntryBuilder<Entry<?>, T>) provider.getBuilder();
        }).orElseThrow(() -> {
            return new UnsupportedOperationException("Could not generate GUI for entry " + entry);
        }).build(entry);
    }

}
