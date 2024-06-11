package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import java.util.List;
import java.util.stream.Collectors;

public class GroupInviteArgumentHandler extends ArgumentHandler<GroupChannel> {

    public GroupInviteArgumentHandler() {
        super(GroupChannel.class);
    }

    @Override
    public GroupChannel handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        RosePlayer player = new RosePlayer(context.getSender());

        GroupChannel channel = RoseChatAPI.getInstance().getGroupChatById(input);
        if (channel == null)
            throw new HandledArgumentException("argument-handler-group");

        if (player.getPlayerData().getGroupInvites().isEmpty())
            throw new HandledArgumentException("gc-no-invites");

        if (!player.getPlayerData().getGroupInvites().contains(channel))
            throw new HandledArgumentException("argument-handler-member-group");

        return channel;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        RosePlayer player = new RosePlayer(context.getSender());
        return player.getPlayerData().getGroupInvites().stream().map(GroupChannel::getId).collect(Collectors.toList());
    }

}
