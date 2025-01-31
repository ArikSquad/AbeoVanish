package eu.mikart.abeovanish.hook;

import eu.mikart.abeovanish.user.Player;

/**
 * I hope this was worth it
 * <p>
 * <i>I'm back: It might've been because now I can basically run them from here, and also I could maybe even add more integrations,
 * maybe not in this file but like the general thing is done</i>
 * <p>
 * Original class name: IOpenInvAPI (You might realise that it was quite confusing), currently it might be a bit confusing too
 *
 * @author ArikSquad with tears
 * @version 1.0
 * @since 1.0
 */
public interface IHook {

    void setSilentContainer(Player player, boolean status);

    void openInventory(Player player, Object inv);

    Object getInventory(Player player, boolean online) throws InstantiationException;

}
