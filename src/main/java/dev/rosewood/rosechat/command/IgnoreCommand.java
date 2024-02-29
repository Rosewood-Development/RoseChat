package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IgnoreCommand extends AbstractCommand {

    public IgnoreCommand() {
        super(true, "ignore");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", this.getSyntax()));
            return;
        }

        Player player = (Player) sender;
        PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());

        // List the players that the sender is ignoring.
        if (args[0].equalsIgnoreCase("list")) {
            List<UUID> ignoring = playerData.getIgnoringPlayers();
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-ignore-list-title", StringPlaceholders.of("amount", ignoring.size()));

            if (ignoring.isEmpty()) return;
            StringBuilder playersBuilder = new StringBuilder();
            playersBuilder.append(this.getAPI().getLocaleManager().getMessage("command-ignore-list-color"));

            boolean first = true;
            for (UUID uuid : ignoring) {
                OfflinePlayer ignoringPlayer = Bukkit.getOfflinePlayer(uuid);
                if (ignoringPlayer == null) continue;

                String name = ignoringPlayer.isOnline() ? ignoringPlayer.getPlayer().getDisplayName() : ignoringPlayer.getName();
                if (first) {
                    playersBuilder.append(name);
                    first = false;
                } else {
                    playersBuilder.append(this.getAPI().getLocaleManager().getMessage("command-ignore-list-separator")).append(name);
                }
            }

            player.sendMessage(HexUtils.colorify(playersBuilder.toString()));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
            return;
        }


        PlayerData targetData = this.getAPI().getPlayerData(target.getUniqueId());
        if (targetData == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
            return;
        }

        String name = targetData.getNickname() == null ? target.getName() : targetData.getNickname();

        if (playerData.getIgnoringPlayers().contains(target.getUniqueId())) {
            playerData.unignore(target.getUniqueId());
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-ignore-unignored", StringPlaceholders.of("player", name));
        } else {
            playerData.ignore(target.getUniqueId());
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-ignore-ignored", StringPlaceholders.of("player", name));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (MessageUtils.isPlayerVanished(player))
                    continue;

                if (player != sender) tab.add(player.getName());
            }
            tab.add("list");
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
