package eu.mikart.abeovanish.database;

import eu.mikart.abeovanish.IAbeo;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public final class SqLiteDatabase extends Database {

	/**
	 * The name of the database file
	 */
	private static final String DATABASE_FILE_NAME = "database.db";

	/**
	 * Path to the SQLite database.db file
	 */
	private final File databaseFile;

	/**
	 * The persistent SQLite database connection
	 */
	private Connection connection;

	public Connection getConnection() throws SQLException {
		if (connection == null) {
			setConnection();
		} else if (connection.isClosed()) {
			setConnection();
		}
		return connection;
	}

	private void setConnection() {
		try {
			// Ensure that the database file exists
			if (databaseFile.createNewFile()) {
				plugin.getLogger().info("Created the SQLite database file");
			}

			// Specify use of the JDBC SQLite driver
			Class.forName("org.sqlite.JDBC");

			// Set SQLite database properties
			SQLiteConfig config = new SQLiteConfig();
			config.enforceForeignKeys(true);
			config.setEncoding(SQLiteConfig.Encoding.UTF8);
			config.setSynchronous(SQLiteConfig.SynchronousMode.FULL);

			// Establish the connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath(), config.toProperties());
		} catch (IOException e) {
			plugin.getLogger().warning("An exception occurred creating the database file " + e);
		} catch (SQLException e) {
			plugin.getLogger().warning("An SQL exception occurred initializing the SQLite database " + e);
		} catch (ClassNotFoundException e) {
			plugin.getLogger().warning("Failed to load the necessary SQLite driver " + e);
		}
	}

	public SqLiteDatabase(@NotNull IAbeo plugin) {
		super(plugin);
		this.databaseFile = new File(plugin.getConfigDirectory().toFile(), DATABASE_FILE_NAME);
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

		// Backup database file
		this.backupFlatFile(databaseFile);

		// Create tables
		if (!isCreated()) {
			plugin.getLogger().info("Creating SQLite database tables");
			try {
				executeScript(getConnection(), "sqlite_schema.sql");
			} catch (SQLException e) {
				plugin.getLogger().warning("Failed to create SQLite database tables");
				setLoaded(false);
				return;
			}
			setSchemaVersion(Migration.getLatestVersion());
			plugin.getLogger().info("SQLite database tables created!");
			setLoaded(true);
			return;
		}

		// Perform migrations
		try {
			performMigrations(getConnection(), Type.SQLITE);
			setLoaded(true);
		} catch (SQLException e) {
			plugin.getLogger().warning("Failed to perform SQLite database migrations");
			setLoaded(false);
		}
	}

	@Override
	public boolean isCreated() {
		if (!databaseFile.exists()) {
			return false;
		}
		try (PreparedStatement statement = getConnection().prepareStatement(format("""
				SELECT `uuid`
				FROM `%user_data%`
				LIMIT 1;"""))) {
			statement.executeQuery();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public int getSchemaVersion() {
		try (PreparedStatement statement = getConnection().prepareStatement(format("""
				SELECT `schema_version`
				FROM `%meta_data%`
				LIMIT 1;"""))) {
			final ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				return resultSet.getInt("schema_version");
			}
		} catch (SQLException e) {
			plugin.getLogger().warning("The database schema version could not be fetched; migrations will be carried out.");
		}
		return -1;
	}

	@Override
	public void setSchemaVersion(int version) {
		if (getSchemaVersion() == -1) {
			try (PreparedStatement insertStatement = getConnection().prepareStatement(format("""
					INSERT INTO `%meta_data%` (`schema_version`)
					VALUES (?);"""))) {
				insertStatement.setInt(1, version);
				insertStatement.executeUpdate();
			} catch (SQLException e) {
				plugin.getLogger().warning("Failed to insert schema version in table: " + e);
			}
			return;
		}

		try (PreparedStatement statement = getConnection().prepareStatement(format("""
				UPDATE `%meta_data%`
				SET `schema_version` = ?;"""))) {
			statement.setInt(1, version);
			statement.executeUpdate();
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
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			plugin.getLogger().warning("Failed to close connection: " + e);
		}
	}

}
