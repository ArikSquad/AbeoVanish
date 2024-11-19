package eu.mikart.abeovanish.user;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Abstract cross-platform Player object
 */
public interface Player {

    /**
     * Return the player's name
     *
     * @return the player's name
     */
    @NotNull
    String getName();

    /**
     * Return the player's {@link UUID}
     *
     * @return the player {@link UUID}
     */
    @NotNull
    UUID getUniqueId();

    /**
     * Return the player's ping
     *
     * @return the player's ping
     */
    int getPing();

    /**
     * Return the name of the server the player is connected to
     *
     * @return player's server name
     */
    @NotNull
    String getServerName();

    @NotNull
    String getAddress();

    /**
     * Return the number of people on that player's server
     *
     * @return player count on the player's server
     */
    int getPlayersOnServer();

    /**
     * Returns if the player has the permission node
     *
     * @param node The permission node string
     * @return {@code true} if the player has the node; {@code false} otherwise
     */
    boolean hasPermission(String node);

    /**
     * Get the audience for this player
     *
     * @return the audience for this player
     */
    @NotNull
    Audience getAudience();

    /**
     * Send a message to the player
     *
     * @param component the message to send
     */
    default void sendMessage(@NotNull Component component) {
        getAudience().sendMessage(component);
    }

}
