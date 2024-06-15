package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class ChannelArgumentHandler extends ArgumentHandler<Channel> {

    private final boolean checkPermissions;

    public ChannelArgumentHandler(boolean checkPermissions) {
        super(Channel.class);

        this.checkPermissions = checkPermissions;
    }

    @Override
    public Channel handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        // If enabled, check if a group channel exists before grabbing the real channel.
        Channel channel;
        if (Setting.ADD_GROUP_CHANNELS_TO_CHANNEL_LIST.getBoolean()) {
            channel = RoseChatAPI.getInstance().getGroupChatById(input);

            if (channel != null)
                return channel;
        }

        channel = RoseChatAPI.getInstance().getChannelById(input);
        if (channel == null)
            throw new HandledArgumentException("argument-handler-channel", StringPlaceholders.of("input", input));

        return channel;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        List<String> suggestions = new ArrayList<>();

        // Show all channel and group channel IDs to console.
        if (!this.checkPermissions || (!(context.getSender() instanceof Player player))) {
            suggestions.addAll(RoseChatAPI.getInstance().getChannelIDs());

            if (Setting.ADD_GROUP_CHANNELS_TO_CHANNEL_LIST.getBoolean())
                suggestions.addAll(RoseChatAPI.getInstance().getGroupChatIDs());

            return suggestions;
        }

        // Show channels that a player can join, and group channels that they are in if enabled.
        suggestions.addAll(RoseChatAPI.getInstance().getChannels().stream().filter(channel -> channel.canJoinByCommand(new RosePlayer(player)))
                .map(Channel::getId).toList());

        if (Setting.ADD_GROUP_CHANNELS_TO_CHANNEL_LIST.getBoolean())
            suggestions.addAll(RoseChatAPI.getInstance().getGroupChats(player.getUniqueId()).stream().map(Channel::getId).toList());

        return suggestions;
    }

}
