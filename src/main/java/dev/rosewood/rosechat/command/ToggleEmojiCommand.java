package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ToggleEmojiCommand extends AbstractCommand {

    public ToggleEmojiCommand() {
        super(true, "toggleemoji", "toggleemojis");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = this.getAPI().getPlayerData(uuid);

        if (playerData.hasEmojis()) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-toggleemoji-off");
        } else {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-toggleemoji-on");
        }

        playerData.setEmojis(!playerData.hasEmojis());
        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getPermission() {
        return "rosechat.toggleemoji";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-toggleemoji-usage");
    }
}
