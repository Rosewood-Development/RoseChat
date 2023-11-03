package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.Collections;
import java.util.List;

public class CustomCommand extends Command {

    public CustomCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        for (Channel channel : RoseChatAPI.getInstance().getChannels()) {
            if (channel.getCommands().contains(cmd.toLowerCase())) {
                if (!sender.hasPermission("rosechat.channel." + channel.getId())) {
                    RoseChatAPI.getInstance().getLocaleManager().sendComponentMessage(sender, "no-permission");
                    return false;
                }

                if (args.length == 0) {
                    if (!ChannelCommand.processChannelSwitch(sender, channel.getId())) {
                        RoseChatAPI.getInstance().getLocaleManager()
                                .sendComponentMessage(sender, "command-channel-custom-usage", StringPlaceholders.of("channel", cmd.toLowerCase()));
                    }
                } else {
                    String message = AbstractCommand.getAllArgs(0, args);
                    RosePlayer rosePlayer = new RosePlayer(sender);
                    if (sender instanceof Player player) {
                        rosePlayer.getPlayerData().setActiveChannel(channel);

                        AsyncPlayerChatEvent asyncPlayerChatEvent = new AsyncPlayerChatEvent(!Bukkit.isPrimaryThread(), player, message, Collections.emptySet());
                        Bukkit.getPluginManager().callEvent(asyncPlayerChatEvent);

                        rosePlayer.getPlayerData().setActiveChannel(null);
                    } else {
                        channel.send(rosePlayer, message);
                    }
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
