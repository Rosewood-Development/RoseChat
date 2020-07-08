package dev.rosewood.rosechat.floralapi.root.command;

import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.utils.Language;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 * Utility class for managing commands and subcommands.
 */
public class CommandManager implements CommandExecutor, TabCompleter {

    /**
     * An instance of the main class.
     */
    private FloralPlugin plugin;

    /**
     * The main command label.
     */
    private String mainCommandLabel;

    /**
     * The main command syntax.
     */
    private String mainSyntax;

    /**
     * The main command. Useful for overriding the main help command.
     */
    private AbstractCommand mainCommand;

    /**
     * A list of subcommands used by this command.
     */
    private List<AbstractCommand> subcommands;

    /**
     * Creates a new instance of the CommandManager. This also creates a new command.
     * @param mainCommandLabel The main command that the player will use. E.g., '/command'
     * @param mainSyntax The main syntax that the player will see. E.g. '/command <reload|help>'
     */
    public CommandManager(String mainCommandLabel, String mainSyntax) {
        this.plugin = FloralPlugin.getInstance();
        this.mainCommandLabel = mainCommandLabel;
        this.mainSyntax = mainSyntax;
        this.subcommands = new ArrayList<>();
        plugin.getCommand(mainCommandLabel).setExecutor(this);
        plugin.getCommand(mainCommandLabel).setTabCompleter(this);
    }

    /**
     * Creates a new instance of the CommandManager. This also creates a new command.
     * Main command label and syntax are taken from the command.
     * @param mainCommand The main command to run.
     */
    public CommandManager(AbstractCommand mainCommand) {
        this(mainCommand.getLabels().get(0), mainCommand.getSyntax());
        this.mainCommand = mainCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (mainCommand != null) {
            if (canSend(sender, mainCommand)) mainCommand.onCommand(sender, args);
            return true;
        }

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))
                || (args.length == 1 && args[0].equalsIgnoreCase("?"))) {
            sendHelpMessage(sender);
            return true;
        }

        for (AbstractCommand subcommand : subcommands) {
            if (!subcommand.getLabels().contains(args[0].toLowerCase())) continue;
            if (canSend(sender, subcommand)) {
                subcommand.onCommand(sender, truncateArgs(args));
                return true;
            }
        }

        Language.INVALID_ARGUMENTS.getLocalizedText().withPrefixPlaceholder()
                .withPlaceholder("syntax", mainSyntax).sendMessage(sender);
        return true;
    }

    private boolean canSend(CommandSender sender, AbstractCommand command) {
        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
            sender.sendMessage(Language.NO_PERMISSION.getFormatted());
            return false;
        }

        if (command.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(Language.PLAYER_ONLY.getFormatted());
            return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String cmd, String[] args) {
        List<String> tab = new ArrayList<>();

        if (mainCommand != null) {
            StringUtil.copyPartialMatches(args[args.length - 1], mainCommand.onTabComplete(sender, args), tab);
            return tab;
        }

        if (args.length == 1) {
            List<String> labels = new ArrayList<>();

            for (AbstractCommand subcommand : subcommands) {
                if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
                if (!subcommand.isPlayerOnly() && !(sender instanceof Player)) continue;
                labels.add(subcommand.getLabels().get(0));
            }

            StringUtil.copyPartialMatches(args[0], labels, tab);
            return tab;
        }

        List<String> temp;

        for (AbstractCommand subcommands : subcommands) {
            if (!subcommands.getLabels().contains(args[0].toLowerCase())) continue;
            if (subcommands.getPermission() != null && !sender.hasPermission(subcommands.getPermission())) continue;
            if (!subcommands.isPlayerOnly() && !(sender instanceof Player)) continue;
            temp = subcommands.onTabComplete(sender, truncateArgs(args));

            if (temp == null) return new ArrayList<>();

            StringUtil.copyPartialMatches(args[args.length - 1], temp, tab);
        }

        return tab;
    }

    /**
     * Decreases the arguments for use in subcommands.
     * @param args The arguments sent with the cards.
     * @return The arguments sent with the command, except the first one.
     */
    private String[] truncateArgs(String[] args) {
        String[] trueArgs = new String[args.length - 1];
        System.arraycopy(args, 1, trueArgs, 0, args.length - 1);
        return trueArgs;
    }

    /**
     * Sends a default help message to the sender.
     * @param sender The player of console who sent the command.
     */
    public void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(new LocalizedText("prefix").format() + new LocalizedText(" &7Plugin created by <g:#C0FFEE:#F768F7>Lilac").format());
        for (AbstractCommand subcommand : subcommands) {
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            String label = subcommand.getLabels().get(0);
            sender.sendMessage(new LocalizedText(
                    new LocalizedText(subcommand.isJuniorCommand() ? mainCommandLabel + "-command-color" : "command-color").format() +
                            subcommand.getSyntax() + " &7- ").format() +
                    new LocalizedText((subcommand.isJuniorCommand() ? mainCommandLabel + "-command-" : "command-") + "" +
                            subcommand.getLabels().get(0) + "-description"
            ).format());
        }
    }

    /**
     * Adds a new subcommand to this command.
     * @param subcommand The subcommand to add.
     * @return An instance of this class.
     */
    public CommandManager addSubcommand(AbstractCommand subcommand) {
        this.subcommands.add(subcommand);
        return this;
    }

    /**
     * Gets a subcommands for this command.
     * @return A list of subcommands for this command.
     */
    public List<AbstractCommand> getSubcommands() {
        return subcommands;
    }

    /**
     * Gets the overriden main command.
     * @return The overriden main command.
     */
    public AbstractCommand getMainCommand() {
        return mainCommand;
    }

    /**
     * Gets the main command label.
     * @return The main command label.
     */
    public String getMainCommandLabel() {
        return mainCommandLabel != null ? mainCommandLabel : mainCommand.getLabels().get(0);
    }

    /**
     * Gets the main command syntax.
     * @return The main command syntax.
     */
    public String getMainSyntax() {
        return mainSyntax;
    }
}

