package eu.mikart.abeovanish;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import eu.mikart.abeovanish.commands.VanishCommand;
import eu.mikart.abeovanish.config.Locales;
import eu.mikart.abeovanish.config.Settings;
import eu.mikart.abeovanish.database.Database;
import eu.mikart.abeovanish.listener.PacketEventsPacketListener;
import eu.mikart.abeovanish.user.BukkitPlayer;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Logger;

@Setter
@Getter
public final class AbeoBukkit extends JavaPlugin implements IAbeo {
	private Settings settings;
	private Locales locales;
	private Database database;

	@Override
	public void onLoad() {
		PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
		PacketEvents.getAPI().load();
		CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
	}

	@Override
	public void onEnable() {
		this.loadConfig();
		this.loadDatabase();

		PacketEvents.getAPI().getEventManager().registerListener(
				new PacketEventsPacketListener(this), PacketListenerPriority.NORMAL);
		PacketEvents.getAPI().init();
		CommandAPI.onEnable();

		new VanishCommand(this);
	}

	@Override
	public void onDisable() {
		getDatabase().close();
		PacketEvents.getAPI().terminate();
		CommandAPI.onDisable();
	}

	@Override
	@NotNull
	public Path getConfigDirectory() {
		return getDataFolder().toPath();
	}

	@Override
	@NotNull
	public AbeoBukkit getPlugin() {
		return this;
	}

	@Override
	@NotNull
	public Logger getLogger() {
		return super.getLogger();
	}

	@NotNull
	@Override
	public Audience getConsole() {
		return getServer().getConsoleSender();
	}

	@NotNull
	@Override
	public String getPlatform() {
		return getServer().getName();
	}

	@Override
	public Collection<eu.mikart.abeovanish.user.Player> getOnlinePlayers() {
		return getServer().getOnlinePlayers().stream()
				.map(user -> (eu.mikart.abeovanish.user.Player) BukkitPlayer.adapt(user))
				.toList();
	}

	@Override
	public Collection<eu.mikart.abeovanish.user.Player> getOnlinePlayersOnServer(@NotNull eu.mikart.abeovanish.user.Player player) {
		return getOnlinePlayers();
	}

	@Override
	public Collection<eu.mikart.abeovanish.user.Player> getOnlineVanishedPlayers() {
		return getServer().getOnlinePlayers().stream()
				.filter(player -> getDatabase().getVanishState(player.getUniqueId()))
				.map(user -> (eu.mikart.abeovanish.user.Player) BukkitPlayer.adapt(user))
				.toList();
	}

}
