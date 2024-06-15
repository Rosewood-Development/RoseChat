package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.stream.Collectors;

public class GroupArgumentHandler extends ArgumentHandler<GroupChannel> {

    private final boolean checkPermissions;

    public GroupArgumentHandler(boolean checkPermissions) {
        super(GroupChannel.class);

        this.checkPermissions = checkPermissions;
    }

    @Override
    public GroupChannel handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        boolean hasAdminPermission = (context.getSender() instanceof Player player)
                && player.hasPermission("rosechat.group.admin");
        boolean isConsole = (context.getSender() instanceof ConsoleCommandSender);

        GroupChannel group = RoseChatAPI.getInstance().getGroupChatById(input);
        if (!this.checkPermissions || (hasAdminPermission || isConsole)) {
            if (group == null)
                throw new HandledArgumentException("argument-handler-group");

            return group;
        }

        if (!group.getMembers().contains(((Player) context).getUniqueId()))
            throw new HandledArgumentException("gc-invalid");

        return group;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        boolean hasAdminPermission = (context.getSender() instanceof Player player)
                && player.hasPermission("rosechat.group.admin");
        boolean isConsole = (context.getSender() instanceof ConsoleCommandSender);

        if (!this.checkPermissions || (hasAdminPermission || isConsole))
            return RoseChatAPI.getInstance().getGroupChatIDs();

        return RoseChatAPI.getInstance().getGroupChats(((Player) context).getUniqueId()).stream()
                .map(GroupChannel::getId).collect(Collectors.toList());
    }

}
