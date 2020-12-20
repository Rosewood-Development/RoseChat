package dev.rosewood.rosechat.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

public class MessageUtils {

    public static final Pattern URL_PATTERN = Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&=]*)");

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

    public static void sendStandardMessage(CommandSender sender, MessageWrapper messageWrapper, ChatChannel channel) {
        if (messageWrapper.isEmpty()) return;
        if (messageWrapper.isBlocked()) {
            if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().sendWarning(sender);
            return;
        }

        for (UUID recieverUuid : channel.getPlayers()) {
            Player receiver = Bukkit.getPlayer(recieverUuid);
            if (receiver == null) continue;
            messageWrapper.parsePlaceholders(channel.getFormatId(), receiver);
            messageWrapper.send(receiver);
        }

        messageWrapper.send(Bukkit.getConsoleSender());
        messageWrapper.tagPlayers();
    }
}
