package me.lilac.rosechat.chat;

import me.lilac.rosechat.Rosechat;
import me.lilac.rosechat.storage.Messages;
import me.lilac.rosechat.storage.Settings;
import me.lilac.rosechat.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatFilter {

    private Rosechat plugin;
    private Player player;
    private String message;

    public ChatFilter(Player player, String message) {
        this.plugin = Rosechat.getInstance();
        this.player = player;
        this.message = message;
    }

    public String getFilteredMessage() {
        if (Settings.shouldDoCapsCheck() && message != null) filterCaps();
        if (Settings.shouldDoSpamCheck() && message != null) filterSpam();
        if (message != null) replaceLanguage();
        if (message != null) checkColor();

        return message;
    }

    private boolean isCaps() {
        String nm = message.replaceAll("[^A-Za-z0-9]*", "");
        int caps = 0;
        for (int i = 0; i < nm.length(); i++) {
            if (nm.charAt(i) == Character.toUpperCase(nm.charAt(i))) {
                caps++;
            }
        }

        return caps > 8
                && message.matches("[A-Za-z0-9 ]*")
                && !player.hasPermission("rosechat.bypass.caps")
                && message.length() > 5;
    }

    private void filterCaps() {
        if (!isCaps()) return;
        if (Settings.shouldLowercaseCaps()) {
               message = message.toLowerCase();
        } else {
            message = null;
            player.sendMessage(Methods.format(Messages.getBlockedCaps()));
        }
    }

    private boolean isSpam() {
        if (player.hasPermission("rosechat.bypass.spam")) return false;
        int repeatedMessages = 0;
        if (!plugin.getChatManager().getMessages().containsKey(player.getUniqueId())) return false;
        for (String msg : plugin.getChatManager().getMessagesReversed(player.getUniqueId())) {
            if (!message.equalsIgnoreCase(msg)) break;
            if (message.equalsIgnoreCase(msg)) repeatedMessages++;
        }

        if (plugin.getChatManager().getMessages().get(player.getUniqueId()).size() > 4) {
            for (int i = 0; i < 3; i++) {
                plugin.getChatManager().getMessages().get(player.getUniqueId()).remove(0);
            }
        }

        return repeatedMessages > 2;
    }

    private void filterSpam() {
        if (!isSpam()) return;
        message = null;
        player.sendMessage(Methods.format(Messages.getBlockedSpam()));
    }

    private void replaceLanguage() {
        if (player.hasPermission("rosechat.bypass.language")) return;
        for (String blocked : Settings.getBlockedMessages().keySet()) {
            if (!message.toLowerCase().contains(blocked.toLowerCase())) continue;
            if (Settings.getBlockedMessages().get(blocked) == null) {
                player.sendMessage(Methods.format(Messages.getBlockedLanguage()));
                message = null;
                return;
            }

            message = message.replace(blocked, Settings.getBlockedMessages().get(blocked));
        }
    }

    private void checkColor() {
        if (!player.hasPermission("rosechat.format")) {
            message = plugin.getChatManager().removeFormatting(message);
        }

        if (!player.hasPermission("rosechat.color")) {
            message = plugin.getChatManager().removeColours(message);
        }
    }
}
