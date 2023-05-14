package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.NicknameCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.PrivateMessageInfo;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.placeholders.RoseChatPlaceholder;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RosePlayer sender, RosePlayer viewer) {
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
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @param channel The {@link Channel} that these placeholders will be sent in.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RosePlayer sender, RosePlayer viewer, Channel channel) {
        if (channel == null) return getSenderViewerPlaceholders(sender, viewer);
        else if (channel.getId().equalsIgnoreCase("group")) return getSenderViewerPlaceholders(sender, viewer, (GroupChannel) channel);

        StringPlaceholders.Builder builder = getSenderViewerPlaceholders(sender, viewer);
        builder.addPlaceholder("channel", channel.getId());;
        return builder;
    }

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @param channel The {@link Channel} that these placeholders will be sent in.
     * @param extra More {@link StringPlaceholders} to use.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RosePlayer sender, RosePlayer viewer, Channel channel, StringPlaceholders extra) {
        StringPlaceholders.Builder builder;
        if (channel == null) builder =  getSenderViewerPlaceholders(sender, viewer);
        else if (channel.getId().equalsIgnoreCase("group")) return getSenderViewerPlaceholders(sender, viewer, (GroupChannel) channel);
        else builder = getSenderViewerPlaceholders(sender, viewer, channel);

        return extra == null ? builder : builder.addAll(extra);
    }

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @param group The {@link GroupChannel} that these placeholders will be sent in.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RosePlayer sender, RosePlayer viewer, GroupChannel group) {
        StringPlaceholders.Builder builder = getSenderViewerPlaceholders(sender, viewer);
        builder.addPlaceholder("group", group.getId())
                .addPlaceholder("group_name", group.getName())
                .addPlaceholder("group_owner", Bukkit.getOfflinePlayer(group.getOwner()).getName());

        return builder;
    }

    /**
     * Sends a message to a Discord channel.
     * @param message The message to send.
     * @param channel The {@link Channel} that the message was sent from.
     * @param discordChannel The channel to send the message to.
     */
    public static void sendDiscordMessage(RoseMessage message, Channel channel, String discordChannel) {
        RoseChatAPI.getInstance().getDiscord().sendMessage(message, channel, discordChannel);
    }

    /**
     * Sends a private message from one player to another.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param targetName The name of the player receiving the message.
     * @param message The message to send.
     */
    public static void sendPrivateMessage(RosePlayer sender, String targetName, String message) {
        Player target = Bukkit.getPlayer(targetName);
        RosePlayer messageTarget = target == null ? new RosePlayer(targetName, "default") : new RosePlayer(target);

        // Create the private message info, rules, and the message, then apply the rules.
        PrivateMessageInfo privateMessageInfo = new PrivateMessageInfo(sender, messageTarget);
        MessageRules rules = new MessageRules().applyAllFilters().applySenderChatColor().setPrivateMessageInfo(privateMessageInfo);

        RoseMessage roseMessage = new RoseMessage(sender, MessageLocation.MESSAGE, message);
        roseMessage.applyRules(rules);

        // If the message is blocked, send a warning to the player.
        if (roseMessage.isBlocked()) {
            if (roseMessage.getFilterType() != null) roseMessage.getFilterType().sendWarning(sender);
            return;
        }

        // If the message was sent by a player, check if the receiver is ignoring them.
        if (sender.isPlayer()) {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            PlayerData targetData = RoseChatAPI.getInstance().getPlayerData(offlineTarget.getUniqueId());
            if (targetData != null && targetData.getIgnoringPlayers().contains(sender.getUUID())) {
                RoseChatAPI.getInstance().getLocaleManager().sendMessage(sender.asPlayer(), "command-togglemessage-cannot-message");
                return;
            }
        }

        // Parse the message for the console to generate the tokens
        BaseComponent[] parsedMessage = roseMessage.parse(new RosePlayer(Bukkit.getConsoleSender()), Setting.CONSOLE_MESSAGE_FORMAT.getString());

        // If the console is not the target of the message, send the console message format. Otherwise, send the received message format later.
        // The tokens will always be generated before even if this message is not sent.
        if (!targetName.equalsIgnoreCase("Console") && !sender.isConsole())
            Bukkit.getConsoleSender().spigot().sendMessage(parsedMessage);

        // Parse for the channel spies.
        for (UUID uuid : RoseChatAPI.getInstance().getPlayerDataManager().getMessageSpies()) {
            // Don't send the spy message if the spy is the sender or receiver.
            if ((sender.isPlayer() && uuid.equals(sender.getUUID())) || messageTarget.isPlayer() && uuid.equals(messageTarget.getUUID())) continue;

            // If the spy isn't valid, continue.
            Player spy = Bukkit.getPlayer(uuid);
            if (spy == null) continue;

            // Adds the spy to the private message info.
            privateMessageInfo.addSpy(new RosePlayer(spy));

            RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
                BaseComponent[] parsedSpyMessage = roseMessage.parse(new RosePlayer(spy), Setting.MESSAGE_SPY_FORMAT.getString());
                spy.spigot().sendMessage(parsedSpyMessage);
            });
        }

        // Parse the message for the sender and the receiver.
        RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
            BaseComponent[] parsedSentMessage = roseMessage.parse(sender, Setting.MESSAGE_SENT_FORMAT.getString());
            BaseComponent[] parsedReceivedMessage = roseMessage.parse(messageTarget, Setting.MESSAGE_RECEIVED_FORMAT.getString());

            if (target == null) {
                // If the target is not valid and the name is "Console", then send the message to the console.
                if (targetName.equalsIgnoreCase("Console")) {
                    sender.send(parsedSentMessage);
                    Bukkit.getConsoleSender().spigot().sendMessage(parsedReceivedMessage);
                } else {
                    // If the target is not valid, but the name isn't console, we should see if it is a bungee player.
                    RoseChatAPI.getInstance().getBungeeManager().sendDirectMessage(sender, targetName, message, (success) -> {
                        if (success) {
                            // If the message was received successfully, send the sent message to the sender.
                            sender.send(parsedSentMessage);
                        } else {
                            // If the message was not received successfully, then the player is assumed to not be online.
                            RoseChatAPI.getInstance().getLocaleManager().sendComponentMessage(sender, "player-not-found");
                        }
                    });
                }
            } else {
                // The sender should receive the message first.
                sender.send(parsedSentMessage);

                // If the target is online, send the message.
                messageTarget.send(parsedReceivedMessage);
            }
        });

        // Update the player's display name if the setting is enabled.
        if (sender.getPlayerData() == null || sender.getPlayerData().getNickname() == null) return;
        if (Setting.UPDATE_DISPLAY_NAMES.getBoolean() && sender.getPlayerData().getNickname() != null && !sender.getDisplayName().equals(sender.getPlayerData().getNickname())) {
            RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
                RoseMessage nickname = new RoseMessage(sender, MessageLocation.NICKNAME, sender.getPlayerData().getNickname());
                nickname.parse(sender, null);

                if (sender.getPlayerData().getNickname() != null) NicknameCommand.setDisplayName(sender, nickname);
            });
        }
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
     * @param sender The {@link RosePlayer} who is sending the string.
     * @param str The string to check.
     * @param permissionArea The location, from a {@link MessageLocation} as a string.
     * @return True if the message can be colored.
     */
    public static boolean canColor(RosePlayer sender, String str, String permissionArea) {
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
            sender.sendLocaleMessage("no-permission");
            return false;
        }

        return true;
    }

    /**
     * @param input The string to check.
     * @param roseMessage The {@link RoseMessage} containing the message.
     * @return True if this message starts with a saved /chatcolor.
     */
    public static boolean hasDefaultColor(String input, RoseMessage roseMessage) {
        if (roseMessage == null || roseMessage.getSenderData() == null || roseMessage.getSenderData().getColor() == null) return false;

        String message = roseMessage.getMessage();
        String color = roseMessage.getSenderData().getColor();
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

    /**
     * Checks if the sender of a given {@link RoseMessage} has the specified permission.
     * @param message The {@link RoseMessage} to get information from, such as the sender and message location.
     * @param permission The permission to check, should not contain the location information. For example, 'rosechat.emojis'.
     * @return True if the sender has the permission
     */
    public static boolean hasTokenPermission(RoseMessage message, String permission) {
        // If the message doesn't exist, sent from the console, or has a location of 'NONE', then the sender should have permission.
        if (message == null || message.getSender() == null || message.getLocation() == MessageLocation.NONE || message.getSender().isConsole()) return true;

        // Gets the full permission, e.g. rosechat.emoji.channel.global
        String fullPermission = permission + "." + message.getLocationPermission();

        return message.getSender().hasPermission(fullPermission)
                || message.getSender().getIgnoredPermissions().contains(fullPermission.replace("rosechat.", ""))
                || message.getSender().getIgnoredPermissions().contains("*");
    }

    /**
     * Checks if the sender of a given {@link RoseMessage} has the specified permission.
     * Checks against the first permission, for example, 'rosechat.emojis', and extended permissions such as 'rosechat.emoji.smile'.
     * @param message The {@link RoseMessage} to get information from, such as the sender and message location.
     * @param permission The permission to check, should not contain the location information. For example, 'rosechat.emojis'
     * @param extendedPermission The extended permission, should not contain the location information. For example, 'rosechat.emoji.smile'.
     * @return True if the sender has permission.
     */
    public static boolean hasExtendedTokenPermission(RoseMessage message, String permission, String extendedPermission) {
        // The sender will not have an extended permission if they do not have the base permission.
        if (!hasTokenPermission(message, permission)) return false;

        return message.getSender().hasPermission(extendedPermission)
                || message.getSender().getIgnoredPermissions().contains(extendedPermission.replace("rosechat.", ""))
                || message.getSender().getIgnoredPermissions().contains("*");
    }

    public static BaseComponent[] parseDeletedMessagePlaceholder(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders, DeletableMessage deletableMessage) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        String placeholderId = Setting.DELETED_MESSAGE_FORMAT.getString();
        RoseChatPlaceholder placeholder = api.getPlaceholderManager().getPlaceholder(placeholderId.substring(1, placeholderId.length() - 1));
        if (placeholder == null) return null;

        BaseComponent[] components;
        HoverEvent hoverEvent = null;
        ClickEvent clickEvent = null;

        if (placeholder.getText() == null) return null;
        String text = placeholder.getText().parseToString(sender, viewer, placeholders);
        if (text == null) return null;
        components = api.parse(sender, viewer, text);

        if (placeholder.getHover() != null) {
            String hover = placeholder.getHover().parseToString(sender, viewer, placeholders);
            if (hover != null) {
                if (hover.equalsIgnoreCase("%original%")) {
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentSerializer.parse(deletableMessage.getJson()));
                } else {
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, api.parse(sender, viewer, hover));
                }
            }
        }

        if (placeholder.getClick() != null) {
            String click = placeholder.getClick().parseToString(sender, viewer, placeholders);
            ClickEvent.Action action = placeholder.getClick().parseToAction(sender, viewer, placeholders);
            if (click != null && action != null) {
                clickEvent = new ClickEvent(action, TextComponent.toPlainText(api.parse(sender, viewer, click)));
            }
        }

        for (BaseComponent component : components) {
            component.setHoverEvent(hoverEvent);
            component.setClickEvent(clickEvent);
        }

        return components;
    }

}
