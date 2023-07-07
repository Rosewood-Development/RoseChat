package dev.rosewood.rosechat.hook;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.placeholders.RoseChatPlaceholder;
import dev.rosewood.rosegarden.utils.HexUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RoseChatPlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        if (placeholder == null) return null;

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

        if (placeholder.startsWith("emoji_")) {
            for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
                if (placeholder.equalsIgnoreCase("emoji_" + emoji.getId())) {
                    return emoji.getReplacement();
                }
            }
        }

        if (player == null) return null;
        PlayerData playerData = api.getPlayerData(player.getUniqueId());
        if (playerData == null) return null;

        if (placeholder.startsWith("placeholder_")) {
            String rcPlaceholderId = placeholder.substring("placeholder_".length());
            RosePlayer sender = new RosePlayer(player);
            RoseChatPlaceholder rcPlaceholder = placeholderSettingManager.getPlaceholder(rcPlaceholderId);
            if (rcPlaceholder == null) return null;

            return HexUtils.colorify(rcPlaceholder.getText().parseToString(sender, sender, MessageUtils.getSenderViewerPlaceholders(sender, sender).build()));
        }

        return switch (placeholder) {
            case "chat_color" -> playerData.getColor();
            case "nickname" -> playerData.getNickname();
            case "nickname_fallback" -> playerData.getNickname() == null ? player.getDisplayName() : playerData.getNickname();
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
