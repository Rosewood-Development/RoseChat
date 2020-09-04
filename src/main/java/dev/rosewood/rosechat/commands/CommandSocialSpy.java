package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.floralapi.AbstractCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandSocialSpy extends AbstractCommand {

    public CommandSocialSpy() {
        super(true, "socialspy");
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
