package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class MessageCommand extends AbstractCommand {

    public MessageCommand() {
        super(false, "message", "msg", "m", "pm", "whisper", "w", "tell", "t");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        if (args.length == 1) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-message-enter-message");
            return;
        }

        String target = args[0];

        if (!target.equalsIgnoreCase("Console") && Bukkit.getPlayer(args[0]) == null && ConfigurationManager.Setting.ALLOW_BUNGEECORD_MESSAGES.getBoolean() && this.getAPI().isBungee()) {
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

        String message = getAllArgs(1, args);

        if (message.isEmpty()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-message-enter-message");
            return;
        }

        String colorified = HexUtils.colorify(message);
        if (ChatColor.stripColor(colorified).isEmpty()) {
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

        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
            playerData.setReplyTo(target);
            playerData.save();
        }

        if (this.getAPI().isBungee()) {
            BungeeListener.updateReply(sender.getName(), target);
        } else {
            PlayerData targetData = this.getAPI().getPlayerData(targetPlayer.getUniqueId());
            targetData.setReplyTo(sender.getName());
            targetData.save();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            BungeeListener.getPlayers("ALL");

            for (Player player : Bukkit.getOnlinePlayers()){
                if (sender instanceof Player && player.getUniqueId().equals(((Player) sender).getUniqueId())) continue;
                tab.add(player.getName());
            }

            if (ConfigurationManager.Setting.ALLOW_BUNGEECORD_MESSAGES.getBoolean()) {
                if (this.getAPI().getDataManager().getBungeePlayers().containsKey("ALL")) {
                    List<String> players = this.getAPI().getDataManager().getPlayersOnServer("ALL");
                    for (String player : players) {
                        if (sender instanceof Player && sender.getName().equalsIgnoreCase(player)) continue;
                        tab.add(player);
                    }
                }
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.message";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-message-usage");
    }
}
