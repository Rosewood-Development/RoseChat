package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;

public class BaseGroupCommand extends BaseRoseCommand {

    public BaseGroupCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("group")
                .aliases("gc")
                .arguments(ArgumentsDefinition.builder()
                        .optionalSub(
                                new GroupHelpCommand(this.rosePlugin, this),
                                new GroupAcceptCommand(this.rosePlugin),
                                new GroupCreateCommand(this.rosePlugin),
                                new GroupDenyCommand(this.rosePlugin),
                                new GroupDisbandCommand(this.rosePlugin),
                                new GroupInfoCommand(this.rosePlugin),
                                new GroupInviteCommand(this.rosePlugin),
                                new GroupKickCommand(this.rosePlugin),
                                new GroupLeaveCommand(this.rosePlugin),
                                new GroupListCommand(this.rosePlugin),
                                new GroupMembersCommand(this.rosePlugin),
                                new GroupRenameCommand(this.rosePlugin)
                        ))
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        GroupHelpCommand.displayHelpMessage(this, context);
    }

}
