package eu.mikart.abeovanish.hook;

import com.lishid.openinv.IOpenInv;
import com.lishid.openinv.internal.ISpecialInventory;
import eu.mikart.abeovanish.user.BukkitPlayer;
import eu.mikart.abeovanish.user.Player;

// This was quite nice to implement IOpenInv here
public abstract class OpenInv implements IOpenInv, IHook {

    @Override
    public void setSilentContainer(Player player, boolean status) {
        BukkitPlayer bukkitPlayer = (BukkitPlayer) player;
        setSilentContainerStatus(bukkitPlayer.getBukkitPlayer(), status);
    }

    @Override
    public void openInventory(Player player, Object inv) {
        BukkitPlayer bukkitPlayer = (BukkitPlayer) player;
        openInventory(bukkitPlayer.getBukkitPlayer(), (ISpecialInventory) inv);
    }

    @Override
    public ISpecialInventory getInventory(Player player, boolean online) throws InstantiationException {
        BukkitPlayer bukkitPlayer = (BukkitPlayer) player;
        return getSpecialInventory(bukkitPlayer.getBukkitPlayer(), online);
    }

}
