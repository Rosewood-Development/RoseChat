package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.command.CommandSender;
import java.util.List;

public class ReloadCommand extends AbstractCommand {

    /**
     * Creates a new subcommand with the argument 'reload',
     */
    public ReloadCommand() {
        super("reload", "rl");
    }

    /**
     * Calls the reload function in the main class and sends a nice message to the sender.
     * @param sender The player or console who executed this command.
     * @param args The arguments executed with this command.
     */
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        RoseChat.getInstance().reload();
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-reload-reloaded");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.reload";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-reload-usage");
    }

}
