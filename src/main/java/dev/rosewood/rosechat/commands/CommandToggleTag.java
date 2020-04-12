package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.floralapi.root.command.AbstractCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandToggleTag extends AbstractCommand {

    public CommandToggleTag() {
        super("toggletag", "tagtoggle");
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
        return "rosechat.toggletag";
    }

    @Override
    public String getSyntax() {
        return "toggletag";
    }
}
