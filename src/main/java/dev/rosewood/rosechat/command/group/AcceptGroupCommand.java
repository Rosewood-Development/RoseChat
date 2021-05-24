package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import java.util.List;

public class AcceptGroupCommand extends AbstractCommand {

    public AcceptGroupCommand() {
        super(true, "accept", "join");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // get last pending invite, join group
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.accept";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-group-accept");
    }
}
