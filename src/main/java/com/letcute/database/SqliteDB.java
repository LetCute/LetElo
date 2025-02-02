package com.letcute.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.letcute.LetElo;

import lombok.Getter;

public class SqliteDB {

    @Getter
    private final LetElo plugin;
    @Getter
    private Connection connection;

    @Getter
    private final SqliteExecutor sqliteExecutor;

    @Getter
    private EloTable eloTable;

    public SqliteDB(LetElo plugin) {
        this.plugin = plugin;
        this.sqliteExecutor = new SqliteExecutor(this);
        this.eloTable = new EloTable(plugin, this.sqliteExecutor);
    }

    public Connection connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                plugin.getLogger().warning("Không thể tạo thư mục plugin: " + dataFolder.getAbsolutePath());
                return null;
            }

            if (connection == null || connection.isClosed()) {
                connection = DriverManager
                        .getConnection("jdbc:sqlite:" + new File(dataFolder, "elo.db").getAbsolutePath());
                plugin.getLogger()
                        .info("Kết nối thành công đến SQLite: " + new File(dataFolder, "elo.db").getAbsolutePath());
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Không thể kết nối đến SQLite: " + e.getMessage());
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Đóng kết nối SQLite thành công.");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Lỗi khi đóng kết nối SQLite: " + e.getMessage());
        }
    }
}
