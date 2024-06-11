package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.GroupArgumentHandler;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentCondition;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class GroupKickCommand extends RoseChatCommand {

    public GroupKickCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("kick")
                .aliases("remove")
                .descriptionKey("command-gc-kick-description")
                .permission("rosechat.group.kick")
                .arguments(ArgumentsDefinition.builder()
                        .optional("group", new GroupArgumentHandler(false),
                                ArgumentCondition.hasPermission("rosechat.group.admin"))
                        .required("member", RoseChatArgumentHandlers.GROUP_MEMBER)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, RosePlayer target) {
        RosePlayer player = new RosePlayer(context.getSender());
        GroupChannel group = player.getOwnedGroupChannel();
        if (group == null) {
            player.sendLocaleMessage("no-gc");
            return;
        }

        this.execute(player, group, target);
    }

    @RoseExecutable
    public void execute(CommandContext context, GroupChannel group, RosePlayer target) {
        this.execute(new RosePlayer(context.getSender()), group, target);
    }

    private void execute(RosePlayer owner, GroupChannel group, RosePlayer target) {
        if (group.getOwner().equals(target.getUUID())) {
            owner.sendLocaleMessage("command-gc-kick-self");
            return;
        }

        boolean success = group.kick(target.getUUID(), true);
        if (!success)
            return;

        if (target.getChannel() == group)
            target.switchChannel(Channel.findNextChannel(target.asPlayer()));

        target.sendLocaleMessage("command-gc-kick-kicked",
                StringPlaceholders.of("name", group.getName()));
        for (UUID uuid : group.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member == null)
                continue;

            this.getLocaleManager().sendComponentMessage(member, "command-gc-kick-success",
                    StringPlaceholders.of(
                            "player", target.getName(),
                            "name", group.getName()
                    ));
        }
    }

}
