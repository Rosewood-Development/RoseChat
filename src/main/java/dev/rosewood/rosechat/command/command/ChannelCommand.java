package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class ChannelCommand extends RoseChatCommand {

    public ChannelCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("channel")
                .aliases("c")
                .descriptionKey("command-channel-description")
                .permission("rosechat.channel")
                .arguments(ArgumentsDefinition.builder()
                        .required("channel", RoseChatArgumentHandlers.CHANNEL)
                        .optional("message", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, Channel channel) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (player.isConsole()) {
            player.sendLocaleMessage("only-player");
            return;
        }

        // Return if the player can't join the channel.
        if (!channel.canJoinByCommand(player.asPlayer())) {
            player.sendLocaleMessage("command-channel-not-joinable");
            return;
        }

        // Move the player to the default channel if they're attempting to switch to the channel they're in.
        if (channel.getId().equals(player.getPlayerData().getCurrentChannel().getId()))
            channel = this.getAPI().getDefaultChannel();

        player.switchChannel(channel, true);

        player.sendLocaleMessage("command-channel-joined",
                StringPlaceholders.of("id", channel.getId()));
    }

    @RoseExecutable
    public void execute(CommandContext context, Channel channel, String message) {
        RosePlayer player = new RosePlayer(context.getSender());
        if (!player.hasPermission("rosechat.channel." + channel.getId())) {
            player.sendLocaleMessage("no-permission");
            return;
        }

        player.quickChat(channel, message);
    }

}