package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.ChannelArgumentHandler;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class ChatSlowmodeCommand extends RoseChatCommand {

    public ChatSlowmodeCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("slowmode")
                .descriptionKey("command-chat-slowmode-description")
                .permission("rosechat.chat.slowmode")
                .arguments(ArgumentsDefinition.builder()
                        .required("channel", new ChannelArgumentHandler(false))
                        .required("speed", ArgumentHandlers.INTEGER)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, Channel channel, Integer speed) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (speed == 0) {
            channel.stopSlowmode(true);
            channel.setSlowmodeSpeed(0);

            player.sendLocaleMessage("command-chat-slowmode-off",
                    StringPlaceholders.of("channel", channel.getId()));

            RoseChatAPI.getInstance().getPlayerDataManager().saveChannelSettings(channel);
            return;
        }

        channel.stopSlowmode(false);
        channel.setSlowmodeSpeed(speed);
        channel.startSlowmode();

        player.sendLocaleMessage("command-chat-slowmode-on",
                StringPlaceholders.of(
                        "speed", speed,
                        "channel", channel.getId()
                ));

        RoseChatAPI.getInstance().getPlayerDataManager().saveChannelSettings(channel);
    }

}
