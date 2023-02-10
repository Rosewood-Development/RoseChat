package dev.rosewood.rosechat.command;

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
       /* for (ChatChannel channel : RoseChatAPI.getInstance().getChannels()) {
            if (channel.getCommand() != null && channel.getCommand().equalsIgnoreCase(cmd)) {
                if (!sender.hasPermission("rosechat.channel." + channel.getId())) {
                    RoseChatAPI.getInstance().getLocaleManager().sendComponentMessage(sender, "no-permission");
                    return false;
                }

                if (args.length == 0) {
                    if (!ChannelCommand.processChannelSwitch(sender, channel.getId())) {
                        RoseChatAPI.getInstance().getLocaleManager()
                                .sendComponentMessage(sender, "command-channel-custom-usage", StringPlaceholders.single("channel", channel.getCommand()));
                    }
                } else {
                    String message = AbstractCommand.getAllArgs(0, args);

                    RoseSender roseSender = new RoseSender(sender);
                    if (!channel.canSendMessage(roseSender, message)) return false;

                    MessageWrapper messageWrapper = new MessageWrapper(roseSender, MessageLocation.CHANNEL, channel, message).filter().applyDefaultColor();
                    if (!messageWrapper.canBeSent()) {
                        if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().sendWarning(roseSender);
                        return true;
                    }

                    channel.send(messageWrapper);
                    BaseComponent[] messageComponents = messageWrapper.toComponents();
                    if (messageComponents != null) Bukkit.getConsoleSender().spigot().sendMessage(messageComponents);
                }
                return true;
            }
        }*/

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }

}
