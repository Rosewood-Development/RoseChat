package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
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
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        ChatChannel oldChannel = this.getAPI().getPlayerData(((Player) sender).getUniqueId()).getCurrentChannel();
        ChatChannel channel = null;
        for (String channelStr : this.getAPI().getChannelIDs()) {
            if (channelStr.equalsIgnoreCase(args[0])) {
                channel = this.getAPI().getChannelById(channelStr);
                break;
            }
        }

        if (channel == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-channel-not-found");
            return;
        }

        if (args.length == 1) {
            Player player = (Player) sender;

            this.getAPI().getLocaleManager().sendMessage(sender, "command-channel-joined", StringPlaceholders.single("id", channel.getId()));
            oldChannel.remove(player);
            channel.add(player);

            PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
            playerData.setCurrentChannel(channel);
            playerData.save();
        } else {
            String message = getAllArgs(1, args);
            //  TODO: Check channel permissions & config settings.
            /*MessageWrapperOld messageWrapperOld = new MessageWrapperOld((Player) sender, message)
                    .checkAll("rosechat.chat")
                    .filterAll("rosechat.message")
                    .withReplacements()
                    .withTags()
                    .inChannel(channel)
                    .parse(channel.getFormatId(), null);

            MessageUtils.sendStandardMessage(sender, messageWrapperOld, channel);*/
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
}
