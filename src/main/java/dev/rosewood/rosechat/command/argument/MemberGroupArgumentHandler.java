package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.stream.Collectors;

public class MemberGroupArgumentHandler extends ArgumentHandler<GroupChannel> {

    public MemberGroupArgumentHandler() {
        super(GroupChannel.class);
    }

    @Override
    public GroupChannel handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        GroupChannel group = RoseChatAPI.getInstance().getGroupChatById(input);
        if (group == null)
            throw new HandledArgumentException("argument-handler-member-group");

        if (!group.getMembers().contains(((Player) context.getSender()).getUniqueId()))
            throw new HandledArgumentException("gc-invalid");

        return group;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return RoseChatAPI.getInstance().getGroupChats(((Player) context.getSender()).getUniqueId()).stream()
                .map(GroupChannel::getId).collect(Collectors.toList());
    }

}
