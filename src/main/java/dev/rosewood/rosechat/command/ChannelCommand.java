package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.RosePlayer;
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
            Channel channel = this.getAPI().getChannelById(args[0]);
            if (channel == null) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
                return;
            }

            String message = getAllArgs(1, args);
            RosePlayer rosePlayer = new RosePlayer(sender);
            channel.send(rosePlayer, message);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Channel channel : this.getAPI().getChannels()) {
                if (channel.canJoinByCommand((Player) sender)) tab.add(channel.getId());
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
            Channel oldChannel = api.getPlayerData(player.getUniqueId()).getCurrentChannel();
            Channel newChannel = api.getChannelById(channel);

            if (newChannel == null) {
                api.getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
                return true;
            }

            if (!sender.hasPermission("rosechat.channel." + newChannel.getId())) {
                api.getLocaleManager().sendComponentMessage(sender, "no-permission");
                return true;
            }

            if (!newChannel.canJoinByCommand(player)) {
                api.getLocaleManager().sendComponentMessage(sender, "command-channel-not-joinable");
                return true;
            }

            oldChannel.onLeave(player);
            newChannel.onJoin(player);

            PlayerData playerData = api.getPlayerData(player.getUniqueId());
            playerData.setCurrentChannel(newChannel);
            playerData.setIsInGroupChannel(false);
            playerData.save();

            api.getLocaleManager().sendMessage(sender, "command-channel-joined", StringPlaceholders.single("id", newChannel.getId()));
            return true;
        } else {
            return false;
        }
    }

}
