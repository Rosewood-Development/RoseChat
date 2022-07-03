package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {

    public static final Pattern URL_PATTERN = Pattern.compile("(http(s){0,1}://){0,1}[-a-zA-Z0-9@:%._\\+~#=]{2,32}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");
    public static final Pattern DISCORD_EMOJI_PATTERN = Pattern.compile("<a?:([a-zA-Z0-9_\\-~]+):[0-9]{18}>");
    public static final Pattern EMOJI_PATTERN = Pattern.compile(":([a-zA-Z_]+):");
    public static final Pattern DISCORD_CHANNEL_PATTERN = Pattern.compile("<#([0-9]{18})>");
    public static final Pattern DISCORD_TAG_PATTERN = Pattern.compile("<@([0-9]{18})>");
    public static final Pattern DISCORD_ROLE_TAG_PATTERN = Pattern.compile("<@&([0-9]{18})>");
    public static final Pattern URL_MARKDOWN_PATTERN = Pattern.compile("\\[(.+)\\]\\(((http(s){0,1}://){0,1}[-a-zA-Z0-9@:%._\\+~#=]{2,32}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*))\\)");

    public static final Pattern DISCORD_BOLD_MARKDOWN = Pattern.compile("\\*\\*([\\s\\S]+?)\\*\\*(?!\\*)");
    public static final Pattern DISCORD_UNDERLINE_MARKDOWN = Pattern.compile("__([\\s\\S]+?)__(?!_)");
    public static final Pattern DISCORD_ITALIC_MARKDOWN = Pattern.compile("\\b_((?:__|\\\\[\\s\\S]|[^\\\\_])+?)_\\b|\\*(?=\\S)((?:\\*\\*|\\s+(?:[^*\\s]|\\*\\*)|[^\\s*])+?)\\*(?!\\*)");
    public static final Pattern DISCORD_STRIKETHROUGH_MARKDOWN = Pattern.compile("~~(?=\\S)([\\s\\S]*?\\S)~~");
    public static final Pattern VALID_LEGACY_REGEX = Pattern.compile("&[0-9a-fA-F]");
    public static final Pattern VALID_LEGACY_REGEX_FORMATTING = Pattern.compile("&[k-oK-OrR]");
    public static final Pattern HEX_REGEX = Pattern.compile("<#([A-Fa-f0-9]){6}>|\\{#([A-Fa-f0-9]){6}}|&#([A-Fa-f0-9]){6}|#([A-Fa-f0-9]){6}");
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

    public static String stripAccents(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFKD);

        for (char c : string.toCharArray()) {
            if (c <= '\u007F') sb.append(c);
        }

        return sb.toString();
    }

    public static double getLevenshteinDistancePercent(String first, String second) {
        int levDistance = LevenshteinDistance.getDefaultInstance().apply(MessageUtils.stripAccents(first.toLowerCase()), MessageUtils.stripAccents(second.toLowerCase()));

        String longerMessage = second;

        if (second.length() < first.length()) longerMessage = first;
        return (longerMessage.length() - levDistance) / (double) longerMessage.length();
    }

    public static boolean isMessageEmpty(String message) {
        String colorified = HexUtils.colorify(message);
        return StringUtils.isBlank(ChatColor.stripColor(colorified));
    }

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

    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RoseSender sender, RoseSender viewer, Group group) {
        if (group == null) return getSenderViewerPlaceholders(sender, viewer);
        else if (group instanceof GroupChat) return getSenderViewerPlaceholders(sender, viewer, (GroupChat) group);
        else if (group instanceof ChatChannel) return getSenderViewerPlaceholders(sender, viewer, (ChatChannel) group);
        else return getSenderViewerPlaceholders(sender, viewer);
    }

    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RoseSender sender, RoseSender viewer, Group group, StringPlaceholders extra) {
        StringPlaceholders.Builder builder;
        if (group == null) builder =  getSenderViewerPlaceholders(sender, viewer);
        else if (group instanceof GroupChat) builder = getSenderViewerPlaceholders(sender, viewer, (GroupChat) group);
        else if (group instanceof ChatChannel) builder = getSenderViewerPlaceholders(sender, viewer, (ChatChannel) group);
        else builder = getSenderViewerPlaceholders(sender, viewer);

        return extra == null ? builder : builder.addAll(extra);
    }

    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RoseSender sender, RoseSender viewer, GroupChat group) {
        StringPlaceholders.Builder builder = getSenderViewerPlaceholders(sender, viewer);
        builder.addPlaceholder("group", group.getId())
                .addPlaceholder("group_name", group.getName())
                .addPlaceholder("group_owner", Bukkit.getOfflinePlayer(group.getOwner()).getName());

        return builder;
    }

    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RoseSender sender, RoseSender viewer, ChatChannel inChannel) {
        StringPlaceholders.Builder builder = getSenderViewerPlaceholders(sender, viewer);
        builder.addPlaceholder("channel", inChannel.getId());;
        return builder;
    }

    public static BaseComponent[] parseCustomPlaceholder(RoseSender sender, RoseSender viewer, String id, List<Tokenizer<?>> tokenizers, StringPlaceholders placeholders, boolean discordify) {
//        CustomPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(id);
//        if (placeholder == null) return null;
//
//        BaseComponent[] component;
//        HoverEvent hoverEvent = null;
//        ClickEvent clickEvent = null;
//
//        String text = placeholder.getText().parse(sender, viewer, placeholders);
//        if (text == null) return null;
//        text = placeholders.apply(text);
//        MessageTokenizer textTokenizer = new MessageTokenizer.Builder()
//                .sender(sender).viewer(viewer).location(MessageLocation.OTHER).tokenizers(tokenizers).simplify(false).colorize(!discordify)
//                .tokenize(discordify ? MessageUtils.stripColors(text) : text);
//        component = textTokenizer.toComponents();
//
//        String hoverString = placeholder.getHover() != null ? placeholders.apply(placeholder.getHover().parse(sender, viewer, placeholders)) : null;
//        if (hoverString != null) {
//            MessageTokenizer hoverTokenizer = new MessageTokenizer.Builder()
//                    .sender(sender).viewer(viewer).location(MessageLocation.OTHER).tokenizers(tokenizers).tokenize(hoverString);
//            BaseComponent[] hover = hoverTokenizer.toComponents();
//            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
//        }
//
//        String clickString = placeholder.getClick() != null ? placeholders.apply(placeholder.getClick().parse(sender, viewer, placeholders)) : null;
//        ClickEvent.Action action = placeholder.getClick() != null ? placeholder.getClick().parseToAction(sender, viewer, placeholders) : null;
//        if (clickString != null && action != null) {
//            String click = sender.isPlayer() ? PlaceholderAPIHook.applyPlaceholders(sender.asPlayer(), clickString) : clickString;
//            clickEvent = new ClickEvent(action, ChatColor.stripColor(click));
//        }
//
//        for (BaseComponent c : component) {
//            c.setHoverEvent(hoverEvent);
//            c.setClickEvent(clickEvent);
//        }
//
//        return component;
        return null;
    }

    public static BaseComponent[] parseCustomPlaceholder(RoseSender sender, RoseSender viewer, String id, StringPlaceholders placeholders, boolean discordify) {
//        return parseCustomPlaceholder(sender, viewer, id,
//                Setting.USE_DISCORD_FORMATTING.getBoolean() ? Tokenizers.DEFAULT_WITH_DISCORD_TOKENIZERS : Tokenizers.DEFAULT_TOKENIZERS, placeholders, discordify);
        return null;
    }

    public static void sendDiscordMessage(MessageWrapper message, Group group, String channel) {
        RoseChatAPI.getInstance().getDiscord().sendMessage(message, group, channel);
    }

    public static void sendPrivateMessage(RoseSender sender, String targetName, MessageWrapper message) {
        Player target = Bukkit.getPlayer(targetName);
        RoseSender messageTarget = target == null ? new RoseSender(Bukkit.getConsoleSender()) : new RoseSender(target);

        BaseComponent[] sentMessage = message.parse(Setting.MESSAGE_SENT_FORMAT.getString(), messageTarget);
        BaseComponent[] receivedMessage = message.parse(Setting.MESSAGE_RECEIVED_FORMAT.getString(), messageTarget);
        BaseComponent[] spyMessage = message.parse(Setting.MESSAGE_SPY_FORMAT.getString(), messageTarget);

        if (sender.isPlayer()) {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            if (offlineTarget != null) {
                PlayerData targetData = RoseChatAPI.getInstance().getDataManager().getPlayerData(offlineTarget.getUniqueId());
                if (targetData != null && targetData.getIgnoringPlayers().contains(sender.getUUID())) {
                    RoseChatAPI.getInstance().getLocaleManager().sendMessage(sender.asPlayer(), "command-togglemessage-cannot-message");
                    return;
                }
            }
        }

        sender.send(sentMessage);
        if (target == null) {
            if (targetName.equalsIgnoreCase("Console")) {
                Bukkit.getConsoleSender().spigot().sendMessage(receivedMessage);
            } else {
                BungeeListener.sendDirectMessage(sender.getUUID(), targetName, ComponentSerializer.toString(receivedMessage));
            }
        } else {
            target.spigot().sendMessage(receivedMessage);
        }

        for (UUID uuid : RoseChatAPI.getInstance().getDataManager().getMessageSpies()) {
            boolean isSpySender = sender.isPlayer() && uuid.equals(sender.asPlayer().getUniqueId());
            boolean isSpyTarget = messageTarget.isPlayer() && uuid.equals(messageTarget.asPlayer().getUniqueId());
            if (isSpySender || isSpyTarget) continue;

            Player spy = Bukkit.getPlayer(uuid);
            if (spy != null)
                spy.spigot().sendMessage(spyMessage);
        }
    }

    public static Player getPlayer(String name) {
        if (name == null || name.isEmpty()) return null;
        Player player = Bukkit.getPlayer(name);
        if (player != null) return player;

        for (PlayerData playerData : RoseChatAPI.getInstance().getDataManager().getPlayerData().values()) {
            if (playerData.getNickname() == null) continue;
            if (ChatColor.stripColor(HexUtils.colorify(playerData.getNickname().toLowerCase())).startsWith(name.toLowerCase())) {
                player = Bukkit.getPlayer(playerData.getUUID());
                return player;
            }
        }

        return null;
    }

    public static void parseFormat(String id, String format) {
        RoseChatAPI.getInstance().getPlaceholderManager().parseFormat(id, format);
    }

    public static boolean canColor(CommandSender sender, String str, String permissionArea) {
        Matcher colorMatcher = VALID_LEGACY_REGEX.matcher(str);
        Matcher formatMatcher = VALID_LEGACY_REGEX_FORMATTING.matcher(str);
        Matcher hexMatcher = HEX_REGEX.matcher(str);
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(str);
        Matcher rainbowMatcher = RAINBOW_PATTERN.matcher(str);

        boolean canColor = !colorMatcher.find() || sender.hasPermission("rosechat.color." + permissionArea);
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

    public static String stripColors(String message) {
        return message.replaceAll(VALID_LEGACY_REGEX.pattern(), "")
                .replaceAll(HEX_REGEX.pattern(), "")
                .replaceAll(GRADIENT_PATTERN.pattern(), "")
                .replaceAll(RAINBOW_PATTERN.pattern(), "");
    }

    public static String processForDiscord(String text) {
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
                    if (Character.toLowerCase(nextChar) == 'r') {
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

        return stringBuilder.toString();
    }

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

}
