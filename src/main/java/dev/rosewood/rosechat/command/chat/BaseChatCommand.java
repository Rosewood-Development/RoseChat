package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;

public class BaseChatCommand extends BaseRoseCommand {

    public BaseChatCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("chat")
                .arguments(ArgumentsDefinition.builder()
                        .optionalSub(
                                new ChatHelpCommand(this.rosePlugin, this),
                                new ChatClearCommand(this.rosePlugin),
                                new ChatInfoCommand(this.rosePlugin),
                                new ChatMoveCommand(this.rosePlugin),
                                new ChatMuteCommand(this.rosePlugin),
                                new ChatSudoCommand(this.rosePlugin),
                                new ChatToggleCommand(this.rosePlugin),
                                new ChatSlowmodeCommand(this.rosePlugin)
                        ))
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        ChatHelpCommand.displayHelpMessage(this, context);
    }

}
