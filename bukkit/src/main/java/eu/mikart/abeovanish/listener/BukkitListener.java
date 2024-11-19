package eu.mikart.abeovanish.listener;

import eu.mikart.abeovanish.AbeoBukkit;
import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.BukkitPlayer;
import eu.mikart.abeovanish.user.Player;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class BukkitListener implements Listener {

	private final IAbeo plugin;

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		for (Player p : plugin.getOnlineVanishedPlayers()) {
			BukkitPlayer bukkitPlayer = (BukkitPlayer) p;
			org.bukkit.entity.Player player = bukkitPlayer.getBukkitPlayer();
			event.getPlayer().hidePlayer((AbeoBukkit) plugin.getPlugin(), player);
		}
	}

	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent event) {

	}

}
