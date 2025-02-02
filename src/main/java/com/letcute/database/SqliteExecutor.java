package com.letcute.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteExecutor {

    private final SqliteDB sqliteDB;

    public SqliteExecutor(SqliteDB sqliteDB) {
        this.sqliteDB = sqliteDB;
    }

    public ResultSet query(String sql, Object... args) throws SQLException {
        Connection connection = sqliteDB.connect();
        PreparedStatement statement = prepareStatement(connection, sql, args);

        return statement.executeQuery();
    }

    public int update(String sql, Object... args) {
        try (Connection connection = sqliteDB.connect();
                PreparedStatement statement = prepareStatement(connection, sql, args)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            sqliteDB.getPlugin().getLogger().warning("Error executing update: " + e.getMessage());
            return 0;
        }
    }

    private PreparedStatement prepareStatement(Connection connection, String sql, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
        return statement;
    }
}
