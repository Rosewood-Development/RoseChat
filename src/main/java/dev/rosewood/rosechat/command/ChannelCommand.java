package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
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
            PlayerData data = null;
            RoseSender roseSender = new RoseSender(sender);
            if (roseSender.isPlayer() && roseSender.getUUID() != null) data = this.getAPI().getPlayerData(roseSender.getUUID());

            if (!channel.canSendMessage(roseSender, data, message)) return;
            if (!channel.isJoinable()) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-cannot-message");
                return;
            }

            MessageWrapper messageWrapper = new MessageWrapper(roseSender, MessageLocation.CHANNEL, channel, message).filter().applyDefaultColor();
            if (!messageWrapper.canBeSent()) {
                if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().sendWarning(roseSender);
                return;
            }

            channel.send(messageWrapper);
            BaseComponent[] messageComponents = messageWrapper.toComponents();
            if (messageComponents != null) Bukkit.getConsoleSender().spigot().sendMessage(messageComponents);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (String channel : this.getAPI().getChannelIDs()) {
                if (sender.hasPermission("rosechat.channel." + channel)) tab.add(channel);
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

            if (!newChannel.isJoinable()) {
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
