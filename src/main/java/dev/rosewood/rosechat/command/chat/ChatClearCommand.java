package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.ChannelArgumentHandler;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class ChatClearCommand extends RoseChatCommand {

    public ChatClearCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("clear")
                .descriptionKey("command-chat-clear-description")
                .permission("rosechat.chat.clear")
                .arguments(ArgumentsDefinition.builder()
                        .optional("channel", new ChannelArgumentHandler(false))
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (player.isConsole()) {
            player.sendLocaleMessage("only-player");
            return;
        }

        this.execute(player, player.getChannel());
    }

    @RoseExecutable
    public void execute(CommandContext context, Channel channel) {
        this.execute(new RosePlayer(context.getSender()), channel);
    }

    private void execute(RosePlayer player, Channel channel) {
        for (int i = 0; i < 100; i++)
            channel.send("\n");

        player.sendLocaleMessage("command-chat-clear-cleared",
                StringPlaceholders.of("channel", channel.getId()));
    }

}
