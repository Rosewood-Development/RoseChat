package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import java.util.List;

public class KickGroupCommand extends AbstractCommand {

    public KickGroupCommand() {
        super(true, "kick");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // kick a player from the gc
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.kick";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-kick-usage");
    }
}
