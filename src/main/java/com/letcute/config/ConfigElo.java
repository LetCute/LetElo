package com.letcute.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.letcute.LetElo;

import java.io.File;
import java.io.IOException;

public class ConfigElo {
    private final LetElo plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigElo(LetElo plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false); // Sao chép file mặc định nếu chưa có
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public Long getSword() {
        return config.getLong("sword", 1); // Trả về 1200 nếu không có trong file
    }

    public Long getBow() {
        return config.getLong("bow", 2);
    }

    public Long getTNT() {
        return config.getLong("tnt", 3);
    }

    public Long getFireball() {
        return config.getLong("fireball", 4);
    }

    public Long getPoison() {
        return config.getLong("poison", 5);
    }

    public String getMessageKill() {
        return config.getString("message-kill", "You have received an additional {elo} elo");
    }

    public String getMessageDeath() {
        return config.getString("message-death", "You have lost {elo} elo");
    }

    public void setEloConfig(String path, Object value) {
        config.set(path, value);
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Lỗi khi lưu config.yml: " + e.getMessage());
        }
    }
}
