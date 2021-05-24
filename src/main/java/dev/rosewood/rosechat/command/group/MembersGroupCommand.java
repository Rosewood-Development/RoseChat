package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import java.util.List;

public class MembersGroupCommand extends AbstractCommand {

    public MembersGroupCommand() {
        super(true, "members");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // list members
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.members";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-members-usage");
    }
}
