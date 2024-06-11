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

public class ChatMoveCommand extends RoseChatCommand {

    public ChatMoveCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("move")
                .descriptionKey("command-chat-move-description")
                .permission("rosechat.chat.move")
                .arguments(ArgumentsDefinition.builder()
                        .required("player", RoseChatArgumentHandlers.ROSE_PLAYER)
                        .required("channel", new ChannelArgumentHandler(false))
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, RosePlayer target, Channel channel) {
        // Move the player to the default channel if they're attempting to switch to the channel they're in.
        if (channel.getId().equals(target.getPlayerData().getCurrentChannel().getId()))
            channel = this.getAPI().getDefaultChannel();

        boolean success = target.switchChannel(channel);
        if (!success)
            return;

        this.getLocaleManager().sendComponentMessage(context.getSender(), "command-chat-move-success",
                StringPlaceholders.of(
                        "player", target.getName(),
                        "channel", channel.getId()));
        this.getLocaleManager().sendComponentMessage(target, "command-chat-move-moved",
                StringPlaceholders.of("channel", channel.getId()));
    }

}
