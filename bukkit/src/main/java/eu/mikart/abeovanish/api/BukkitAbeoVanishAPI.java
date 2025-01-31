package eu.mikart.abeovanish.api;

import eu.mikart.abeovanish.AbeoBukkit;
import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.BukkitPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BukkitAbeoVanishAPI extends AbeoVanishAPI {

    protected BukkitAbeoVanishAPI(@NotNull IAbeo plugin) {
        super(plugin);
    }

    /**
     * Get an instance of the AbeoVanish API.
     *
     * @return instance of the AbeoVanish API
     * @throws NotRegisteredException if the API has not yet been registered.
     * @since 1.0
     */
    @NotNull
    public static BukkitAbeoVanishAPI getInstance() throws NotRegisteredException {
        return (BukkitAbeoVanishAPI) AbeoVanishAPI.getInstance();
    }

    /**
     * <b>(Internal use only)</b> - Unregister the API instance.
     *
     * @since 1.0
     */
    @ApiStatus.Internal
    public static void register(@NotNull AbeoBukkit plugin) {
        instance = new BukkitAbeoVanishAPI(plugin);
    }

    /**
     * Set the vanish state of a player.
     *
     * @param player the bukkit player
     * @param state  the state
     * @since 1.0
     */
    public void setVanished(Player player, boolean state) {
        this.setVanished(BukkitPlayer.adapt(player), state);
    }

    /**
     * Check if a player is vanished
     *
     * @param player the bukkit player
     * @return true if the player is vanished
     * @since 1.0
     */
    public boolean isVanished(Player player) {
        return this.isVanished(BukkitPlayer.adapt(player));
    }

}
