package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class ChatHelpCommand extends RoseChatCommand {

    private final BaseRoseCommand parent;

    public ChatHelpCommand(RosePlugin rosePlugin, BaseRoseCommand parent) {
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
        displayHelpMessage(this.parent, context);
    }

    public static void displayHelpMessage(BaseRoseCommand parent, CommandContext context) {
        LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
        ArgumentsDefinition argumentsDefinition = parent.getCommandArguments();

        Argument argument = argumentsDefinition.get(0);
        if (!(argument instanceof Argument.SubCommandArgument subCommandArgument))
            throw new IllegalStateException("Help command parent must have a subcommand argument.");

        localeManager.sendComponentMessage(context.getSender(), "command-help-title");
        for (RoseCommand command : subCommandArgument.subCommands()) {
            String descriptionKey = command.getDescriptionKey();
            if (!command.canUse(context.getSender()) || descriptionKey == null)
                continue;

            StringPlaceholders stringPlaceholders = StringPlaceholders.of(
                    "cmd", context.getCommandLabel().toLowerCase(),
                    "subcmd", command.getName().toLowerCase(),
                    "args", command.getParametersString(context),
                    "desc", localeManager.getLocaleMessage(descriptionKey)
            );

            localeManager.sendSimpleCommandMessage(context.getSender(),
                    "command-chat-help-list-description",
                    stringPlaceholders);
        }
    }

}