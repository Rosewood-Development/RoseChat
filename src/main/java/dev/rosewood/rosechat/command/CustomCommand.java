package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CustomCommand extends Command {

    public CustomCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender commandSender, String cmd, String[] args) {
        for (ChatChannel channel : RoseChatAPI.getInstance().getChannels()) {
            if (channel.getCommand() != null && channel.getCommand().equalsIgnoreCase(cmd)) {
                //channel.send();
                Bukkit.broadcastMessage("Sending Message In: " + channel.getId() + " channel");
                return true;
            }
        }

        return false;
    }
}
