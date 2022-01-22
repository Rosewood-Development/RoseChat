package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class IgnoreCommand extends AbstractCommand {

    public IgnoreCommand() {
        super(true, "ignore");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            return;
        }

        Player player = (Player) sender;
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "player-not-found");
            return;
        }

        PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());

        if (playerData.getIgnoringPlayers().contains(target.getUniqueId())) {
            playerData.unignore(target.getUniqueId());
            this.getAPI().getLocaleManager().sendMessage(sender, "command-ignore-unignored", StringPlaceholders.single("player", target.isOnline() ? target.getPlayer().getDisplayName() : target.getName()));
        } else {
            playerData.ignore(target.getUniqueId());
            this.getAPI().getLocaleManager().sendMessage(sender, "command-ignore-ignored", StringPlaceholders.single("player", target.isOnline() ? target.getPlayer().getDisplayName() : target.getName()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != sender) tab.add(player.getName());
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.ignore";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-ignore-usage");
    }
}
