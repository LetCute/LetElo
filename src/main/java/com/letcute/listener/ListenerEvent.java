package com.letcute.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.letcute.LetElo;
import com.letcute.config.ConfigElo;
import com.letcute.database.EloTable;
import com.letcute.manager.EloManager;

import lombok.Getter;

public class ListenerEvent implements Listener {

    @Getter
    private LetElo letElo;

    @Getter
    private EloManager eloManager;

    public ListenerEvent(LetElo letElo) {

        this.letElo = letElo;
        this.eloManager = letElo.getEloManager();
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        ConfigElo configElo = letElo.getConfigElo();

        Player killer = victim.getKiller();

        if (killer != null) {
            eloManager.killPlayer(killer, victim, configElo);
            return;
        }
        eloManager.checkKill(victim, configElo);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        EloTable eloTable = letElo.getEloManager().getEloTable();
        eloTable.setElo(player.getName(), 1200L);

    }

}
