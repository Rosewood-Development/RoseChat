package dev.rosewood.rosechat.floralapi.root.command;

import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * A subcommand example. E.g. /command reload.
 */
public class CommandReload extends AbstractCommand {

    /**
     * Creates a new subcommand with the argument 'reload'/
     */
    public CommandReload() {
        super("reload", "rl");
    }
    /**
     * Calls the reload function in the main class and sends a nice message to the sender.
     * @param sender The player or console that entered the command.
     * @param args The arguments sent with the command.
     */
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        FloralPlugin.getInstance().reload();
        sender.sendMessage(new LocalizedText("reloaded").withPrefixPlaceholder().format());
    }

    @Override
    public List<String> onTab(CommandSender sender, String[] args) {
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
