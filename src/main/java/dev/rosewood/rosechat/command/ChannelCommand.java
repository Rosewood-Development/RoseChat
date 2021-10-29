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
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
        } else if (args.length == 1) {
            if (!switchChannel(sender, args[0])) {
                this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            }
        } else {
            ChatChannel channel = this.getAPI().getChannelById(args[0]);
            if (channel == null) {
                this.getAPI().getLocaleManager().sendMessage(sender, "command-channel-not-found");
                return;
            }

            if (!channel.isJoinable()) {
                this.getAPI().getLocaleManager().sendMessage(sender, "command-channel-not-joinable");
                return;
            }

            String message = getAllArgs(1, args);

            if (MessageUtils.isMessageEmpty(message)) {
                this.getAPI().getLocaleManager().sendMessage(sender, "message-blank");
                return;
            }

            RoseSender roseSender = new RoseSender(sender);
            MessageWrapper messageWrapper = new MessageWrapper(roseSender, MessageLocation.CHANNEL, channel, message).validate().filter();
            if (!messageWrapper.canBeSent()) {
                if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().sendWarning(roseSender);
                return;
            }

            channel.send(messageWrapper);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) tab.addAll(this.getAPI().getChannelIDs());

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

    public static boolean switchChannel(CommandSender sender, String channel) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            ChatChannel oldChannel = api.getPlayerData(player.getUniqueId()).getCurrentChannel();
            ChatChannel newChannel = api.getChannelById(channel);

            if (newChannel == null) {
                api.getLocaleManager().sendMessage(sender, "command-channel-not-found");
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
