package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
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
            this.getAPI().getLocaleManager().sendMessage(sender, "command-chat-move-usage");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        PlayerData data = this.getAPI().getPlayerData(player.getUniqueId());
        String channelStr = args[1];

        if (player == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "player-not-found");
            return;
        }

        if (this.getAPI().getChannelById(channelStr) == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-channel-not-found");
            return;
        }

        ChatChannel oldChannel = this.getAPI().getPlayerData(player.getUniqueId()).getCurrentChannel();
        ChatChannel channel = this.getAPI().getChannelById(channelStr);

        oldChannel.remove(player);
        channel.add(player);
        data.setCurrentChannel(channel);
        data.save();

        this.getAPI().getLocaleManager().sendMessage(sender, "command-chat-move-success",
                StringPlaceholders.builder("player", player.getDisplayName())
                        .addPlaceholder("channel", channel.getId()).build());
        this.getAPI().getLocaleManager().sendMessage(player, "command-chat-move-moved", StringPlaceholders.single("channel", channel.getId()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != sender) tab.add(player.getName());
            }
        } else if (args.length == 2) {
            tab.addAll(this.getAPI().getChannelIDs());
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.admin.move";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-chat-move-usage");
    }
}
