package dev.rosewood.rosechat.floralapi.root.command;

import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.utils.Language;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * A command example, /command reload
 */
public class CommandReload extends AbstractCommand {

    /**
     * Creates a new subcommand with the argument 'reload',
     */
    public CommandReload() {
        super("reload", "rl");
    }

    /**
     * Calls the reload function in the main class and sends a nice message to the sender.
     * @param sender The player or console who executed this command.
     * @param args The arguments executed with this command.
     */
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        FloralPlugin.getInstance().reload();
        sender.sendMessage(Language.RELOADED.getFormatted());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return FloralPlugin.getInstance().getPluginTitle() + ".reload";
    }

    @Override
    public String getSyntax() {
        return "reload";
    }
}
