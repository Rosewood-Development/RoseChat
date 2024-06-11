package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

public class CustomChannelCommand extends Command {

    public CustomChannelCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        RosePlayer player = new RosePlayer(sender);
        RoseChatAPI api = RoseChatAPI.getInstance();

        for (Channel channel : api.getChannels()) {
            if (!channel.getCommands().contains(label.toLowerCase()))
                continue;

            if (!player.hasPermission("rosechat.channel." + channel.getId())) {
                player.sendLocaleMessage("no-permission");
                return false;
            }

            if (player.isPlayer()) {
                if (!channel.canJoinByCommand(player.asPlayer())) {
                    player.sendLocaleMessage("no-permission");
                    return false;
                }
            }

            // Switch channels if the player doesn't specify a message.
            if (args.length == 0) {
                if (player.isConsole()) {
                    player.sendLocaleMessage("only-player");
                    return false;
                }

                // Move the player to the default channel if they're attempting to switch to the channel they're in.
                if (channel.getId().equals(player.getPlayerData().getCurrentChannel().getId()))
                    channel = api.getDefaultChannel();

                player.switchChannel(channel);

                player.sendLocaleMessage("command-channel-joined",
                        StringPlaceholders.of("id", channel.getId()));
                return true;
            }

            String message = String.join(" ", args);
            player.quickChat(channel, message);
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }

}
