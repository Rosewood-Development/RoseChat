package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MuteCommand extends AbstractCommand {

    public MuteCommand() {
        super(false, "mute");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // mute a specific player
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.mute";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-mute-usage");
    }
}
