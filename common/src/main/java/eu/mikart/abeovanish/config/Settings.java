package eu.mikart.abeovanish.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import eu.mikart.abeovanish.database.Database;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class Settings {

    protected static final String CONFIG_HEADER = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
            ┃       AbeoVanish Config      ┃
            ┃    Developed by ArikSquad    ┃
            ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
            ┣╸ Information: link here
            ┣╸ Config Help: link here
            ┗╸ Documentation: link here""";

    @Comment("Locale of the default language file to use. Docs: ")
    private String language = Locales.DEFAULT_LOCALE;

    @Comment("Whether to enable debug mode. This will print additional information to the console.")
    private boolean debug = false;

    @Comment("Whether to enable the plugin's update checker.")
    private boolean updateChecker = true;

    @Comment("Whether to send join/quit messages on vanish")
    private boolean sendJoinQuitMessages = true;

    @Comment("Database settings")
    private DatabaseSettings database = new DatabaseSettings();

    @Comment("Vanish settings")
    private VanishSettings vanishSettings = new VanishSettings();

    @Comment("Functionality settings")
    private FunctionalitySettings functionalitySettings = new FunctionalitySettings();

    @Comment("Settings for hooks which this plugin supports")
    private HookSettings hookSettings = new HookSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DatabaseSettings {

        @Comment("Type of database to use (SQLITE, MYSQL, MARIADB)")
        private Database.Type type = Database.Type.SQLITE;

        @Comment("Specify credentials here for your MYSQL or MARIADB database")
        private DatabaseCredentials credentials = new DatabaseCredentials();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class DatabaseCredentials {
            private String host = "localhost";
            private int port = 3306;
            private String database = "AbeoVanish";
            private String username = "root";
            private String password = "pa55w0rd";
            private String parameters = String.join("&",
                    "?autoReconnect=true", "useSSL=false",
                    "useUnicode=true", "characterEncoding=UTF-8");
        }

        @Comment("MYSQL / MARIADB database Hikari connection pool properties. Don't modify this unless you know what you're doing!")
        private PoolOptions connectionPool = new PoolOptions();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class PoolOptions {
            private int size = 10;
            private int idle = 10;
            private long lifetime = 1800000;
            private long keepalive = 0;
            private long timeout = 5000;
        }

        @Comment("Names of tables to use on your database. Don't modify this unless you know what you're doing!")
        @Getter(AccessLevel.NONE)
        private Map<String, String> tableNames = Database.TableName.getDefaults();

        @NotNull
        public String getTableName(@NotNull Database.TableName tableName) {
            return tableNames.getOrDefault(tableName.name().toLowerCase(Locale.ENGLISH), tableName.getDefaultName());
        }

    }

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FunctionalitySettings {
        @Comment("Whether to prefer the use of packets for chat argument hiding. Default: false (Experimental)")
        private boolean useChatArgumentPacketLevelHiding = false;
    }

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class VanishSettings {
        @Comment("Whether to reappear on world change. Bypass permission: abeovanish.bypass.worldchange")
        private boolean reappearOnWorldChange = false;

        @Comment("Whether to disable pressure plates while vanished")
        private boolean disablePressurePlates = true;

        @Comment("Whether to disable trampling crops while vanished")
        private boolean disableCropTrampling = true;

        @Comment("Whether to disable block breaking while vanished. Bypass permission: abeovanish.bypass.blockbreak")
        private boolean disableBlockBreaking = true;

        @Comment("Whether to disable block placing while vanished. Bypass permission: abeovanish.bypass.blockplace")
        private boolean disableBlockPlacing = true;

        @Comment("What character to use to bypass chat messages while vanished")
        private String bypassChatCharacter = "#";

        @Comment("Whether to enable silent chests. You need to have OpenInv installed for this to work.")
        private boolean silentChests = true;
    }


    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HookSettings {
        @Comment("Settings for other players inventory access while vanished (Requires OpenInv)")
        private OpenInventory openInv = new OpenInventory();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class OpenInventory {
            @Comment("Whether to allow players to open other players inventories while vanished.")
            private boolean enabled = true;
            @Comment("Whether to require players to be on spectator mode to open players inventories.")
            private boolean requireSpectatorToOpenInv = true;
            @Comment("Whether to require players to be sneaking to open players inventories.")
            private boolean requireSneakingToOpenInv = true;
        }
    }
}
