package eu.mikart.abeovanish;

import eu.mikart.abeovanish.config.ConfigProvider;
import eu.mikart.abeovanish.database.Database;
import eu.mikart.abeovanish.database.MySqlDatabase;
import eu.mikart.abeovanish.database.SqLiteDatabase;
import eu.mikart.abeovanish.user.Player;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.logging.Logger;

public interface IAbeo extends ConfigProvider {

    Logger getLogger();

    Database getDatabase();

    @NotNull
    String getPlatform();

    @NotNull
    Audience getConsole();

    @NotNull
    default Database loadDatabase() throws RuntimeException {
        final Database.Type databaseType = getSettings().getDatabase().getType();
        final Database database = switch (databaseType) {
            case MYSQL, MARIADB -> new MySqlDatabase(this);
            case SQLITE -> new SqLiteDatabase(this);
        };
        database.initialize();
        getLogger().info("Successfully initialized the " + databaseType.getDisplayName() + " database");
        return database;
    }

    default void reload() {
        loadConfig();
    }

    Collection<Player> getOnlinePlayers();

    Collection<Player> getOnlinePlayersOnServer(@NotNull Player player);

    Collection<eu.mikart.abeovanish.user.Player> getOnlineVanishedPlayers();
}