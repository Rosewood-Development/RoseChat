package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.floralapi.root.command.AbstractCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandToggleMessage extends AbstractCommand {

    public CommandToggleMessage() {
        super("togglemessage", "togglemessages", "togglemsg", "msgtoggle", "messagetoggle");
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
        return "rosechat.togglemessage";
    }

    @Override
    public String getSyntax() {
        return "togglemessage";
    }
}
