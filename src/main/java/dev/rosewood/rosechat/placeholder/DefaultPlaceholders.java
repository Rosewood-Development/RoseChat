package dev.rosewood.rosechat.placeholder;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;

public class DefaultPlaceholders {

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getFor(RosePlayer sender, RosePlayer viewer) {
        StringPlaceholders.Builder builder = StringPlaceholders.builder()
                .add("player_name", sender.getRealName())
                .add("player_displayname", sender.getDisplayName())
                .add("player_nickname", sender.getName());

        if (viewer != null) {
            builder.add("other_player_name", viewer.getRealName())
                    .add("other_player_displayname", viewer.getDisplayName())
                    .add("other_player_nickname", viewer.getName());
        }

        Permission vault = RoseChatAPI.getInstance().getVault();
        if (vault != null)
            builder.add("vault_rank", sender.getPermissionGroup());

        return builder;
    }

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @param channel The {@link Channel} that these placeholders will be sent in.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getFor(RosePlayer sender, RosePlayer viewer, Channel channel) {
        if (channel == null)
            return getFor(sender, viewer);

        else if (channel.getId().equalsIgnoreCase("group"))
            return getFor(sender, viewer, (GroupChannel) channel);

        StringPlaceholders.Builder builder = getFor(sender, viewer);
        builder.add("channel", channel.getId());

        return builder;
    }

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @param group The {@link GroupChannel} that these placeholders will be sent in.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getFor(RosePlayer sender, RosePlayer viewer, GroupChannel group) {
        StringPlaceholders.Builder builder = getFor(sender, viewer);
        builder.add("group", group.getId())
                .add("group_name", group.getName())
                .add("group_owner", Bukkit.getOfflinePlayer(group.getOwner()).getName());

        return builder;
    }

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @param channel The {@link Channel} that these placeholders will be sent in.
     * @param extra More {@link StringPlaceholders} to use.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getFor(RosePlayer sender, RosePlayer viewer, Channel channel, StringPlaceholders extra) {
        StringPlaceholders.Builder builder;
        if (channel == null)
            builder = getFor(sender, viewer);
        else if (channel instanceof GroupChannel)
            return getFor(sender, viewer, (GroupChannel) channel);
        else
            builder = getFor(sender, viewer, channel);

        return extra == null ? builder : builder.addAll(extra);
    }

}
