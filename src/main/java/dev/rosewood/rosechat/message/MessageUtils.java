package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

public class MessageUtils {

    public static final Pattern URL_PATTERN = Pattern.compile("(http(s){0,1}://){0,1}[-a-zA-Z0-9@:%._\\+~#=]{2,32}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");

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
        return ChatColor.stripColor(colorified).isEmpty();
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

    public static BaseComponent[] parseCustomPlaceholder(RoseSender sender, RoseSender viewer, String id, StringPlaceholders placeholders) {
        CustomPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(id);
        if (placeholder == null) return null;

        BaseComponent[] component;
        HoverEvent hoverEvent = null;
        ClickEvent clickEvent = null;

        String text = placeholder.getText().parse(sender, viewer, placeholders);
        if (text == null) return null;
        component = new MessageTokenizer(sender, viewer, MessageLocation.OTHER, placeholders.apply(text)).toComponents();

        String hoverString = placeholder.getHover() != null ? placeholders.apply(placeholder.getHover().parse(sender, viewer, placeholders)) : null;
        if (hoverString != null) {
            BaseComponent[] hover = new MessageTokenizer(sender, viewer, MessageLocation.OTHER, hoverString).toComponents();
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
        }

        String clickString = placeholder.getClick() != null ? placeholders.apply(placeholder.getClick().parse(sender, viewer, placeholders)) : null;
        ClickEvent.Action action = placeholder.getClick() != null ? placeholder.getClick().parseToAction(sender, viewer, placeholders) : null;
        if (clickString != null && action != null) {
            String click = sender.isPlayer() ? PlaceholderAPIHook.applyPlaceholders(sender.asPlayer(), clickString) : clickString;
            clickEvent = new ClickEvent(action, ChatColor.stripColor(click));
        }

        for (BaseComponent c : component) {
            c.setHoverEvent(hoverEvent);
            c.setClickEvent(clickEvent);
        }

        return component;
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
}
