package com.letcute.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityDamageEvent;

import com.letcute.LetElo;
import com.letcute.config.ConfigElo;
import com.letcute.database.EloTable;

import lombok.Getter;

public class EloManager {

    public LetElo plugin;

    @Getter
    public EloTable eloTable;

    @Getter
    private Map<String, String> placeholderData = new HashMap<>();

    private Map<String, Long> top10Elo = new HashMap<>();

    public EloManager(LetElo plugin) {
        this.plugin = plugin;
        this.eloTable = plugin.getSqliteDB().getEloTable();

        top10Elo = eloTable.getTop10Elo();
        updatePlaceHolerAPI();
    }

    public void updatePlaceHolerAPI() {
        int index = 1;
        for (Map.Entry<String, Long> entry : top10Elo.entrySet()) {
            String name = entry.getKey();
            Long elo = entry.getValue();
            placeholderData.put("top_" + index + "_name", name);
            placeholderData.put("top_" + index + "_elo", String.valueOf(elo));
            index++;
        }
    }

    public void checkKill(Player victim, ConfigElo configElo) {
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage == null || lastDamage.getEntity() == null) {
            return;
        }

        Entity damager = lastDamage.getEntity();

        Map<Class<? extends Entity>, Function<Entity, Player>> damageSources = Map.of(
                ThrownPotion.class, e -> ((ThrownPotion) e).getShooter() instanceof Player p ? p : null,
                Arrow.class, e -> ((Arrow) e).getShooter() instanceof Player p ? p : null,
                TNTPrimed.class, e -> ((TNTPrimed) e).getSource() instanceof Player p ? p : null,
                Fireball.class, e -> ((Fireball) e).getShooter() instanceof Player p ? p : null);

        Player attacker = damageSources.getOrDefault(damager.getClass(), e -> null).apply(damager);
        if (attacker == null) {
            return;
        }

        Map<Class<? extends Entity>, Long> eloPointsMap = Map.of(
                ThrownPotion.class, configElo.getPoison(),
                Arrow.class, configElo.getBow(),
                TNTPrimed.class, configElo.getTNT(),
                Fireball.class, configElo.getFireball());

        Long eloPoints = eloPointsMap.get(damager.getClass());
        if (eloPoints != null) {
            updateEloAndNotify(attacker, victim, eloPoints, configElo);
        }
    }

    public void killPlayer(Player killer, Player victim, ConfigElo configElo) {

        Long sword = configElo.getSword();
        updateEloAndNotify(killer, victim, sword, configElo);
    }

    private void updateEloAndNotify(Player player, Player victim, Long elo, ConfigElo configElo) {
        if (player.getName().equals(victim.getName()))
            return;
        eloTable.addElo(player.getName(), elo);
        eloTable.addElo(victim.getName(), -elo);

        player.sendMessage(configElo.getMessageKill().replace("{elo}", String.valueOf(elo)));
        victim.sendMessage(configElo.getMessageDeath().replace("{elo}", String.valueOf(elo)));

        updateTop(player.getName(), eloTable.getElo(player.getName()));
        updateTop(victim.getName(), eloTable.getElo(victim.getName()));
    }

    public void updateTop(String namePlayer, Long eloPlayer) {
        Map<String, Long> previousTop10Elo = new HashMap<>(top10Elo);

        boolean playerUpdated = top10Elo.containsKey(namePlayer) || top10Elo.size() < 10;

        if (!playerUpdated) {
            Map.Entry<String, Long> lowestEloEntry = Collections.min(top10Elo.entrySet(), Map.Entry.comparingByValue());
            if (eloPlayer > lowestEloEntry.getValue()) {
                top10Elo.remove(lowestEloEntry.getKey());
                playerUpdated = true;
            }
        }

        if (playerUpdated) {
            top10Elo.put(namePlayer, eloPlayer);
        }

        top10Elo = top10Elo.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        if (!top10Elo.equals(previousTop10Elo)) {
            updatePlaceHolerAPI();
        }
    }

}
