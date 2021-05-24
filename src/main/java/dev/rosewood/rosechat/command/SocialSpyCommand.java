package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class SocialSpyCommand extends AbstractCommand {

    public SocialSpyCommand() {
        super(true, "socialspy", "ss", "spy");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = this.getAPI().getPlayerData(uuid);

        // TODO: Channel spying, group spying
        if (playerData.hasSocialSpy()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-socialspy-disabled");
        } else {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-socialspy-enabled");
        }

        playerData.setSocialSpy(!playerData.hasSocialSpy());
        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.spy";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-socialspy-usage");
    }
}
