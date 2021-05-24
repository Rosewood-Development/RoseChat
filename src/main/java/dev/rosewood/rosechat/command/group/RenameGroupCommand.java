package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import java.util.List;

public class RenameGroupCommand extends AbstractCommand {

    public RenameGroupCommand() {
        super(true, "disband");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // rename the gc
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.rename";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-rename-usage");
    }
}
