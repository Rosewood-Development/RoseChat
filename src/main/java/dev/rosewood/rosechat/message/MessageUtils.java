package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

public class MessageUtils {

    public static final char[] COLORS = { '1', '2', '3', '4',
            '5', '6', '7', '8',
            '9', '0', 'a', 'b',
            'c', 'd', 'e', 'f' };
    public static final char[] FORMATTING = { 'l', 'm', 'n', 'o'};
    public static final char MAGIC = 'k';
    public static final Pattern COLOR_PATTERN = Pattern.compile("(?i)&[0-9a-f]");
    public static final Pattern FORMATTING_PATTERN = Pattern.compile("(?i)&[l-o]");
    public static final Pattern HEX_PATTERN = Pattern.compile("(?i)#[0-9a-f]{6}|#[0-9a-f]{3}");
    public static final Pattern RAINBOW_PATTERN = Pattern.compile("<(?<type>rainbow|r)(#(?<speed>\\d+))?(:(?<saturation>\\d*\\.?\\d+))?(:(?<brightness>\\d*\\.?\\d+))?(:(?<loop>l|L|loop))?>");
    public static final Pattern GRADIENT_PATTERN = Pattern.compile("<(?<type>gradient|g)(#(?<speed>\\d+))?(?<hex>(:#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})){2,})(:(?<loop>l|L|loop))?>");
    public static final Pattern URL_PATTERN = Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{5,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&=]*)");
    public static final Pattern REPLACEMENT_PATTERN = Pattern.compile("(?=\uD954)");
    public static final String REPLACEMENT_CHARACTER = "\uD954";

    public static String stripAccents(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFKD);

        for (char c : string.toCharArray()) {
            if (c <= '\u007F') sb.append(c);
        }

        return sb.toString();
    }

    public static double getLevenshteinDistancePercent(String first, String second) {
        int levDistance = StringUtils.getLevenshteinDistance(MessageUtils.stripAccents(first.toLowerCase()),
                MessageUtils.stripAccents(second.toLowerCase()));

        String longerMessage = second;

        if (second.length() < first.length()) longerMessage = first;
        return (longerMessage.length() - levDistance) / (double) longerMessage.length();
    }

    public static boolean isCaps(String message) {
        int caps = 0;
        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (Character.isAlphabetic(ch) && ch == Character.toUpperCase(ch)) caps++;
        }

        return caps > ConfigurationManager.Setting.MAXIMUM_CAPS_ALLOWED.getInt();
    }

    public static boolean isMessageEmpty(String message) {
        String colorified = HexUtils.colorify(message);
        return ChatColor.stripColor(colorified).isEmpty();
    }

    public static void sendPrivateMessage(CommandSender sender, String targetName, String message) {
        Player target = Bukkit.getPlayer(targetName);
        MessageSender messageSender = new MessageSender(sender);
        MessageSender messageTarget = target == null ? new MessageSender(targetName, "") : new MessageSender(target);

        MessageWrapper sentMessage = new MessageWrapper(messageSender.getName() + " -> " + messageTarget.getName(), messageSender, message);
        MessageWrapper receivedMessage = new MessageWrapper(messageSender.getName() + " -> " + messageTarget.getName(), messageSender, message);
        MessageWrapper spyMessage = new MessageWrapper("[Spy] " + messageSender.getName() + " -> " + messageTarget.getName(), messageSender, message);

        if (sender instanceof Player) {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            if (offlineTarget != null) {
                PlayerData targetData = RoseChatAPI.getInstance().getDataManager().getPlayerData(offlineTarget.getUniqueId());
                if (targetData != null && targetData.getIgnoringPlayers().contains(((Player) sender).getUniqueId())) {
                    RoseChatAPI.getInstance().getLocaleManager().sendMessage(sender, "command-togglemessage-cannot-message");
                    return;
                }
            }
        }

        sender.spigot().sendMessage(sentMessage.getComponents());
        if (target == null) {
            if (targetName.equalsIgnoreCase("Console")) {
                Bukkit.getConsoleSender().spigot().sendMessage(sentMessage.getComponents());
                return;
            }
            BungeeListener.sendDirectMessage(messageSender.getUUID(), targetName, ComponentSerializer.toString(receivedMessage.getComponents()));
        } else {
            target.spigot().sendMessage(receivedMessage.getComponents());
        }

        if (sender instanceof Player) {
            for (UUID uuid : RoseChatAPI.getInstance().getDataManager().getMessageSpies()) {
                if (!uuid.equals(messageSender.asPlayer().getUniqueId()) && !uuid.equals(messageTarget.asPlayer().getUniqueId())) {
                    Player spy = Bukkit.getPlayer(uuid);
                    if (spy != null) spy.spigot().sendMessage(spyMessage.getComponents());
                }
            }
        }
    }
}
