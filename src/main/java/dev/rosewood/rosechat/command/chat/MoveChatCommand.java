package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class MoveChatCommand extends AbstractCommand {

    public MoveChatCommand() {
        super(false, "move");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-chat-move-usage");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
            return;
        }

        PlayerData data = this.getAPI().getPlayerData(player.getUniqueId());
        String channelStr = args[1];

        if (this.getAPI().getChannelById(channelStr) == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
            return;
        }

        Channel oldChannel = this.getAPI().getPlayerData(player.getUniqueId()).getCurrentChannel();
        Channel channel = this.getAPI().getChannelById(channelStr);

        RosePlayer rosePlayer = new RosePlayer(player);
        if (!rosePlayer.changeChannel(oldChannel, channel)) {
            return;
        }

        String name = data.getNickname() == null ? player.getDisplayName() : data.getNickname();

        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-chat-move-success",
                StringPlaceholders.builder("player", name)
                        .add("channel", channel.getId()).build());
        this.getAPI().getLocaleManager().sendComponentMessage(player, "command-chat-move-moved", StringPlaceholders.of("channel", channel.getId()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                tab.add(player.getName());
            }
        } else if (args.length == 2) {
            tab.addAll(this.getAPI().getChannelIDs());
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.chat.move";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-chat-move-usage");
    }

}
