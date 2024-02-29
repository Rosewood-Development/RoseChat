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

public class UnmuteCommand extends AbstractCommand {

    public UnmuteCommand() {
        super(false, "unmute");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || args.length == 2) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
            return;
        }

        PlayerData targetData = this.getAPI().getPlayerData(target.getUniqueId());
        targetData.unmute();

        String name = targetData.getNickname() == null ? target.getDisplayName() : targetData.getNickname();
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-unmute-success",
                StringPlaceholders.builder("player", name).build());

        this.getAPI().getLocaleManager().sendComponentMessage(target, "command-mute-unmuted");

        targetData.save();
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
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.unmute";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-unmute-usage");
    }

}
