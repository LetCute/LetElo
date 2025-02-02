package com.letcute.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.letcute.LetElo;

public class EloTable {

    private LetElo plugin;

    private SqliteExecutor sqliteExecutor;

    private Map<String, Long> elotable = new HashMap<>();

    private Map<String, Long> top10Elo = new HashMap<>();

    public EloTable(LetElo letElo, SqliteExecutor sqliteExecutor) {
        this.sqliteExecutor = sqliteExecutor;
        this.plugin = letElo;
        createTable();

    }

    public void createTable() {
        sqliteExecutor.update(
                "CREATE TABLE IF NOT EXISTS EloScores ( id INTEGER PRIMARY KEY AUTOINCREMENT, player_name TEXT NOT NULL, elo BIGINT NOT NULL DEFAULT 1200, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP );");
    }

    public Long getElo(String playerName) {
        if (elotable.containsKey(playerName)) {
            return elotable.get(playerName);
        }
        String query = "SELECT elo FROM EloScores WHERE player_name = ?";

        try (ResultSet rs = sqliteExecutor.query(query, playerName)) {
            if (rs != null && rs.next()) {
                return rs.getLong("elo");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error when getting Elo of " + playerName + ": " + e.getMessage());
        }
        insertElo(playerName, 1200L);
        return 1200L;
    }

    public boolean playerExists(String playerName) {
        if (elotable.containsKey(playerName)) {
            return true;
        }
        String query = "SELECT 1 FROM EloScores WHERE player_name = ?";

        try (ResultSet rs = sqliteExecutor.query(query, playerName)) {
            return rs != null && rs.next();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error checking existence of " + playerName + ": " + e.getMessage());
        }

        return false;
    }

    public boolean insertElo(String playerName, Long elo) {
        String query = "INSERT INTO EloScores (player_name, elo) VALUES (?, ?)";
        try {
            int row = sqliteExecutor.update(query, playerName, elo);
            if (row > 0) {
                elotable.put(playerName, elo);
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error adding Elo for " + playerName + ": " + e.getMessage());
        }

        return false;
    }

    public boolean updateElo(String playerName, Long elo) {
        String query = "UPDATE EloScores SET elo = ?, updated_at = CURRENT_TIMESTAMP WHERE player_name = ?;";

        try {
            int row = sqliteExecutor.update(query, elo, playerName);
            if (row > 0) {
                if (elotable.containsKey(playerName)) {
                    elotable.remove(playerName);
                    elotable.put(playerName, elo);
                } else {
                    elotable.put(playerName, elo);
                }

                LetElo.getInstance().getEloManager().updateTop(playerName, elo);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Error while updating Elo for " + playerName + ": " + e.getMessage());
        }

        return false;
    }

    public Map<String, Long> getTop10Elo() {
        String query = "SELECT player_name, elo FROM EloScores ORDER BY elo DESC, updated_at ASC LIMIT 10;";
        try (ResultSet rs = sqliteExecutor.query(query)) {
            top10Elo.clear();
            while (rs.next()) {
                top10Elo.put(rs.getString("player_name"), rs.getLong("elo"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return top10Elo;
    }

    public boolean setElo(String playerName, Long elo) {
        if (playerExists(playerName)) {
            return updateElo(playerName, elo);
        } else {
            return insertElo(playerName, elo);
        }
    }

    public boolean addElo(String playerName, Long elo) {
        if (!playerExists(playerName)) {
            return insertElo(playerName, 1200L);
        }

        Long currentElo = getElo(playerName);
        return updateElo(playerName, currentElo + elo);
    }
}
