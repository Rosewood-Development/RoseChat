package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.floralapi.root.command.AbstractCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandStaffChat extends AbstractCommand {

    public CommandStaffChat() {
        super("staffchat", "sc", "ac");
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
        return "rosechat.staffchat";
    }

    @Override
    public String getSyntax() {
        return "staffchat [message]";
    }
}
