package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import java.util.List;

public class InviteGroupCommand extends AbstractCommand {

    public InviteGroupCommand() {
        super(true, "invite");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // invite a player to the gc
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.invite";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-invite-usage");
    }
}
