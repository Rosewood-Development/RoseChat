package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RealnameCommand extends AbstractCommand {
    public RealnameCommand() {
        super(false, "realname");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        Player player = MessageUtils.getPlayer(args[0]);
        if (player == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
            return;
        }

        PlayerData data = this.getAPI().getPlayerData(player.getUniqueId());
        String name = data.getNickname() != null ? data.getNickname() : player.getDisplayName();

        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-realname-success",
                StringPlaceholders.builder().add("player", player.getName())
                        .add("name", name).build());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (MessageUtils.isPlayerVanished(player))
                    continue;

                tab.add(player.getName());
            }

            return tab;
        }

        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.realname";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-realname-usage");
    }

}
