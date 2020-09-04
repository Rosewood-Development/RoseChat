package dev.rosewood.rosechat.floralapi;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * A command example, /command reload
 */
public class CommandReload extends AbstractCommand {

    private LocaleManager localeManager;

    /**
     * Creates a new subcommand with the argument 'reload',
     */
    public CommandReload() {
        super("reload", "rl");
        localeManager = RoseChat.getInstance().getManager(LocaleManager.class);
    }

    /**
     * Calls the reload function in the main class and sends a nice message to the sender.
     * @param sender The player or console who executed this command.
     * @param args The arguments executed with this command.
     */
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        RoseChat.getInstance().reload();
        localeManager.sendMessage(sender, "command-reload-reloaded");
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
        return "/rc reload";
    }
}
