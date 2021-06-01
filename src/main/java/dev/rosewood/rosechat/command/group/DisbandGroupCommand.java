package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import java.util.List;

public class DisbandGroupCommand extends AbstractCommand {

    public DisbandGroupCommand() {
        super(true, "disband");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 0) {
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.disband";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-disband-usage");
    }
}