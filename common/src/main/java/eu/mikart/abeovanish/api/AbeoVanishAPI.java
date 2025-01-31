package eu.mikart.abeovanish.api;

import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.Player;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class AbeoVanishAPI {
    // Singleton API instance
    protected static AbeoVanishAPI instance;
    // Plugin instance
    protected final IAbeo plugin;

    /**
     * Returns if the plugin has finished loading data
     *
     * @return {@code true} if the plugin has finished loading data, {@code false} otherwise
     * @since 1.0
     */
    public boolean isLoaded() {
        return true; // TODO: Implement
    }

    /**
     * Get a raw locale from the plugin locale file
     *
     * @param localeId     the locale ID to get
     * @param replacements the replacements to make in the locale
     * @return the locale, with replacements made
     * @since 1.0
     */
    public Optional<String> getRawLocale(@NotNull String localeId, @NotNull String... replacements) {
        return plugin.getLocales().getRawLocale(localeId, replacements);
    }

    /**
     * Get a locale from the plugin locale file
     *
     * @param localeId     the locale ID to get
     * @param replacements the replacements to make in the locale
     * @return the locale as a formatted adventure {@link Component}, with replacements made
     * @since 1.0
     */
    public Optional<Component> getLocale(@NotNull String localeId, @NotNull String... replacements) {
        return plugin.getLocales().getLocale(localeId, replacements);
    }

    /**
     * Get an instance of the AbeoVanish API.
     *
     * @return instance of the AbeoVanish API
     * @throws NotRegisteredException if the API has not yet been registered.
     * @since 1.0
     */
    @NotNull
    public static AbeoVanishAPI getInstance() throws NotRegisteredException {
        if (instance == null) {
            throw new NotRegisteredException();
        }
        return instance;
    }

    /**
     * <b>(Internal use only)</b> - Unregister the API instance.
     *
     * @since 1.0
     */
    @ApiStatus.Internal
    public static void unregister() {
        instance = null;
    }

    /**
     * Check if a player is vanished
     *
     * @param player the {@link Player} to check
     * @param state  the state to set the player to
     * @since 1.0
     */
    public void setVanished(Player player, boolean state) {
        plugin.getVanishStateManager().setVanished(player, state);
    }

    /**
     * Check if a player is vanished
     *
     * @param player the {@link Player} to check
     * @return {@code true} if the player is vanished, {@code false} otherwise
     */
    public boolean isVanished(Player player) {
        return plugin.getDatabase().getVanishState(player.getUniqueId());
    }

    /**
     * An exception indicating the plugin has been accessed before it has been registered.
     *
     * @since 1.0
     */
    public static final class NotRegisteredException extends IllegalStateException {

        private static final String MESSAGE = """
                Could not access the AbeoVanish API as it has not yet been registered. This could be because:
                1) AbeoVanish has failed to enable successfully
                2) Your plugin isn't set to load after AbeoVanish has
                   (Check if it set as a (soft)depend in plugin.yml or to load: BEFORE in paper-plugin.yml?)
                3) You are attempting to access AbeoVanish on plugin construction/before your plugin has enabled.""";

        NotRegisteredException() {
            super(MESSAGE);
        }

    }

}
