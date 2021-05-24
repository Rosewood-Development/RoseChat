package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class MessageUtils {

    public static final char[] COLORS = { '1', '2', '3', '4',
            '5', '6', '7', '8',
            '9', '0', 'a', 'b',
            'c', 'd', 'e', 'f' };
    public static final char[] FORMATTING = { 'l', 'm', 'n', 'o'};
    public static final char MAGIC = 'k';
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

    public static void sendStandardMessage(CommandSender sender, MessageWrapperOld messageWrapperOld, ChatChannel channel) {
        sendStandardMessage(sender, messageWrapperOld, channel, true);
    }

    public static void sendStandardMessage(CommandSender sender, MessageWrapperOld messageWrapperOld, ChatChannel channel, boolean sendToConsole) {
        /*if (messageWrapperOld.isEmpty()) return;
        if (messageWrapperOld.isBlocked()) {
            if (messageWrapperOld.getFilterType() != null) messageWrapperOld.getFilterType().sendWarning(sender);
            return;
        }

        for (UUID recieverUuid : channel.getPlayers()) {
            Player receiver = Bukkit.getPlayer(recieverUuid);
            if (receiver == null) continue;
            messageWrapperOld.parse(channel.getFormatId(), receiver);
            messageWrapperOld.send(receiver);
        }

        if (sendToConsole) messageWrapperOld.send(Bukkit.getConsoleSender());
        messageWrapperOld.tagPlayers();
    }

    public static void sendPrivateMessage(DataManager dataManager, PlayerData playerData, PlayerData targetData, String message) {
        Player player = Bukkit.getPlayer(playerData.getUuid());
        Player target = Bukkit.getPlayer(targetData.getUuid());

        MessageWrapperOld messageSentWrapper = new MessageWrapperOld(player, message)
                .checkAll("rosechat.message")
                .filterAll("rosechat.message")
                .withReplacements()
                .withTags()
                .parse("message-sent", target);

        MessageWrapperOld messageReceivedWrapper = new MessageWrapperOld(target, message)
                .checkAll("rosechat.message")
                .filterAll("rosechat.message")
                .withReplacements()
                .withTags()
                .parse("message-received", player);

        MessageWrapperOld spyMessageWrapperOld = new MessageWrapperOld(player, message)
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
            if (spy != null) spy.spigot().sendMessage(spyMessageWrapperOld.build());
        }

        try {
            if (targetData.hasMessageSounds()) {
                Sound sound = Sound.valueOf(ConfigurationManager.Setting.MESSAGE_SOUND.getString());
                target.playSound(target.getLocation(), sound, 1, 1);
            }
        } catch (Exception e) {

        }*/
    }
}
