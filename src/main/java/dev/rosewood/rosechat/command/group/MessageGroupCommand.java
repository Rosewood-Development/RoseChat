package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.ChannelCommand;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class MessageGroupCommand extends AbstractCommand {

    public MessageGroupCommand() {
        super(true, "gcm", "gcmsg", "gmsg");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        if (args.length == 1) {
            if (Setting.CAN_JOIN_GROUP_CHANNELS.getBoolean()) {
                if (!processChannelSwitch(sender, args[0])) {
                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
                }
                return;
            }

            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-message-enter-message");
            return;
        }

        GroupChannel groupChat = this.getAPI().getGroupChatById(args[0]);

        if (groupChat == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-does-not-exist");
            return;
        }

        Player player = (Player) sender;
        if (!groupChat.getMembers().contains(player.getUniqueId())) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-invalid");
            return;
        }

        String message = getAllArgs(1, args);

        if (MessageUtils.isMessageEmpty(message)) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "message-blank");
            return;
        }

        RosePlayer messageSender = new RosePlayer((Player) sender);
        groupChat.send(messageSender, message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (GroupChannel groupChat : this.getAPI().getGroupChats(((Player) sender).getUniqueId())) {
                tab.add(groupChat.getId());
            }
        } else if (args.length == 2) {
            tab.add("<message>");
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.message";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gcm-usage");
    }

    public static boolean processChannelSwitch(CommandSender sender, String group) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Channel oldChannel = api.getPlayerData(player.getUniqueId()).getCurrentChannel();
            GroupChannel newChannel = api.getGroupChatById(group);

            if (newChannel == null) {
                api.getLocaleManager().sendComponentMessage(sender, "gc-does-not-exist");
                return true;
            }

            if (!newChannel.getMembers().contains(player.getUniqueId())) {
                api.getLocaleManager().sendComponentMessage(sender, "gc-invalid");
                return true;
            }

            oldChannel.onLeave(player);
            // Don't join the channel if it is a group channel, as the player is already in it.

            PlayerData playerData = api.getPlayerData(player.getUniqueId());
            playerData.setCurrentChannel(newChannel);
            playerData.setIsInGroupChannel(true);
            playerData.save();

            api.getLocaleManager().sendComponentMessage(sender, "command-channel-joined", StringPlaceholders.of("id", newChannel.getName()));
            return true;
        } else {
            return false;
        }
    }

}
