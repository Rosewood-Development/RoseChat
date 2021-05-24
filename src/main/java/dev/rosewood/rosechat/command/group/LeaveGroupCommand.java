package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import java.util.List;

public class LeaveGroupCommand extends AbstractCommand {

    public LeaveGroupCommand() {
        super(true, "leave");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // removes the player from the gc
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.leave";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-leave-usage");
    }
}
