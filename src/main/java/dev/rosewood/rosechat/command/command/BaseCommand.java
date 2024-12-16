package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.PrimaryCommand;
import dev.rosewood.rosegarden.command.ReloadCommand;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandInfo;

public class BaseCommand extends PrimaryCommand {

    public BaseCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("rc")
                .aliases("rosechat")
                .permission("rosechat.basecommand")
                .arguments(ArgumentsDefinition.builder()
                        .optionalSub(
                                new HelpCommand(this.rosePlugin, this),
                                new ReloadCommand(this.rosePlugin),
                                new DebugCommand(this.rosePlugin)
                        ))
                .build();
    }

}
