package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
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
                if (args.length == 0) {
                    if (!ChannelCommand.switchChannel(sender, cmd)) {
                        RoseChatAPI.getInstance().getLocaleManager()
                                .sendMessage(sender, "command-channel-custom-usage", StringPlaceholders.single("channel", channel.getCommand()));
                    }
                } else {
                    String message = AbstractCommand.getAllArgs(0, args);
                    String colorified = HexUtils.colorify(message);
                    if (ChatColor.stripColor(colorified).isEmpty()) {
                        RoseChatAPI.getInstance().getLocaleManager().sendMessage(sender, "message-blank");
                        return true;
                    }

                    RoseSender roseSender = new RoseSender(sender);
                    MessageWrapper messageWrapper = new MessageWrapper(roseSender, MessageLocation.CHANNEL, channel, message).validate().filter();
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
