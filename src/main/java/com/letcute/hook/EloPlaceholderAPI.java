package com.letcute.hook;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.letcute.LetElo;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class EloPlaceholderAPI extends PlaceholderExpansion {

    private LetElo letElo;

    public EloPlaceholderAPI(LetElo letElo) {
        this.letElo = letElo;

    }

    @Override
    public @Nullable String getAuthor() {
        return "LetCute";
    }

    @Override
    public @Nullable String getIdentifier() {
        return "elo";
    }

    @Override
    public @Nullable String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier.equals("player")) {
            Long elo = letElo.getEloManager().getEloTable().getElo(player.getName());
            return formatElo(elo);
        }
        String result = letElo.getEloManager().getPlaceholderData().get(identifier);
        return result != null ? result : "N/A";
    }

    private String formatElo(long elo) {
        if (elo >= 1_000_000_000) {
            return String.format("%.1fb", elo / (double) 1_000_000_000);
        }
        if (elo >= 1_000_000) {
            return String.format("%.1fm", elo / (double) 1_000_000);
        }
        if (elo >= 1_000) {
            return String.format("%.1fk", elo / (double) 1_000);
        }
        return String.valueOf(elo);
    }
}
