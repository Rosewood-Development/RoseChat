package dev.rosewood.rosechat.command.group;

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

public class GroupPromoteCommand extends RoseChatCommand {

    public GroupPromoteCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("promote")
                .descriptionKey("command-gc-promote-description")
                .permission("rosechat.group.promote")
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

    public void execute(RosePlayer player, GroupChannel group, RosePlayer target) {
        if (group.getOwner().equals(target.getUUID())) {
            player.sendLocaleMessage("command-gc-promote-already-owner");
            return;
        }

        group.setOwner(target.getUUID());
        group.save();

        player.sendLocaleMessage("command-gc-promote-success",
                StringPlaceholders.of(
                        "player", target.getName(),
                        "name", group.getName()
                        ));
        target.sendLocaleMessage("command-gc-promote-promoted",
                StringPlaceholders.of("name", group.getName()));
    }

}
