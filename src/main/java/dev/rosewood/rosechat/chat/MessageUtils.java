package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.managers.ConfigurationManager;
import dev.rosewood.rosechat.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

public class MessageUtils {

    public static final Pattern URL_PATTERN = Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&=]*)");
    public static final char[] COLORS = { '1', '2', '3', '4',
            '5', '6', '7', '8',
            '9', '0', 'a', 'b',
            'c', 'd', 'e', 'f' };
    public static final char[] FORMATTING = { 'l', 'm', 'n', 'o'};
    public static final char MAGIC = 'k';

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
        sendStandardMessage(sender, messageWrapper, channel, true);
    }

    public static void sendStandardMessage(CommandSender sender, MessageWrapper messageWrapper, ChatChannel channel, boolean sendToConsole) {
        if (messageWrapper.isEmpty()) return;
        if (messageWrapper.isBlocked()) {
            if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().sendWarning(sender);
            return;
        }

        for (UUID recieverUuid : channel.getPlayers()) {
            Player receiver = Bukkit.getPlayer(recieverUuid);
            if (receiver == null) continue;
            messageWrapper.parse(channel.getFormatId(), receiver);
            messageWrapper.send(receiver);
        }

        if (sendToConsole) messageWrapper.send(Bukkit.getConsoleSender());
        messageWrapper.tagPlayers();
    }

    public static void sendPrivateMessage(DataManager dataManager, PlayerData playerData, PlayerData targetData, String message) {
        Player player = Bukkit.getPlayer(playerData.getUuid());
        Player target = Bukkit.getPlayer(targetData.getUuid());

        MessageWrapper messageSentWrapper = new MessageWrapper(player, message)
                .checkAll("rosechat.message")
                .filterAll("rosechat.message")
                .withReplacements()
                .withTags()
                .parse("message-sent", target);

        MessageWrapper messageReceivedWrapper = new MessageWrapper(target, message)
                .checkAll("rosechat.message")
                .filterAll("rosechat.message")
                .withReplacements()
                .withTags()
                .parse("message-received", player);

        MessageWrapper spyMessageWrapper = new MessageWrapper(player, message)
                .checkAll("rosechat.message")
                .filterAll("rosechat.message")
                .withReplacements()
                .withTags()
                .parse("social-spy", target);

        if (messageSentWrapper.isEmpty()) return;
        if (messageSentWrapper.isBlocked()) {
            if (messageSentWrapper.getFilterType() != null) messageSentWrapper.getFilterType().sendWarning(player);
            return;
        }

        player.spigot().sendMessage(messageSentWrapper.build());
        target.spigot().sendMessage(messageReceivedWrapper.build());

        for (UUID uuid : dataManager.getSocialSpies()) {
            if (uuid.equals(player.getUniqueId()) || uuid.equals(target.getUniqueId())) continue;
            Player spy = Bukkit.getPlayer(uuid);
            if (spy != null) spy.spigot().sendMessage(spyMessageWrapper.build());
        }

        try {
            if (targetData.hasMessageSounds()) {
                Sound sound = Sound.valueOf(ConfigurationManager.Setting.MESSAGE_SOUND.getString());
                target.playSound(target.getLocation(), sound, 1, 1);
            }
        } catch (Exception e) {

        }
    }
}
