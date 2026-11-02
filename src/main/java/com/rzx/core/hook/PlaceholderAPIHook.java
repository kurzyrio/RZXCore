package com.rzx.core.hook;

import com.rzx.core.data.PlayerData;
import com.rzx.core.data.PlayerDataService;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * PlaceholderAPI expansion registering {@code RZXCore_*} placeholders.
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final Plugin plugin;
    private final PlayerDataService playerDataService;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public PlaceholderAPIHook(Plugin plugin, PlayerDataService playerDataService) {
        this.plugin = plugin;
        this.playerDataService = playerDataService;
    }

    @Override public String getIdentifier() { return "rzxcore"; }
    @Override public String getAuthor() { return "RZX"; }
    @Override public String getVersion() { return "1.0.0"; }
    @Override public boolean persist() { return true; }
    @Override public boolean canRegister() { return true; }
    @Override public String getRequiredPlugin() { return plugin.getName(); }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "N/A";

        PlayerData data = playerDataService.getPlayerData(player.getUniqueId());
        if (data == null) return "N/A";

        return switch (params) {
            case "playtime" -> {
                Duration duration = Duration.between(
                        Instant.ofEpochMilli(data.getFirstJoin()),
                        Instant.ofEpochMilli(data.getLastSeen()));
                yield duration.toHours() + "h " + duration.toMinutesPart() + "m";
            }
            case "first_join" -> FORMATTER.format(Instant.ofEpochMilli(data.getFirstJoin()));
            case "last_seen" -> FORMATTER.format(Instant.ofEpochMilli(data.getLastSeen()));
            default -> null;
        };
    }

    /** Resolves PlaceholderAPI placeholders in text for the given player. */
    public static String resolvePlaceholders(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}

