package eu.mikart.abeovanish.database;

import com.zaxxer.hikari.HikariDataSource;
import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.config.Settings;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

public final class MySqlDatabase extends Database {

    private static final String DATA_POOL_NAME = "AbeoVanishHikariPool";
    private final String flavor;
    private final String driverClass;
    private HikariDataSource dataSource;

    public MySqlDatabase(@NotNull IAbeo plugin) {
        super(plugin);
        this.flavor = plugin.getSettings().getDatabase().getType() == Type.MARIADB
                ? "mariadb" : "mysql";
        this.driverClass = plugin.getSettings().getDatabase().getType() == Type.MARIADB
                ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver";
    }
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void setConnection() {
        // Initialize the Hikari pooled connection
        final Settings.DatabaseSettings databaseSettings = plugin.getSettings().getDatabase();
        final Settings.DatabaseSettings.DatabaseCredentials credentials = databaseSettings.getCredentials();

        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s%s",
                flavor,
                credentials.getHost(),
                credentials.getPort(),
                credentials.getDatabase(),
                credentials.getParameters()
        ));

        // Authenticate with the database
        dataSource.setUsername(credentials.getUsername());
        dataSource.setPassword(credentials.getPassword());

        // Set connection pool options
        final Settings.DatabaseSettings.PoolOptions poolOptions = databaseSettings.getConnectionPool();
        dataSource.setMaximumPoolSize(poolOptions.getSize());
        dataSource.setMinimumIdle(poolOptions.getIdle());
        dataSource.setMaxLifetime(poolOptions.getLifetime());
        dataSource.setKeepaliveTime(poolOptions.getKeepalive());
        dataSource.setConnectionTimeout(poolOptions.getTimeout());
        dataSource.setPoolName(DATA_POOL_NAME);

        // Set additional connection pool properties
        final Properties properties = new Properties();
        properties.putAll(
                Map.of("cachePrepStmts", "true",
                        "prepStmtCacheSize", "250",
                        "prepStmtCacheSqlLimit", "2048",
                        "useServerPrepStmts", "true",
                        "useLocalSessionState", "true",
                        "useLocalTransactionState", "true"
                ));
        properties.putAll(
                Map.of(
                        "rewriteBatchedStatements", "true",
                        "cacheResultSetMetadata", "true",
                        "cacheServerConfiguration", "true",
                        "elideSetAutoCommits", "true",
                        "maintainTimeStats", "false")
        );
        dataSource.setDataSourceProperties(properties);
    }

    @Override
    protected void executeScript(@NotNull Connection connection, @NotNull String name) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String schemaStatement : getScript(name)) {
                statement.execute(schemaStatement);
            }
        }
    }

    @Override
    public void initialize() throws RuntimeException {
        // Establish connection
        this.setConnection();

        // Create tables
        final Database.Type type = plugin.getSettings().getDatabase().getType();
        if (!isCreated()) {
            plugin.getLogger().log(Level.INFO, String.format("Creating %s database tables", type.getDisplayName()));
            try (Connection connection = getConnection()) {
                executeScript(connection, String.format("%s_schema.sql", flavor));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, String.format("Failed to create %s database tables", type.getDisplayName()), e);
                setLoaded(false);
                return;
            }
            setSchemaVersion(Migration.getLatestVersion());
            plugin.getLogger().log(Level.INFO, String.format("Created %s database tables", type.getDisplayName()));
            setLoaded(true);
            return;
        }

        // Perform migrations
        try {
            performMigrations(getConnection(), type);
            setLoaded(true);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("Failed to perform %s database migrations", type.getDisplayName()), e);
            setLoaded(false);
        }
    }


    // Select a table to check if the database has been created
    @Override
    public boolean isCreated() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `uuid`
                    FROM `%user_data%`
                    LIMIT 1;"""))) {
                statement.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int getSchemaVersion() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `schema_version`
                    FROM `%meta_data%`
                    LIMIT 1;"""))) {
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("schema_version");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("The database schema version could not be fetched; migrations will be carried out.");
        }
        return -1;
    }

    @Override
    public void setSchemaVersion(int version) {
        if (getSchemaVersion() == -1) {
            try (Connection connection = getConnection()) {
                try (PreparedStatement insertStatement = connection.prepareStatement(format("""
                        INSERT INTO `%meta_data%` (`schema_version`)
                        VALUES (?)"""))) {
                    insertStatement.setInt(1, version);
                    insertStatement.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to insert schema version in table: " + e);
            }
            return;
        }

        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    UPDATE `%meta_data%`
                    SET `schema_version` = ?;"""))) {
                statement.setInt(1, version);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to update schema version in table: " + e);
        }
    }

    // START OF SQL THINGS

    public void setVanishState(@NotNull UUID uuid, boolean state) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    INSERT INTO `%user_data%` (`uuid`, `vanished`)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE `vanished` = ?;"""))) {
                statement.setString(1, uuid.toString());
                statement.setBoolean(2, state);
                statement.setBoolean(3, state);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to set vanish state for " + uuid, e);
        }
    }

    public boolean getVanishState(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `vanished`
                    FROM `%user_data%
                    WHERE `uuid` = ?;"""))) {
                statement.setString(1, uuid.toString());
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getBoolean("vanished");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get vanish state for " + uuid, e);
        }
        return false;
    }

    // END OF SQL THINGS

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
