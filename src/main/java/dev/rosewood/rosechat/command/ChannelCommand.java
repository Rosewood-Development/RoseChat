package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class ChannelCommand extends AbstractCommand {

    public ChannelCommand() {
        super(false, "channel", "c");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
        } else if (args.length == 1) {
            if (!processChannelSwitch(sender, args[0])) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            }
        } else {
            ChatChannel channel = this.getAPI().getChannelById(args[0]);
            if (channel == null) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
                return;
            }

            String message = getAllArgs(1, args);
            RoseSender roseSender = new RoseSender(sender);

            if (!channel.canSendMessage(roseSender, message)) return;
            if (!channel.isJoinable() && !(sender.hasPermission("rosechat.channelbypass"))) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-cannot-message");
                return;
            }

            MessageWrapper messageWrapper = new MessageWrapper(roseSender, MessageLocation.CHANNEL, channel, message).filter().applyDefaultColor();
            MessageUtils.sendMessageWrapper(roseSender, channel, messageWrapper);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (ChatChannel channel : this.getAPI().getChannels()) {
                if (sender.hasPermission("rosechat.channel." + channel.getId())
                        && channel.isJoinable()) tab.add(channel.getId());
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.channel";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-channel-usage");
    }

    public static boolean processChannelSwitch(CommandSender sender, String channel) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            ChatChannel oldChannel = api.getPlayerData(player.getUniqueId()).getCurrentChannel();
            ChatChannel newChannel = api.getChannelById(channel);

            if (newChannel == null) {
                api.getLocaleManager().sendMessage(sender, "command-channel-not-found");
                return true;
            }

            if (!sender.hasPermission("rosechat.channel." + newChannel.getId())) {
                api.getLocaleManager().sendMessage(sender, "no-permission");
                return true;
            }

            if (!newChannel.isJoinable() && !(sender.hasPermission("rosechat.channelbypass"))) {
                api.getLocaleManager().sendComponentMessage(sender, "command-channel-not-joinable");
                return true;
            }

            oldChannel.remove(player);
            newChannel.add(player);

            PlayerData playerData = api.getPlayerData(player.getUniqueId());
            playerData.setCurrentChannel(newChannel);
            playerData.save();

            api.getLocaleManager().sendMessage(sender, "command-channel-joined", StringPlaceholders.single("id", newChannel.getId()));
            return true;
        } else {
            return false;
        }
    }

}
