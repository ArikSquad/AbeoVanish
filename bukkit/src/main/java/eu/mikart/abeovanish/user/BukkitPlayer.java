package eu.mikart.abeovanish.user;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class BukkitPlayer implements Player {
	private final org.bukkit.entity.Player player;

	private BukkitPlayer(@NotNull org.bukkit.entity.Player player) {
		this.player = player;
	}

	@NotNull
	public static BukkitPlayer adapt(@NotNull org.bukkit.entity.Player player) {
		return new BukkitPlayer(player);
	}

	@Override
	@NotNull
	public String getAddress() {
		return Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
	}

	@NotNull
	@Override
	public String getName() {
		return player.getName();
	}

	@NotNull
	@Override
	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	@Override
	public int getPing() {
		return player.getPing();
	}

	@NotNull
	@Override
	public String getServerName() {
		return "server";
	}

	@Override
	public int getPlayersOnServer() {
		return player.getServer().getOnlinePlayers().size();
	}

	@Override
	public boolean hasPermission(String node) {
		return player.hasPermission(node);
	}

	@NotNull
	@Override
	public Audience getAudience() {
		return player;
	}

	@NotNull
	public org.bukkit.entity.Player getBukkitPlayer() {
		return player;
	}

}
