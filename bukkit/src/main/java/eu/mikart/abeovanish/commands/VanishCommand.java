package eu.mikart.abeovanish.commands;

import dev.jorel.commandapi.CommandAPICommand;
import eu.mikart.abeovanish.AbeoBukkit;
import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.BukkitPlayer;
import org.bukkit.entity.Player;

public class VanishCommand {
	private final IAbeo plugin;

	public VanishCommand(IAbeo plugin) {
		this.plugin = plugin;

		new CommandAPICommand("vanish")
				.withAliases("v")
				.withPermission("abeovanish.vanish")
				.executesPlayer((player, args) -> {
					handleVanish(player);
				})
				.register();
	}

	private void handleVanish(Player player) {
		boolean toggle = plugin.getDatabase().getVanishState(player.getUniqueId());
		plugin.getDatabase().setVanishState(player.getUniqueId(), !toggle);

		for (eu.mikart.abeovanish.user.Player p : plugin.getOnlinePlayers()) {
			BukkitPlayer bukkitPlayer = (BukkitPlayer) p;
			Player player1 = player.getPlayer();

			if (player1 == null) {
				continue;
			}

			player1.hidePlayer((AbeoBukkit) plugin, bukkitPlayer.getBukkitPlayer());

			if (plugin.getSettings().isSendJoinQuitMessages()) {
				if (toggle) {
					plugin.getLocales().getLocale("fake_quit", player.getName()).ifPresent(player1::sendMessage);
				} else {
					plugin.getLocales().getLocale("fake_join", player.getName()).ifPresent(player1::sendMessage);
				}
			}
		}

		plugin.getLocales().getLocale("vanish_" + (!toggle ? "on" : "off")).ifPresent(player::sendMessage);
	}

}
