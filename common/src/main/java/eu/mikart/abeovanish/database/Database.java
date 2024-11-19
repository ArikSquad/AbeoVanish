package eu.mikart.abeovanish.database;

import eu.mikart.abeovanish.IAbeo;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Database {

    protected final IAbeo plugin;
    private boolean loaded;

    protected Database(@NotNull IAbeo plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the schema statements from the schema file
     *
     * @return the {@link #format formatted} schema statements
     */
    @NotNull
    protected final String[] getScript(@NotNull String name) {
        name = (name.startsWith("database/") ? "" : "database/") + name + (name.endsWith(".sql") ? "" : ".sql");
        try (InputStream schemaStream = Objects.requireNonNull(plugin.getResource(name))) {
            final String schema = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
            return format(schema).split(";");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load database schema", e);
        }
        return new String[0];
    }

    protected abstract void executeScript(@NotNull Connection connection, @NotNull String name) throws SQLException;

    /**
     * Format a string for use in an SQL query
     *
     * @param statement The SQL statement to format
     * @return The formatted SQL statement
     */
    @NotNull
    protected final String format(@NotNull String statement) {
        final Pattern pattern = Pattern.compile("%(\\w+)%");
        final Matcher matcher = pattern.matcher(statement);
        final StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            final TableName tableName = TableName.match(matcher.group(1));
            matcher.appendReplacement(sb, plugin.getSettings().getDatabase().getTableName(tableName));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Backup a flat file database
     *
     * @param file the file to back up
     */
    protected final void backupFlatFile(@NotNull File file) {
        if (!file.exists()) {
            return;
        }

        final File backup = new File(file.getParent(), String.format("%s.bak", file.getName()));
        try {
            if (!backup.exists() || backup.delete()) {
                Files.copy(file.toPath(), backup.toPath());
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to backup flat file database", e);
        }
    }

    /**
     * Initialize the database connection
     *
     * @throws RuntimeException if the database initialization fails
     */
    public abstract void initialize() throws RuntimeException;

    public abstract boolean isCreated();

    /**
     * Perform database migrations
     *
     * @param connection the database connection
     * @throws SQLException if an SQL error occurs during migration
     */
    protected final void performMigrations(@NotNull Connection connection, @NotNull Type type) throws SQLException {
        final int currentVersion = getSchemaVersion();
        final int latestVersion = Migration.getLatestVersion();
        if (currentVersion < latestVersion) {
            plugin.getLogger().log(Level.INFO, "Performing database migrations (Target version: v" + latestVersion + ")");
            for (Migration migration : Migration.getOrderedMigrations()) {
                if (!migration.isSupported(type)) {
                    continue;
                }
                if (migration.getVersion() > currentVersion) {
                    try {
                        plugin.getLogger().log(Level.INFO, "Performing database migration: " + migration.getMigrationName()
                            + " (v" + migration.getVersion() + ")");
                        final String scriptName = "migrations/" + migration.getVersion() + "-" + type.name().toLowerCase() +
                            "-" + migration.getMigrationName() + ".sql";
                        executeScript(connection, scriptName);
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.WARNING, "Migration " + migration.getMigrationName()
                            + " (v" + migration.getVersion() + ") failed; skipping", e);
                    }
                }
            }
            setSchemaVersion(latestVersion);
            plugin.getLogger().log(Level.INFO, "Completed database migration (Target version: v" + latestVersion + ")");
        }
    }

    /**
     * Get the database schema version
     *
     * @return the database schema version
     */
    public abstract int getSchemaVersion();

    /**
     * Set the database schema version
     *
     * @param version the database schema version
     */
    public abstract void setSchemaVersion(int version);

    // START OF SQL THINGS

    public abstract void setVanishState(@NotNull UUID uuid, boolean state);
    public abstract boolean getVanishState(@NotNull UUID uuid);

    // END OF SQL THINGS

    /**
     * Close the database connection
     */
    public abstract void close();

    /**
     * Check if the database has been loaded
     *
     * @return {@code true} if the database has loaded successfully; {@code false} if it failed to initialize
     */
    public boolean hasLoaded() {
        return loaded;
    }

    /**
     * Set if the database has loaded
     *
     * @param loaded whether the database has loaded successfully
     */
    protected void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * Identifies types of databases
     */
    public enum Type {
        MYSQL("MySQL"),
        MARIADB("MariaDB"),
        SQLITE("SQLite");
        @NotNull
        private final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }

        @NotNull
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents the names of tables in the database
     */
    @Getter
    public enum TableName {
        META_DATA("vanish_metadata"),
        USER_DATA("vanish_users");

        @NotNull
        private final String defaultName;

        TableName(@NotNull String defaultName) {
            this.defaultName = defaultName;
        }

        @NotNull
        public static Database.TableName match(@NotNull String placeholder) throws IllegalArgumentException {
            return TableName.valueOf(placeholder.toUpperCase());
        }

        @NotNull
        private Map.Entry<String, String> toEntry() {
            return Map.entry(name().toLowerCase(Locale.ENGLISH), defaultName);
        }

        @NotNull
        @SuppressWarnings("unchecked")
        public static Map<String, String> getDefaults() {
            return Map.ofEntries(Arrays.stream(values())
                .map(TableName::toEntry)
                .toArray(Map.Entry[]::new));
        }

    }

    /**
     * Represents database migrations that need to be run
     */
    public enum Migration {
        ADD_METADATA_TABLE(
            0, "add_metadata_table",
            Type.MYSQL, Type.MARIADB, Type.SQLITE
        );

        private final int version;
        private final String migrationName;
        private final Type[] supportedTypes;

        Migration(int version, @NotNull String migrationName, @NotNull Type... supportedTypes) {
            this.version = version;
            this.migrationName = migrationName;
            this.supportedTypes = supportedTypes;
        }

        private int getVersion() {
            return version;
        }

        private String getMigrationName() {
            return migrationName;
        }

        private boolean isSupported(@NotNull Type type) {
            return Arrays.stream(supportedTypes).anyMatch(supportedType -> supportedType == type);
        }

        public static List<Migration> getOrderedMigrations() {
            return Arrays.stream(Migration.values())
                .sorted(Comparator.comparingInt(Migration::getVersion))
                .collect(Collectors.toList());
        }

        public static int getLatestVersion() {
            return getOrderedMigrations().getLast().getVersion();
        }

    }
}
