package dev.rosewood.rosechat.floralapi;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.managers.LocaleManager;
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
    public void sendHelpMessage(CommandSender sender) {
        localeManager.sendMessage(sender, "command-help-title");
        for (CommandManager manager : commandManagers) {
            if (manager.getMainCommand() == null) {
                String label = manager.getMainCommandLabel();
                localeManager.sendCustomMessage(sender, localeManager.getLocaleMessage("base-command-color") +
                        localeManager.getLocaleMessage("command-" + label + "-description"));

                return;
            }

            AbstractCommand command = manager.getMainCommand();
            String label = command.getLabels().get(0);
            String colour = localeManager.getLocaleMessage(command.isJuniorCommand() ? label + "-command-color" : "base-command-color");
            localeManager.sendMessage(sender, colour + localeManager.getLocaleMessage("command-" + label + "-description"));
        }

        for (AbstractCommand subcommand : getSubcommands()) {
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            String colour = localeManager.getLocaleMessage("base-command-color");
            localeManager.sendMessage(sender, colour + localeManager.getLocaleMessage("command-" + subcommand.getLabels().get(0) + "-description"));
        }
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
