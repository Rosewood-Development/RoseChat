package dev.rosewood.rosechat.hook;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.chat.channel.ChannelMessageOptions;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.placeholder.CustomPlaceholder;
import dev.rosewood.rosechat.placeholder.DefaultPlaceholders;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public class RoseChatPlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public String onRequest(OfflinePlayer player, String placeholder) {
        if (placeholder == null)
            return null;

        RoseChatAPI api = RoseChatAPI.getInstance();
        PlaceholderManager placeholderSettingManager = api.getPlaceholderManager();
        GroupManager groupManager = api.getGroupManager();

        // These placeholders don't require a player.
        if (placeholder.startsWith("group_")) {
            if (placeholder.endsWith("_owner")) {
                String gc = placeholder.substring("group_".length(), placeholder.indexOf("_owner"));
                return Bukkit.getOfflinePlayer(groupManager.getGroupChatById(gc).getOwner()).getName();
            }

            if (placeholder.endsWith("_name")) {
                String gc = placeholder.substring("group_".length(), placeholder.indexOf("_name"));
                GroupChannel groupChat = groupManager.getGroupChatById(gc);
                return groupChat == null ? null : groupChat.getName();
            }
        }

        if (placeholder.startsWith("replacement_")) {
            for (Replacement replacement : RoseChatAPI.getInstance().getReplacements()) {
                if (placeholder.equalsIgnoreCase("replacement_" + replacement.getId())) {
                    return replacement.getOutput().getText();
                }
            }
        }

        if (placeholder.startsWith("channel_")) {
            for (Channel channel : RoseChatAPI.getInstance().getChannels()) {
                if (!placeholder.contains("_" + channel.getId() + "_"))
                    continue;

                StringPlaceholders info = channel.getInfoPlaceholders().build();
                for (String infoKey : info.getPlaceholders().keySet()) {
                    if (placeholder.endsWith("_" + infoKey)) {
                        return info.getPlaceholders().get(infoKey);
                    }
                }
            }
        }

        if (player == null) {
            // Grab the last message without a player if no player was provided.
            if (placeholder.startsWith("last_message_")) {
                String channelId = placeholder.substring("last_message_".length());
                Channel channel = RoseChatAPI.getInstance().getChannelById(channelId);
                if (channel == null)
                    return null;

                ChannelMessageOptions options = channel.getMessageLog().getAndRemoveNextMessage();
                if (options == null)
                    return null;

                return options.message();
            } else
                return null;
        }

        PlayerData playerData = api.getPlayerData(player.getUniqueId());
        if (playerData == null)
            return null;

        if (placeholder.startsWith("placeholder_")) {
            String rcPlaceholderId = placeholder.substring("placeholder_".length());
            RosePlayer sender = new RosePlayer(player);
            CustomPlaceholder rcPlaceholder = placeholderSettingManager.getPlaceholder(rcPlaceholderId);
            if (rcPlaceholder == null)
                return null;

            return HexUtils.colorify(rcPlaceholder.get("text").parseToString(sender, sender, DefaultPlaceholders.getFor(sender, sender).build()));
        } else if (placeholder.startsWith("last_message_")) {
            String channelId = placeholder.substring("last_message_".length());
            Channel channel = RoseChatAPI.getInstance().getChannelById(channelId);
            if (channel == null)
                return null;

            ChannelMessageOptions options = channel.getMessageLog().getAndRemoveNextMessage(player.getPlayer());
            if (options == null)
                return null;

            return options.message();
        }

        String displayName = (player.getPlayer() != null ? player.getPlayer().getDisplayName() : player.getName());
        return switch (placeholder) {
            case "chat_color" -> playerData.getColor();
            case "nickname" -> playerData.getNickname();
            case "nickname_stripped" -> playerData.getNickname() == null ? null : ChatColor.stripColor(HexUtils.colorify(playerData.getNickname()));
            case "name" -> playerData.getNickname() == null ? displayName : playerData.getNickname();
            case "name_stripped" -> playerData.getNickname() == null ?
                    ChatColor.stripColor(displayName) : ChatColor.stripColor(HexUtils.colorify(playerData.getNickname()));
            case "current_channel" -> playerData.getCurrentChannel().getId();
            case "is_muted" -> playerData.isMuted() ? "yes" : "no";
            case "mute_time" -> String.valueOf(playerData.getMuteExpirationTime());
            case "has_emojis" -> playerData.hasEmojis() ? "yes" : "no";
            case "has_message_sounds" -> playerData.hasMessageSounds() ? "yes" : "no";
            case "has_tag_sounds" -> playerData.hasTagSounds() ? "yes" : "no";
            case "can_be_messaged" -> playerData.canBeMessaged() ? "yes" : "no";
            case "has_group_spy" -> playerData.hasGroupSpy() ? "yes" : "no";
            case "has_channel_spy" -> playerData.hasChannelSpy() ? "yes" : "no";
            case "has_message_spy" -> playerData.hasMessageSpy() ? "yes" : "no";
            case "last_messaged" -> playerData.getReplyTo();
            case "is_group_leader" -> groupManager.getGroupChatByOwner(playerData.getUUID()) != null ? "yes" : "no";
            case "group" -> {
                GroupChannel group = groupManager.getGroupChatByOwner(playerData.getUUID());
                yield group != null ? group.getId() : null;
            }
            default -> null;
        };
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return RoseChat.getInstance().getDescription().getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return RoseChat.getInstance().getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return RoseChat.getInstance().getDescription().getVersion();
    }

}
