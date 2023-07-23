package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class DeleteMessageCommand extends AbstractCommand {

    public DeleteMessageCommand() {
        super(true, "delmsg");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        Player player = (Player) sender;
        UUID uuid = UUID.fromString(args[0]);

        // Check if the message is a client message.
        boolean isClient = false;
        PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
        for (DeletableMessage message : playerData.getMessageLog().getDeletableMessages()) {
            if (message.getUUID().equals(uuid) && message.isClient()) {
                isClient = true;
                break;
            }
        }

        if (isClient) {
            this.getAPI().deleteMessage(new RosePlayer(player), uuid);
        } else {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                this.getAPI().deleteMessage(new RosePlayer(onlinePlayer), uuid);
            }
        }

        if (args.length > 1) {
            Channel channel = this.getAPI().getChannelById(args[1]);
            if (channel == null) return;
            for (String server : channel.getServers()) {
                this.getAPI().getBungeeManager().sendMessageDeletion(server,  uuid);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return null;
    }

}
