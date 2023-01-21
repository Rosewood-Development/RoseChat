package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.NicknameCommand;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    public final static String PUNCTUATION_REGEX = "[\\p{P}\\p{S}]";
    public static final Pattern URL_PATTERN = Pattern.compile("(http(s){0,1}://){0,1}[-a-zA-Z0-9@:%._\\+~#=]{2,32}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");
    public static final Pattern DISCORD_EMOJI_PATTERN = Pattern.compile("<a?(:[a-zA-Z0-9_\\-~]+:)[0-9]{18,19}>");
    public static final Pattern EMOJI_PATTERN = Pattern.compile(":([a-zA-Z_]+):");
    public static final Pattern DISCORD_CHANNEL_PATTERN = Pattern.compile("<#([0-9]{18,19})>");
    public static final Pattern DISCORD_TAG_PATTERN = Pattern.compile("<@([0-9]{18,19})>");
    public static final Pattern DISCORD_ROLE_TAG_PATTERN = Pattern.compile("<@&([0-9]{18,19})>");
    public static final Pattern URL_MARKDOWN_PATTERN = Pattern.compile("\\[(.+)\\]\\(((http(s){0,1}://){0,1}[-a-zA-Z0-9@:%._\\+~#=]{2,32}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*))\\)");
    public static final Pattern BOLD_MARKDOWN_PATTERN = Pattern.compile("\\*\\*([\\s\\S]+?)\\*\\*(?!\\*)");
    public static final Pattern UNDERLINE_MARKDOWN_PATTERN = Pattern.compile("__([\\s\\S]+?)__(?!_)");
    public static final Pattern ITALIC_MARKDOWN_PATTERN = Pattern.compile("\\b_((?:__|\\\\[\\s\\S]|[^\\\\_])+?)_\\b|\\*(?=\\S)((?:\\*\\*|\\s+(?:[^*\\s]|\\*\\*)|[^\\s*])+?)\\*(?!\\*)");
    public static final Pattern STRIKETHROUGH_MARKDOWN_PATTERN = Pattern.compile("~~(?=\\S)([\\s\\S]*?\\S)~~");
    public static final Pattern VALID_LEGACY_REGEX = Pattern.compile("&[0-9a-fA-F]");
    public static final Pattern VALID_LEGACY_REGEX_FORMATTING = Pattern.compile("&[k-oK-OrR]");
    public static final Pattern HEX_REGEX = Pattern.compile("<#([A-Fa-f0-9]){6}>|\\{#([A-Fa-f0-9]){6}}|&#([A-Fa-f0-9]){6}|#([A-Fa-f0-9]){6}");
    public static final Pattern SPIGOT_HEX_REGEX = Pattern.compile("&x(&[A-Fa-f0-9]){6}");
    public static final Pattern SPIGOT_HEX_REGEX_PARSED = Pattern.compile("§x(§[A-Fa-f0-9]){6}");
    public static final Pattern RAINBOW_PATTERN = Pattern.compile("<(?<type>rainbow|r)(#(?<speed>\\d+))?(:(?<saturation>\\d*\\.?\\d+))?(:(?<brightness>\\d*\\.?\\d+))?(:(?<loop>l|L|loop))?>");
    public static final Pattern GRADIENT_PATTERN = Pattern.compile("<(?<type>gradient|g)(#(?<speed>\\d+))?(?<hex>(:#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})){2,})(:(?<loop>l|L|loop))?>");
    public static final Pattern STOP = Pattern.compile(
            "<(rainbow|r)(#(\\d+))?(:(\\d*\\.?\\d+))?(:(\\d*\\.?\\d+))?(:(l|L|loop))?>|" +
                    "<(gradient|g)(#(\\d+))?((:#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})){2,})(:(l|L|loop))?>|" +
                    "(&[a-f0-9r])|" +
                    "<#([A-Fa-f0-9]){6}>|" +
                    "\\{#([A-Fa-f0-9]){6}}|" +
                    "&#([A-Fa-f0-9]){6}|" +
                    "#([A-Fa-f0-9]){6}"
    );

    /**
     * Removes the accents from a string.
     * @param string The string to use.
     * @return A string without accents.
     */
    public static String stripAccents(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFKD);

        for (char c : string.toCharArray()) {
            if (c <= '\u007F') sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Gets the {@link LevenshteinDistance} between two given strings.
     * @param first The first string to use.
     * @param second The string to compare with.
     * @return A percentage of difference between the two strings.
     */
    public static double getLevenshteinDistancePercent(String first, String second) {
        int levDistance = LevenshteinDistance.getDefaultInstance().apply(MessageUtils.stripAccents(first.toLowerCase()), MessageUtils.stripAccents(second.toLowerCase()));

        String longerMessage = second;

        if (second.length() < first.length()) longerMessage = first;
        return (longerMessage.length() - levDistance) / (double) longerMessage.length();
    }

    /**
     * Checks if the given string is empty.
     * @param message The string to check.
     * @return True if the message is empty.
     */
    public static boolean isMessageEmpty(String message) {
        String colorified = HexUtils.colorify(message);
        return StringUtils.isBlank(ChatColor.stripColor(colorified));
    }

    /**
     * @param cs The {@link CharSequence} to check.
     * @return True if the {@link CharSequence} is alphanumeric or a space.
     */
    public static boolean isAlphanumericSpace(final CharSequence cs) {
        if (cs == null) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLetterOrDigit(cs.charAt(i)) && cs.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    /**
     * @param sender The {@link RoseSender} who will send these placeholders.
     * @param viewer The {@link RoseSender} who will view these placeholders.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RoseSender sender, RoseSender viewer) {
        StringPlaceholders.Builder builder = StringPlaceholders.builder()
                .addPlaceholder("player_name", sender.getName())
                .addPlaceholder("player_displayname", sender.getDisplayName())
                .addPlaceholder("player_nickname", sender.getNickname());

        if (viewer != null) {
            builder.addPlaceholder("other_player_name", viewer.getName())
                    .addPlaceholder("other_player_displayname", viewer.getDisplayName())
                    .addPlaceholder("other_player_nickname", viewer.getNickname());
        }

        Permission vault = RoseChatAPI.getInstance().getVault();
        if (vault != null) builder.addPlaceholder("vault_rank", sender.getGroup());
        return builder;
    }

    /**
     * @param sender The {@link RoseSender} who will send these placeholders.
     * @param viewer The {@link RoseSender} who will view these placeholders.
     * @param group The {@link Group} that these placeholders will be sent in.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RoseSender sender, RoseSender viewer, Group group) {
        if (group == null) return getSenderViewerPlaceholders(sender, viewer);
        else if (group instanceof GroupChat) return getSenderViewerPlaceholders(sender, viewer, (GroupChat) group);
        else if (group instanceof ChatChannel) return getSenderViewerPlaceholders(sender, viewer, (ChatChannel) group);
        else return getSenderViewerPlaceholders(sender, viewer);
    }

    /**
     * @param sender The {@link RoseSender} who will send these placeholders.
     * @param viewer The {@link RoseSender} who will view these placeholders.
     * @param group The {@link Group} that these placeholders will be sent in.
     * @param extra More {@link StringPlaceholders} to use.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RoseSender sender, RoseSender viewer, Group group, StringPlaceholders extra) {
        StringPlaceholders.Builder builder;
        if (group == null) builder =  getSenderViewerPlaceholders(sender, viewer);
        else if (group instanceof GroupChat) builder = getSenderViewerPlaceholders(sender, viewer, (GroupChat) group);
        else if (group instanceof ChatChannel) builder = getSenderViewerPlaceholders(sender, viewer, (ChatChannel) group);
        else builder = getSenderViewerPlaceholders(sender, viewer);

        return extra == null ? builder : builder.addAll(extra);
    }

    /**
     * @param sender The {@link RoseSender} who will send these placeholders.
     * @param viewer The {@link RoseSender} who will view these placeholders.
     * @param group The {@link GroupChat} that these placeholders will be sent in.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RoseSender sender, RoseSender viewer, GroupChat group) {
        StringPlaceholders.Builder builder = getSenderViewerPlaceholders(sender, viewer);
        builder.addPlaceholder("group", group.getId())
                .addPlaceholder("group_name", group.getName())
                .addPlaceholder("group_owner", Bukkit.getOfflinePlayer(group.getOwner()).getName());

        return builder;
    }

    /**
     * @param sender The {@link RoseSender} who will send these placeholders.
     * @param viewer The {@link RoseSender} who will view these placeholders.
     * @param channel The {@link ChatChannel} that these placeholders will be sent in.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RoseSender sender, RoseSender viewer, ChatChannel channel) {
        StringPlaceholders.Builder builder = getSenderViewerPlaceholders(sender, viewer);
        builder.addPlaceholder("channel", channel.getId());;
        return builder;
    }

    /**
     * Sends a message to a Discord channel.
     * @param message The message to send.
     * @param group The {@link Group} that the message was sent from.
     * @param channel The channel to send the message to.
     */
    public static void sendDiscordMessage(MessageWrapper message, Group group, String channel) {
        RoseChatAPI.getInstance().getDiscord().sendMessage(message, group, channel);
    }

    /**
     * Sends a private message from one player to another.
     * @param sender The {@link RoseSender} who sent the message.
     * @param targetName The name of the player receiving the message.
     * @param message The message to send.
     */
    public static void sendPrivateMessage(RoseSender sender, String targetName, String message) {
        Player target = Bukkit.getPlayer(targetName);
        RoseSender messageTarget = target == null ? new RoseSender(targetName, "default") : new RoseSender(target);

        PrivateMessageInfo info = new PrivateMessageInfo(sender, messageTarget);
        MessageWrapper messageWrapper = new MessageWrapper(sender, MessageLocation.MESSAGE, null, message).filter().applyDefaultColor()
                .setPrivateMessage().setPrivateMessageInfo(info);

        if (!messageWrapper.canBeSent()) {
            if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().sendWarning(sender);
            return;
        }

        if (sender.isPlayer()) {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            PlayerData targetData = RoseChatAPI.getInstance().getPlayerData(offlineTarget.getUniqueId());
            if (targetData != null && targetData.getIgnoringPlayers().contains(sender.getUUID())) {
                RoseChatAPI.getInstance().getLocaleManager().sendMessage(sender.asPlayer(), "command-togglemessage-cannot-message");
                return;
            }
        }

        // Parse for spies first.
        for (UUID uuid : RoseChatAPI.getInstance().getPlayerDataManager().getMessageSpies()) {
            boolean isSpySender = sender.isPlayer() && uuid.equals(sender.asPlayer().getUniqueId());
            boolean isSpyTarget = messageTarget.isPlayer() && uuid.equals(messageTarget.asPlayer().getUniqueId());
            if (isSpySender || isSpyTarget) continue;

            Player spy = Bukkit.getPlayer(uuid);
            if (spy == null) continue;
            info.addSpy(new RoseSender(spy));
            BaseComponent[] spyMessage = messageWrapper.parse(Setting.MESSAGE_SPY_FORMAT.getString(), new RoseSender(spy));
            spy.spigot().sendMessage(spyMessage);
        }

        BaseComponent[] sentMessage = messageWrapper.parse(Setting.MESSAGE_SENT_FORMAT.getString(), sender);
        BaseComponent[] receivedMessage = messageWrapper.parse(Setting.MESSAGE_RECEIVED_FORMAT.getString(), messageTarget);

        if (target == null) {
            if (targetName.equalsIgnoreCase("Console")) {
                Bukkit.getConsoleSender().spigot().sendMessage(receivedMessage);
            } else {
                RoseChatAPI.getInstance().getBungeeManager().sendDirectMessage(sender, targetName, message, (sent) -> {
                    if (sent) {
                        sender.send(sentMessage);
                    } else {
                        RoseChatAPI.getInstance().getLocaleManager().sendComponentMessage(sender, "player-not-found");
                    }
                });
                return;
            }
        } else {
            target.spigot().sendMessage(receivedMessage);
        }

        sender.send(sentMessage);

        if (Setting.UPDATE_DISPLAY_NAMES.getBoolean() && sender.isPlayer()
                && !sender.getDisplayName().equals(sender.getNickname())) NicknameCommand.setDisplayName(sender.asPlayer(), sender.getNickname());
    }

    /**
     * Gets the player whose name, display name or nickname contains the given name.
     * @param name The name, display name, or nickname of the player.
     * @return A {@link Player} retrieved from the given name.
     */
    public static Player getPlayer(String name) {
        if (name == null || name.isEmpty()) return null;
        Player player = Bukkit.getPlayer(name);
        if (player != null) return player;

        for (PlayerData playerData : RoseChatAPI.getInstance().getPlayerDataManager().getPlayerData().values()) {
            if (playerData.getNickname() == null) continue;
            if (ChatColor.stripColor(HexUtils.colorify(playerData.getNickname().toLowerCase())).startsWith(name.toLowerCase())) {
                player = Bukkit.getPlayer(playerData.getUUID());
                return player;
            }
        }

        return null;
    }

    /**
     * Parses a given format.
     * @param id The id of the format.
     * @param format The format.
     */
    public static void parseFormat(String id, String format) {
        RoseChatAPI.getInstance().getPlaceholderManager().parseFormat(id, format);
    }

    /**
     * Checks if a message can be coloured by the given sender.
     * @param sender The {@link CommandSender} sender who is sending the string.
     * @param str The string to check.
     * @param permissionArea The location, from a {@link MessageLocation} as a string.
     * @return True if the message can be colored.
     */
    public static boolean canColor(CommandSender sender, String str, String permissionArea) {
        Matcher colorMatcher = VALID_LEGACY_REGEX.matcher(str);
        Matcher formatMatcher = VALID_LEGACY_REGEX_FORMATTING.matcher(str);
        Matcher hexMatcher = HEX_REGEX.matcher(str);
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(str);
        Matcher rainbowMatcher = RAINBOW_PATTERN.matcher(str);

        boolean hasColor = colorMatcher.find();
        boolean usePerColorPerms = Setting.USE_PER_COLOR_PERMISSIONS.getBoolean();
        boolean hasLocationPermission = sender.hasPermission("rosechat.color." + permissionArea);
        boolean hasColorPermission = hasColor && sender.hasPermission("rosechat." + ChatColor.getByChar(Character.toLowerCase(colorMatcher.group().charAt(1))).getName().toLowerCase() + "." + permissionArea);
        boolean canColor = !hasColor || (usePerColorPerms ? hasColorPermission && hasLocationPermission : hasLocationPermission);
        boolean canMagic = !str.contains("&k") || sender.hasPermission("rosechat.magic." + permissionArea);
        boolean canFormat = !formatMatcher.find() || sender.hasPermission("rosechat.format." + permissionArea);
        boolean canHex = !hexMatcher.find() || sender.hasPermission("rosechat.hex." + permissionArea);
        boolean canGradient = !gradientMatcher.find() || sender.hasPermission("rosechat.gradient." + permissionArea);
        boolean canRainbow = !rainbowMatcher.find() || sender.hasPermission("rosechat.rainbow." + permissionArea);

        if (!(canColor && canMagic && canFormat && canHex && canGradient && canRainbow)) {
            RoseChatAPI.getInstance().getLocaleManager().sendMessage(sender, "no-permission");
            return false;
        }

        return true;
    }

    /**
     * @param input The string to check.
     * @param messageWrapper The {@link MessageWrapper} containing the message.
     * @return True if this message starts with a saved /chatcolor.
     */
    public static boolean hasDefaultColor(String input, MessageWrapper messageWrapper) {
        if (messageWrapper == null || messageWrapper.getSenderData() == null || messageWrapper.getSenderData().getColor() == null) return false;

        String message = messageWrapper.getMessage();
        String color = messageWrapper.getSenderData().getColor();
        if (color.isEmpty() || color.length() >= message.length()) return false;

        String start = message.substring(0, color.length());
        return input.startsWith(start);
    }

    /**
     * Removes all non-legacy colors from a string.
     * @param message The string to remove colors from.
     * @return The string without any non-legacy colors.
     */
    public static String stripNonLegacyColors(String message) {
        return message.replaceAll(HEX_REGEX.pattern(), "")
                .replaceAll(GRADIENT_PATTERN.pattern(), "")
                .replaceAll(RAINBOW_PATTERN.pattern(), "");
    }

    /**
     * Processes the given string to be sent on Discord.
     * Converts Minecraft formatting to Discord formatting.
     * @param text The string to use.
     * @return The string ready to be sent to Discord.
     */
    public static String processForDiscord(String text) {
        text = stripNonLegacyColors(ChatColor.stripColor(text));
        StringBuilder stringBuilder = new StringBuilder();

        boolean isFormattingCode = false;
        Deque<Character> deque = new ArrayDeque<>();
        for (int i = 0; i < text.length(); i++) {
            if (isFormattingCode) {
                isFormattingCode = false;
                continue;
            }

            char currentChar = text.charAt(i);
            if (i < text.length() - 1) {
                char nextChar = text.charAt(i + 1);
                if (currentChar == '&') {
                    if (Character.toLowerCase(nextChar) == 'r' || Character.isUpperCase(nextChar)) {
                        while (deque.descendingIterator().hasNext()) {
                            Character c = deque.pollLast();
                            if (c != null) {
                                stringBuilder.append(getDiscordFormatting(c));
                            }
                        }

                        isFormattingCode = true;
                        continue;
                    }

                    deque.add(Character.toLowerCase(nextChar));
                    stringBuilder.append(getDiscordFormatting(nextChar));

                    isFormattingCode = true;
                    continue;
                }

                stringBuilder.append(currentChar);
                continue;
            }

            if (i == text.length() - 1) {
                stringBuilder.append(currentChar);
                while (deque.descendingIterator().hasNext()) {
                    Character c = deque.pollLast();
                    if (c != null) {
                        stringBuilder.append(getDiscordFormatting(c));
                    }
                }
            }
        }

        return ChatColor.stripColor(stringBuilder.toString());
    }

    /**
     * Converts Minecraft formatting to Discord formatting.
     * For example, &l to **.
     * @param c The character to convert.
     * @return The converted string, as Discord formatting.
     */
    private static String getDiscordFormatting(char c) {
        switch (Character.toLowerCase(c)) {
            case 'o':
                return "*";
            case 'n':
                return "__";
            case 'm':
                return "~~";
            case 'l':
                return "**";
            default:
                return "";
        }
    }

    public static String getCaptureGroup(Matcher matcher, String group) {
        try {
            return matcher.group(group);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return null;
        }
    }

    public static void sendMessageWrapper(RoseSender sender, ChatChannel channel, MessageWrapper message) {
        if (!message.canBeSent()) {
            if (message.getFilterType() != null) message.getFilterType().sendWarning(sender);
            return;
        }

        channel.send(message);
        BaseComponent[] messageComponents = message.toComponents();
        if (messageComponents != null) Bukkit.getConsoleSender().spigot().sendMessage(messageComponents);
    }

}
