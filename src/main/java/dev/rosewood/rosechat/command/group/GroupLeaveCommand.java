package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class GroupLeaveCommand extends RoseChatCommand {

    public GroupLeaveCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("leave")
                .descriptionKey("command-gc-leave-description")
                .permission("rosechat.group.leave")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .required("group", RoseChatArgumentHandlers.MEMBER_GROUP)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, GroupChannel group) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (group.getOwner().equals(player.getUUID())) {
            player.sendLocaleMessage("command-gc-leave-own");
            return;
        }

        boolean success = group.kick(player.getUUID(), false);
        if (!success)
            return;

        if (player.getChannel() == group)
            player.switchChannel(player.findChannel());

        String name = player.getName();
        player.sendLocaleMessage("command-gc-leave-success",
                StringPlaceholders.of("name", group.getName()));

        for (UUID uuid : group.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member == null)
                continue;

            this.getLocaleManager().sendComponentMessage(member, "command-gc-leave-left",
                    StringPlaceholders.of(
                            "player", name,
                            "name", group.getName()
                    ));
        }
    }

}
