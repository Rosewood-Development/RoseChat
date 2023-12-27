package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelCommand extends AbstractCommand {

    public ChannelCommand() {
        super(false, "channel", "c");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", this.getSyntax()));
        } else if (args.length == 1) {
            if (!processChannelSwitch(sender, args[0])) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", this.getSyntax()));
            }
        } else {
            RosePlayer rosePlayer = new RosePlayer(sender);

            Channel channel = this.getAPI().getChannelById(args[0]);
            if (channel == null) {
                boolean foundGroupChannel = false;
                if (rosePlayer.isPlayer() && ConfigurationManager.Setting.ADD_GROUP_CHANNELS_TO_CHANNEL_LIST.getBoolean()) {
                    for (GroupChannel groupChannel : this.getAPI().getGroupChats(rosePlayer.getUUID())) {
                        if (groupChannel.getId().equalsIgnoreCase(args[0])) {
                            channel = groupChannel;
                            foundGroupChannel = true;
                            break;
                        }
                    }
                }

                if (!foundGroupChannel) {
                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
                    return;
                }
            }

            String message = getAllArgs(1, args);

            if (sender instanceof Player player) {
                rosePlayer.getPlayerData().setActiveChannel(channel);

                AsyncPlayerChatEvent asyncPlayerChatEvent = new AsyncPlayerChatEvent(!Bukkit.isPrimaryThread(), player, message, Collections.emptySet());
                Bukkit.getPluginManager().callEvent(asyncPlayerChatEvent);

                rosePlayer.getPlayerData().setActiveChannel(null);
            } else {
                channel.send(rosePlayer, message);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Channel channel : this.getAPI().getChannels()) {
                if (channel.canJoinByCommand((Player) sender)) tab.add(channel.getId());
            }

            if (ConfigurationManager.Setting.ADD_GROUP_CHANNELS_TO_CHANNEL_LIST.getBoolean()) {
                for (GroupChannel groupChat : this.getAPI().getGroupChats(((Player) sender).getUniqueId())) {
                    tab.add(groupChat.getId());
                }
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

        if (sender instanceof Player player) {
            RosePlayer rosePlayer = new RosePlayer(player);

            Channel oldChannel = api.getPlayerData(player.getUniqueId()).getCurrentChannel();
            Channel newChannel = api.getChannelById(channel);

            if (newChannel == null) {
                boolean foundGroupChannel = false;
                if (rosePlayer.isPlayer() && ConfigurationManager.Setting.ADD_GROUP_CHANNELS_TO_CHANNEL_LIST.getBoolean()) {
                    for (GroupChannel groupChannel : api.getGroupChats(rosePlayer.getUUID())) {
                        if (groupChannel.getId().equalsIgnoreCase(channel)) {
                            newChannel = groupChannel;
                            foundGroupChannel = true;
                            break;
                        }
                    }
                }

                if (!foundGroupChannel) {
                    api.getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
                    return true;
                }
            }

            if (!sender.hasPermission("rosechat.channel." + newChannel.getId())) {
                api.getLocaleManager().sendComponentMessage(sender, "no-permission");
                return true;
            }

            if (!newChannel.canJoinByCommand(player)) {
                api.getLocaleManager().sendComponentMessage(sender, "command-channel-not-joinable");
                return true;
            }

            rosePlayer.getPlayerData().setIsInGroupChannel(false);
            if (!rosePlayer.changeChannel(oldChannel, newChannel)) {
                return true;
            }

            api.getLocaleManager().sendMessage(sender, "command-channel-joined", StringPlaceholders.of("id", newChannel.getId()));
            return true;
        } else {
            return false;
        }
    }

}
