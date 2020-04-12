package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.floralapi.root.command.AbstractCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandMessage extends AbstractCommand {

    public CommandMessage() {
        super("message", "msg", "whisper", "tell", "m", "pm", "dm");
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
        return "rosechat.message";
    }

    @Override
    public String getSyntax() {
        return "message <player>";
    }
}
