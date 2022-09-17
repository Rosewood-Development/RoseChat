package dev.rosewood.rosechat.hook;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.placeholders.RoseChatPlaceholder;
import dev.rosewood.rosechat.placeholders.condition.PlaceholderCondition;
import dev.rosewood.rosegarden.utils.HexUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RoseChatPlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        if (player == null) return null;

        RoseChatAPI api = RoseChatAPI.getInstance();
        PlayerData playerData = api.getPlayerData(player.getUniqueId());
        GroupManager groupManager = api.getGroupManager();
        PlaceholderManager placeholderSettingManager = api.getPlaceholderManager();
        if (playerData == null) return null;

        switch (placeholder) {
            case "chat_color":
                return playerData.getColor();
            case "nickname":
                return playerData.getNickname();
            case "current_channel":
                return playerData.getCurrentChannel().getId();
            case "is_muted":
                return playerData.getMuteTime() > 0 ? "yes" : "no";
            case "mute_time":
                return String.valueOf(playerData.getMuteTime());
            case "has_emojis":
                return playerData.hasEmojis() ? "yes" : "no";
            case "has_message_sounds":
                return playerData.hasMessageSounds() ? "yes" : "no";
            case "has_tag_sounds":
                return playerData.hasTagSounds() ? "yes" : "no";
            case "can_be_messaged":
                return playerData.canBeMessaged() ? "yes" : "no";
            case "has_group_spy":
                return playerData.hasGroupSpy() ? "yes" : "no";
            case "has_channel_spy":
                return playerData.hasChannelSpy() ? "yes" : "no";
            case "has_message_spy":
                return playerData.hasMessageSpy() ? "yes" : "no";
            case "last_messaged":
                return playerData.getReplyTo();
            case "is_group_leader":
                return groupManager.getGroupChatByOwner(playerData.getUUID()) != null ? "yes" : "no";
            case "group":
                GroupChat group = groupManager.getGroupChatByOwner(playerData.getUUID());
                return group != null ? group.getId() : null;
        }

        if (placeholder.startsWith("placeholder_")) {
            String rcPlaceholderId = placeholder.substring("placeholder_".length());
            RoseSender sender = new RoseSender(player);
            RoseChatPlaceholder rcPlaceholder = placeholderSettingManager.getPlaceholder(rcPlaceholderId);
            if (rcPlaceholder == null) return null;

            return HexUtils.colorify(rcPlaceholder.getText().parseToString(sender, sender, MessageUtils.getSenderViewerPlaceholders(sender, sender).build()));
        }

        if (placeholder.startsWith("group_")) {
            if (placeholder.endsWith("_owner")) {
                String gc = placeholder.substring("group_".length(), placeholder.indexOf("_owner"));
                return Bukkit.getOfflinePlayer(groupManager.getGroupChatById(gc).getOwner()).getName();
            }

            if (placeholder.endsWith("_name")) {
                String gc = placeholder.substring("group_".length(), placeholder.indexOf("_name"));
                GroupChat groupChat = groupManager.getGroupChatById(gc);
                return groupChat == null ? null : groupChat.getName();
            }
        }

        return null;
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
