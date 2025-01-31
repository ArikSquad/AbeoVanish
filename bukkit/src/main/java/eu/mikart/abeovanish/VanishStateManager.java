package eu.mikart.abeovanish;

import eu.mikart.abeovanish.user.BukkitPlayer;
import eu.mikart.abeovanish.user.Player;
import lombok.AllArgsConstructor;
import org.bukkit.metadata.FixedMetadataValue;

@AllArgsConstructor
public class VanishStateManager implements IVSM {

    private final IAbeo plugin;

    public void setVanished(Player player, boolean state) {
        org.bukkit.entity.Player bukkitPlayer = ((BukkitPlayer) player).getBukkitPlayer();

        // This cast is almost always needed, so we can use the metadata. I think it's the best idea to use the actual
        // plugin instance in the metadata value, so it could maybe be checked which plugin did it? If spigot/paper thinks
        // It should be passed there, I think there is a reason.
        AbeoBukkit abeo = (AbeoBukkit) plugin;

        // Support most other plugins that use metadata to check if a player is vanished
        if (state) {
            bukkitPlayer.setMetadata("vanished", new FixedMetadataValue(abeo, true));
        } else {
            bukkitPlayer.removeMetadata("vanished", abeo);
        }

        // Set database state, so we can remember this
        plugin.getDatabase().setVanishState(player.getUniqueId(), state);

        for (Player p : plugin.getOnlinePlayers()) {
            if (p.getUniqueId() == player.getUniqueId()) continue;
            org.bukkit.entity.Player onlineBukkitPlayer = ((BukkitPlayer) p).getBukkitPlayer();
            if (onlineBukkitPlayer.hasPermission("abeovanish.see")) {
                plugin.getLocales().getLocale("other_vanished_" + (state ? "on" : "off"), player.getName()).ifPresent(p::sendMessage);
                continue;
            }

            if (state) {
                onlineBukkitPlayer.hidePlayer(abeo, bukkitPlayer);
            } else {
                onlineBukkitPlayer.showPlayer(abeo, bukkitPlayer);
            }
        }

        if (plugin.getSettings().getVanishSettings().isSilentChests()) {
            if (plugin.getOpenInv() != null) {
                plugin.getOpenInv().setSilentContainer(player, state);
            }
        }
    }

}
