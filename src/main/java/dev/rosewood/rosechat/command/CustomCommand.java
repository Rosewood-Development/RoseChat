package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

public class CustomCommand extends Command {

    public CustomCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        for (ChatChannel channel : RoseChatAPI.getInstance().getChannels()) {
            if (channel.getCommand() != null && channel.getCommand().equalsIgnoreCase(cmd)) {
                if (!sender.hasPermission("rosechat.channel." + channel.getId())) {
                    RoseChatAPI.getInstance().getLocaleManager().sendMessage(sender, "no-permission");
                    return false;
                }

                if (args.length == 0) {
                    if (!ChannelCommand.switchChannel(sender, cmd)) {
                        RoseChatAPI.getInstance().getLocaleManager()
                                .sendMessage(sender, "command-channel-custom-usage", StringPlaceholders.single("channel", channel.getCommand()));
                    }
                } else {
                    String message = AbstractCommand.getAllArgs(0, args);

                    PlayerData data = null;
                    RoseSender roseSender = new RoseSender(sender);
                    if (roseSender.isPlayer() && roseSender.getUUID() != null) data = RoseChatAPI.getInstance().getPlayerData(roseSender.getUUID());
                    if (!channel.canSendMessage(roseSender, data, message)) return false;

                    MessageWrapper messageWrapper = new MessageWrapper(roseSender, MessageLocation.CHANNEL, channel, message).validate().filter().applyDefaultColor();
                    if (!messageWrapper.canBeSent()) {
                        if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().sendWarning(roseSender);
                        return true;
                    }

                    channel.send(messageWrapper);
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
