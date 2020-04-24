package dev.rosewood.rosechat.floralapi.root.command;

import dev.rosewood.rosechat.floralapi.root.utils.Language;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
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

    /**
     * Creates a new instance of the senior command manager.
     * @param mainCommandLabel The main command label.
     * @param mainSyntax The main command syntax
     */
    public SeniorCommandManager(String mainCommandLabel, String mainSyntax) {
        super(mainCommandLabel, mainSyntax);
        commandManagers = new ArrayList<>();
    }

    @Override
    public void displayHelpMessage(CommandSender sender) {
        sender.sendMessage(Language.PREFIX.getFormatted());

        for (CommandManager manager : commandManagers) {
            AbstractCommand command = manager.getMainCommand();
            if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) continue;
            new LocalizedText(Language.COLOR.getFormatted() +
                    "/" + manager.getMainCommandLabel() + " &7- " +
                    new LocalizedText("command-" + manager.getMainCommandLabel() + "-description").format())
                    .sendMessage(sender);
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
