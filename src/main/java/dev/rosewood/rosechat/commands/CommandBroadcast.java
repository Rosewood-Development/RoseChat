package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.floralapi.root.command.AbstractCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandBroadcast extends AbstractCommand {

    public CommandBroadcast() {
        super("broadcast", "bc");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

    }

    @Override
    public List<String> onTab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.broadcast";
    }

    @Override
    public String getSyntax() {
        return "broadcast [world]";
    }
}
