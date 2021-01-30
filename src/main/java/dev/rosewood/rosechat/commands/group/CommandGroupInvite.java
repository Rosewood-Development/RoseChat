package dev.rosewood.rosechat.commands.group;

import dev.rosewood.rosechat.floralapi.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandGroupInvite extends AbstractCommand {

    public CommandGroupInvite() {
        super(true, "invite");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return null;
    }
}
