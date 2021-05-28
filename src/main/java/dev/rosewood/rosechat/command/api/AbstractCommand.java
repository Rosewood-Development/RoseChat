package dev.rosewood.rosechat.command.api;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A command to be used within a main command.
 */
public abstract class AbstractCommand {

    /**
     * The labels for the ommand.
     */
    private List<String> labels;

    /**
     * Is the command player only, or can the console use it?
     */
    private boolean playerOnly;

    /**
     * Is this command attached to a senior command?
     */
    private boolean juniorCommand;

    private final RoseChatAPI api;

    /**
     * Creates a new command with the given labels.
     * This command will be able to be used by the console.
     * @param labels The names of the command, e.g /command <label>. Multiple for aliases.
     */
    public AbstractCommand(String... labels) {
        this.api = RoseChatAPI.getInstance();
        this.labels = new ArrayList<>(Arrays.asList(labels));
    }

    /**
     * Creates a new command with the given labels.
     * @param playerOnly Whether or not this command can only be used by a player.
     * @param labels The names of the command, e.g /command <label>. Multiple for aliases.
     */
    public AbstractCommand(boolean playerOnly, String... labels) {
        this(labels);
        this.playerOnly = playerOnly;
    }

    /**
     * Creates a new command with the given labels.
     * @param juniorCommand Whether or not this command is running with a senior command manager.
     * @param playerOnly Whether or not this command can only be used by a player.
     * @param labels The names of the command, e.g. /command <label>. Multiple for aliases.
     */
    public AbstractCommand(boolean juniorCommand, boolean playerOnly, String... labels) {
        this(labels);
        this.juniorCommand = juniorCommand;
        this.playerOnly = playerOnly;
    }

    /**
     * Creates a new command with the given label.
     * Other labels will be taken from the plugin.yml file aliases.
     * @param label The label to use, and also the command in the plugin.yml file for aliases.
     * @param juniorCommand Whether or not this command is running with a senior command manager.
     */
    public AbstractCommand(String label, boolean juniorCommand) {
        this.api = RoseChatAPI.getInstance();
        this.labels = new ArrayList<>();
        this.juniorCommand = juniorCommand;
        this.labels.add(label);

        if (juniorCommand) {
            List<String> aliases = (List<String>) RoseChat.getInstance().getDescription().getCommands().get(label).get("aliases");
            this.labels.addAll(aliases);
        }
    }

    /**
     * Creates a new command, with the given label.
     * Other labels will be taken from the plugin.yml file aliases.
     * @param label The label to use, and also the command in the plugin.yml file for aliases.
     * @param juniorCommand Whether or not this command is running with a senior command manager.
     * @param playerOnly Whether or not this command can only be used by a player.
     */
    public AbstractCommand(String label, boolean juniorCommand, boolean playerOnly) {
        this(label, juniorCommand);
        this.playerOnly = playerOnly;
    }

    /**
     * Called when the command is executed.
     * @param sender The player or console who executed this command.
     * @param args The arguments executed with this command.
     */
    public abstract void onCommand(CommandSender sender, String[] args);

    /**
     * Called when tab completion is executed.
     * @param sender The player or console who tab completed.
     * @param args The arguments executed with this tab completion
     * @return The tab completion results.
     */
    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    /**
     * Gets the permission needed to execute this command.
     * @return The permission needed to execute this command.
     */
    public abstract String getPermission();

    /**
     * Gets the syntax for this command. E.g. '/command give <player> <item>'.
     * @return The syntax for this command.
     */
    public abstract String getSyntax();

    /**
     * Gets all the arguments within this command.
     * @param startArg The argument to start on.
     * @param args The arguments.
     * @return The arguments as a string.
     */
    public String getAllArgs(int startArg, String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = startArg; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }

        return builder.toString().trim();
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public boolean isPlayerOnly() {
        return this.playerOnly;
    }

    public boolean isJuniorCommand() {
        return this.juniorCommand;
    }

    public RoseChatAPI getAPI() {
        return this.api;
    }
}
