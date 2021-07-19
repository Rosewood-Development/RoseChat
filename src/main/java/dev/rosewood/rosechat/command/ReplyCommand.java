package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class ReplyCommand extends AbstractCommand {

    public ReplyCommand() {
        super(true, "reply", "r");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-reply-enter-message");
            return;
        }

        Player player = (Player) sender;
        PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
        String target = playerData.getReplyTo();

        if (target == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-reply-no-one");
            return;
        }

        if (!target.equalsIgnoreCase("Console") && ConfigurationManager.Setting.ALLOW_BUNGEECORD_MESSAGES.getBoolean() && this.getAPI().isBungee()) {
            BungeeListener.getPlayers("ALL");
            if (!this.getAPI().getDataManager().getPlayersOnServer("ALL").contains(target)) {
                this.getAPI().getLocaleManager().sendMessage(sender, "player-not-found");
                return;
            }
        } else {
            if (!target.equalsIgnoreCase("Console") && Bukkit.getPlayer(target) == null) {
                this.getAPI().getLocaleManager().sendMessage(sender, "player-not-found");
                return;
            }
        }

        String message = getAllArgs(0, args);
        if (message.isEmpty()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-reply-enter-message");
            return;
        }

        if (MessageUtils.isMessageEmpty(message)) {
            this.getAPI().getLocaleManager().sendMessage(sender, "message-blank");
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);
        this.getAPI().getDataManager().getPlayerData(targetPlayer.getUniqueId(), data -> {
            if (targetPlayer != null && data != null) {
                if (!data.canBeMessaged()) {
                    this.getAPI().getLocaleManager().sendMessage(sender, "command-togglemessage-cannot-message");
                    return;
                }
            }
        });

        MessageUtils.sendPrivateMessage(sender, target, message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.reply";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-reply-usage");
    }
}
