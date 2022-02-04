package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class ToggleMessageCommand extends AbstractCommand {

    public ToggleMessageCommand() {
        super(true, "togglemessage", "togglemessages", "togglepm", "togglemsg", "togglemsgs");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = this.getAPI().getPlayerData(uuid);

        if (playerData.canBeMessaged()) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-togglemessage-off");
        } else {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-togglemessage-on");
        }

        playerData.setCanBeMessaged(!playerData.canBeMessaged());
        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.togglemessage";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-togglemessage-usage");
    }
}
