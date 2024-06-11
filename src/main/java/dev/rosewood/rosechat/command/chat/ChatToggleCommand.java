package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class ChatToggleCommand extends RoseChatCommand {

    public ChatToggleCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("toggle")
                .descriptionKey("command-chat-toggle-description")
                .permission("rosechat.chat.toggle")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .required("channel", RoseChatArgumentHandlers.CHANNEL)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, Channel channel) {
        RosePlayer player = new RosePlayer(context.getSender());
        PlayerData data = player.getPlayerData();

        if (data.isChannelHidden(channel.getId())) {
            data.showChannel(channel.getId());
            player.sendLocaleMessage("command-chat-toggle-on",
                    StringPlaceholders.of("channel", channel.getId()));
        } else {
            data.hideChannel(channel.getId());
            player.sendLocaleMessage("command-chat-toggle-off",
                    StringPlaceholders.of("channel", channel.getId()));
        }

        data.save();
    }

}
