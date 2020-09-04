package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.floralapi.AbstractCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandRoseChat extends AbstractCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        /*
        Running RoseChat v1.0.0
        Plugin created by: Lilac
        Use /rc help for command information.
         */
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
