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

public class ChatMuteCommand extends RoseChatCommand {

    public ChatMuteCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("mute")
                .descriptionKey("command-chat-mute-description")
                .permission("rosechat.chat.mute")
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
        channel.setMuted(!channel.isMuted());

        if (channel.isMuted()) {
            player.sendLocaleMessage("command-chat-mute-muted",
                    StringPlaceholders.of("channel", channel.getId()));
            this.getAPI().getPlayerDataManager().saveChannelSettings(channel);
        } else {
            player.sendLocaleMessage("command-chat-mute-unmuted",
                    StringPlaceholders.of("channel", channel.getId()));
            this.getAPI().getPlayerDataManager().saveChannelSettings(channel);
        }
    }

}
