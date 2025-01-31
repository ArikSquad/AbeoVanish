package eu.mikart.abeovanish.user;

import eu.mikart.abeovanish.IAbeo;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ConsolePlayer implements Player {

    private static final UUID consoleUUID = new UUID(0, 0);
    private static final String consoleUsername = "[CONSOLE]";
    private final IAbeo plugin;

    private ConsolePlayer(@NotNull IAbeo plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getName() {
        return consoleUsername;
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return consoleUUID;
    }

    @Override
    public int getPing() {
        return 0;
    }

    @Override
    @NotNull
    public String getServerName() {
        return plugin.getPlatform();
    }

    @Override
    public @NotNull String getAddress() {
        return "0.0.0.0";
    }

    @Override
    public int getPlayersOnServer() {
        return plugin.getOnlinePlayers().size();
    }

    @Override
    public boolean hasPermission(String node) {
        return true;
    }

    @NotNull
    @Override
    public Audience getAudience() {
        return plugin.getConsole();
    }


    @NotNull
    public static ConsolePlayer create(@NotNull IAbeo plugin) {
        return new ConsolePlayer(plugin);
    }

    public static boolean isConsolePlayer(@NotNull UUID uuid) {
        return uuid.equals(consoleUUID);
    }

    public static boolean isConsolePlayer(@NotNull String username) {
        return username.equalsIgnoreCase(consoleUsername);
    }

}
