package me.lilac.rosechat.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.lilac.rosechat.storage.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Methods {

    public static String format(String message) {
        return message.contains("%prefix%")
                ? ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", Messages.getPrefix()))
                : ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String format(Player player, String message) {
        return PlaceholderAPI.setPlaceholders(player, format(message));
    }

    public static String format(Player player, Player target, String message) {
        if (message.contains("{player}")) message = message.replace("{player}", target.getName());
        if (message.contains("{player_displayname}")) message = message.replace("{player_displayname}", target.getDisplayName());

        return PlaceholderAPI.setPlaceholders(player, format(message));
    }

    public static void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(format("&8[&cRosechat&8] " + message));
    }
}
