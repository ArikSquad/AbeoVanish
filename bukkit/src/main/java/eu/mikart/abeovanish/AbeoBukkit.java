package eu.mikart.abeovanish;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import dev.jorel.commandapi.CommandAPI;
import eu.mikart.abeovanish.api.BukkitAbeoVanishAPI;
import eu.mikart.abeovanish.commands.AbeoCommand;
import eu.mikart.abeovanish.commands.VanishCommand;
import eu.mikart.abeovanish.config.Locales;
import eu.mikart.abeovanish.config.Settings;
import eu.mikart.abeovanish.database.Database;
import eu.mikart.abeovanish.hook.OpenInv;
import eu.mikart.abeovanish.listener.BukkitListener;
import eu.mikart.abeovanish.listener.ExperimentalPacketListener;
import eu.mikart.abeovanish.listener.PacketEventsPacketListener;
import eu.mikart.abeovanish.listener.TabCompleteListener;
import eu.mikart.abeovanish.user.BukkitPlayer;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Setter
@Getter
public final class AbeoBukkit extends JavaPlugin implements IAbeo {
    private Settings settings;
    private Locales locales;
    private Database database;
    private VanishStateManager vanishStateManager;
    private OpenInv openInv;
    private boolean productionEnv; // not used

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        //CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
    }

    @Override
    public void onEnable() {
        this.loadConfig();
        this.database = this.loadDatabase();

        String buildType = "unknown";
        try (InputStream input = this.getResource("build.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                buildType = prop.getProperty("build.type", "unknown");
            }
        } catch (Exception e) {
            getLogger().warning("Could not load build type: " + e.getMessage());
        }

        if ("development".equalsIgnoreCase(buildType)) {
            getLogger().info("***************************************");
            getLogger().info("* WARNING: This is a development build! *");
            getLogger().info("* Features may be experimental and unstable. *");
            getLogger().info("***************************************");
            productionEnv = false;
        } else if ("production".equalsIgnoreCase(buildType)) {
            productionEnv = true;
        } else {
            getLogger().warning("Unknown build type detected. Proceeding with defaults.");
        }

        this.vanishStateManager = new VanishStateManager(this);
        this.getServer().getPluginManager().registerEvents(new BukkitListener(this), this);

        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketEventsPacketListener(this), PacketListenerPriority.NORMAL);

        if (getSettings().getFunctionalitySettings().isUseChatArgumentPacketLevelHiding()) {
            PacketEvents.getAPI().getEventManager().registerListener(
                    new ExperimentalPacketListener(this), PacketListenerPriority.NORMAL);
            getLogger().info("Using experimental packet level hiding for chat arguments.");
        } else {
            this.getServer().getPluginManager().registerEvents(new TabCompleteListener(this), this);
        }

        PacketEvents.getAPI().init();
        //CommandAPI.onEnable();

        new VanishCommand(this);
        new AbeoCommand(this);

        if (getServer().getPluginManager().getPlugin("OpenInv") != null) {
            this.openInv = (OpenInv) getServer().getPluginManager().getPlugin("OpenInv");
        }

        // I guess there's Folia support... (no idea if it runs on Paper if I do it this way). Also, this is messy
        getServer().getAsyncScheduler().runAtFixedRate(this, (task) -> getOnlineVanishedPlayers().forEach(player -> getLocales().getLocale("currently_vanished").ifPresent(((BukkitPlayer) player).getBukkitPlayer()::sendActionBar)), 0, 2, TimeUnit.SECONDS);
        BukkitAbeoVanishAPI.register(this);
    }

    @Override
    public void onDisable() {
        if (database != null) {
            getDatabase().close();
        }
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

    @Override
    public String getVersion() {
        return getPluginMeta().getVersion();
    }

}
