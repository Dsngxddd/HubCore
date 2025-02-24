package net.cengiz1.lunehubcore.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String prefix;
    private Connection connection;

    public static void initialize(String host, int port, String database, String username, String password, String prefix) {
        instance = new DatabaseManager(host, port, database, username, password, prefix);
    }

    private DatabaseManager(String host, int port, String database, String username, String password, String prefix) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.prefix = prefix;

        this.connect();
        this.createTables();
    }

    private void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    return;
                }

                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + database +
                                "?useSSL=false&autoReconnect=true",
                        username, password);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "stats (" +
                            "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "username VARCHAR(16) NOT NULL, " +
                            "last_lobby VARCHAR(32), " +
                            "join_count INT DEFAULT 0, " +
                            "last_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")"
            );
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}