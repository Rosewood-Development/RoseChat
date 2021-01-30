package dev.rosewood.rosechat.floralapi;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.managers.LocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * A command manager that manages other command managers.
 * Used for having multiple different commands.
 * This manager acts as a way to have a help command to show all commands,
 * multiple command managers can be used without a senior command manager,
 * but no help menu will be shown.
 */
public class SeniorCommandManager extends CommandManager {

    /**
     * A list of command managers used by this manager.
     */
    private List<CommandManager> commandManagers;

    private LocaleManager localeManager;

    /**
     * Creates a new instance of the senior command manager.
     * @param mainCommandLabel The main command label.
     * @param mainSyntax The main command syntax
     */
    public SeniorCommandManager(String mainCommandLabel, String mainSyntax) {
        super(mainCommandLabel, mainSyntax);
        this.commandManagers = new ArrayList<>();
        this.localeManager = RoseChat.getInstance().getManager(LocaleManager.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (this.getMainCommand() != null) {
            if (this.canSend(sender, this.getMainCommand())) this.getMainCommand().onCommand(sender, args);
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        for (AbstractCommand subcommand : this.getSubcommands()) {
            if (!subcommand.getLabels().contains(args[0].toLowerCase())) continue;
            if (canSend(sender, subcommand)) {
                subcommand.onCommand(sender, truncateArgs(args));
                return true;
            }
        }

        localeManager.sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getMainSyntax()));
        return true;
    }

    @Override
    public void sendHelpMessage(CommandSender sender) {
        localeManager.sendCustomMessage(sender, "&eRunning <g:#8A2387:#E94057:#F27121>RoseChat &ev" + RoseChat.getInstance().getDescription().getVersion());
        localeManager.sendCustomMessage(sender, "&ePlugin created by: <g:#C0FFEE:#e98df4>Lilac");
        localeManager.sendCustomMessage(sender, "&eUse &b/rc help &efor command information.");
    }

    /**
     * Adds a new command manager to this manager.
     * @param manager The command manager to add.
     * @return An instance of this class.
     */
    public SeniorCommandManager addCommandManager(CommandManager manager) {
        commandManagers.add(manager);
        return this;
    }

    /**
     * Gets all command managers for this manager.
     * @return A list of the command managers.
     */
    public List<CommandManager> getCommandManagers() {
        return commandManagers;
    }
}
