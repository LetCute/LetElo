package com.letcute;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.letcute.config.ConfigElo;
import com.letcute.database.SqliteDB;
import com.letcute.hook.EloPlaceholderAPI;
import com.letcute.listener.ListenerEvent;
import com.letcute.manager.EloManager;

import lombok.Getter;

/*
 * letello java plugin
 */
public class LetElo extends JavaPlugin {
  private static final Logger LOGGER = Logger.getLogger("LetELo");

  @Getter
  private EloManager eloManager;

  @Getter
  private static LetElo instance;

  @Getter
  private SqliteDB sqliteDB;

  @Getter
  private ConfigElo configElo;

  public void onEnable() {
    instance = this;
    saveDefaultConfig();
    this.configElo = new ConfigElo(this);
    LOGGER.info("LetElo enabled");
    this.sqliteDB = new SqliteDB(this);
    this.eloManager = new EloManager(this);
    getServer().getPluginManager().registerEvents(new ListenerEvent(this), instance);
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new EloPlaceholderAPI(this).register();
      getLogger().info("Enable Elo by PlaceholderAPI");
    }

  }

  public void onDisable() {
    LOGGER.info("LetElo disabled");
  }

}
