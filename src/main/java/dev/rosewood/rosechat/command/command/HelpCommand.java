package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.manager.CommandManager;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class HelpCommand extends RoseChatCommand {

    private final BaseRoseCommand parent;

    public HelpCommand(RosePlugin rosePlugin, BaseRoseCommand parent) {
        super(rosePlugin);
        this.parent = parent;
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("help")
                .descriptionKey("command-help-description")
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        ArgumentsDefinition argumentsDefinition = this.parent.getCommandArguments();
        Argument argument = argumentsDefinition.get(0);
        if (!(argument instanceof Argument.SubCommandArgument subCommandArgument))
            throw new IllegalStateException("Help command parent must have a subcommand argument.");

        this.getLocaleManager().sendComponentMessage(context.getSender(), "command-help-title");
        for (RoseCommand command : subCommandArgument.subCommands()) {
            String descriptionKey = command.getDescriptionKey();
            if (!command.canUse(context.getSender()) || descriptionKey == null)
                continue;

            StringPlaceholders stringPlaceholders = StringPlaceholders.of(
                    "cmd", context.getCommandLabel().toLowerCase(),
                    "subcmd", command.getName().toLowerCase(),
                    "args", command.getParametersString(context),
                    "desc", this.getLocaleManager().getLocaleMessage(descriptionKey)
            );

            this.getLocaleManager().sendSimpleCommandMessage(context.getSender(),
                    "command-help-list-description",
                    stringPlaceholders);
        }

        for (RoseCommandWrapper wrapper : RoseChat.getInstance().getManager(CommandManager.class).getActiveCommands()) {
            RoseCommand command = wrapper.getWrappedCommand();
            if (command.equals(this.parent))
                continue;

            String descriptionKey = command.getDescriptionKey();
            if (!command.canUse(context.getSender()) || descriptionKey == null)
                continue;

            StringPlaceholders stringPlaceholders = StringPlaceholders.of(
                    "cmd", command.getName().toLowerCase(),
                    "args", command.getParametersString(context),
                    "desc", this.getLocaleManager().getLocaleMessage(descriptionKey)
            );

            this.getLocaleManager().sendSimpleCommandMessage(context.getSender(),
                    "command-help-list-description-no-sub",
                    stringPlaceholders);
        }
    }

}
