package dev.rosewood.rosechat.floralapi.root.command;

import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

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
     * The main command.
     * Optional. Used for overriding the main help command.
     */
    private AbstractCommand mainCommand;

    /**
     * A list of subcommands used by this command.
     */
    private List<AbstractCommand> subcommands;

    /**
     * Creates a new instance of the CommandManager.
     * This creates a new command.
     * @param mainCommandLabel The main command that the player will use. E.g. '/command'.
     * @param mainSyntax The main syntax that the player will see. E.g. '/command <reload|help>'.
     */
    public CommandManager(String mainCommandLabel, String mainSyntax) {
        this.plugin = FloralPlugin.getInstance();
        this.mainCommandLabel = mainCommandLabel;
        this.subcommands = new ArrayList<>();
        this.mainSyntax = mainSyntax;
        plugin.getCommand(mainCommandLabel).setExecutor(this);
        plugin.getCommand(mainCommandLabel).setTabCompleter(this);
    }

    /**
     * Creates a new instance of the CommandManager.
     * This creates a new command.
     * @param mainCommand The main command to run. This is used instead of the default help command.
     */
    public CommandManager(AbstractCommand mainCommand) {
        this(mainCommand.getLabels().get(0), mainCommand.getSyntax());
        this.mainCommand = mainCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (mainCommand != null) {
            mainCommand.onCommand(sender, args);
            return false;
        }

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))
                || (args.length == 1 && args[0].equalsIgnoreCase("?"))) {
            displayHelpMessage(sender);
            return false;
        }

        for (AbstractCommand subcommand : subcommands) {
            if (!subcommand.getLabels().contains(args[0].toLowerCase())) continue;
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) {
                sender.sendMessage(new LocalizedText("no-permission").withPrefixPlaceholder().format());
                return false;
            }

            if (subcommand.isPlayerOnly() && !(sender instanceof Player)) {
                sender.sendMessage(new LocalizedText("player-only").withPrefixPlaceholder().format());
                return false;
            }

            subcommand.onCommand(sender, truncateArgs(args));
            return false;
        }

        sender.sendMessage(new LocalizedText("invalid-arguments").withPrefixPlaceholder()
                .withPlaceholder("syntax", mainSyntax).format());

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String cmd, String[] args) {
        List<String> tab = new ArrayList<>();

        if (mainCommand != null) {
            StringUtil.copyPartialMatches(args[args.length - 1], mainCommand.onTab(sender, args), tab);

            return tab;
        }

        if (args.length == 1) {
            List<String> labels = new ArrayList<>();

            for (AbstractCommand subcommand : subcommands) {
                if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
                if (!subcommand.isPlayerOnly() && !(sender instanceof Player)) continue;
                labels.add(subcommand.getLabels().get(0));
            }

            StringUtil.copyPartialMatches(args[args.length - 1], labels, tab);

            return tab;
        }

        List<String> temp;
        List<String> players = new ArrayList<>();
        for (AbstractCommand subcommand : subcommands) {
            if (!subcommand.getLabels().contains(args[0].toLowerCase())) continue;
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;;
            if (!subcommand.isPlayerOnly() && !(sender instanceof Player)) continue;
            temp = subcommand.onTab(sender, truncateArgs(args));

            if (temp == null) return new ArrayList<>();

            if (temp.contains("players")) {
                for (String str : temp) {
                    if (!str.equalsIgnoreCase("players")) players.add(str);
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    players.add(player.getName());
                }

                StringUtil.copyPartialMatches(args[args.length - 2], players, tab);

                return tab;
            }

            StringUtil.copyPartialMatches(args[args.length - 2], temp, tab);
            return tab;
        }

        return tab;
    }

    /**
     * Decreases the arguments for use in subcommands.
     * @param args The arguments sent with the command.
     * @return The arguments sent with the command, aside from the first one.
     */
    private String[] truncateArgs(String[] args) {
        String[] trueArgs = new String[args.length - 1];

        for (int i = 0; i < args.length; i++) {
            if (i == 0) continue;
            trueArgs[i - 1] = args[i];
        }

        return trueArgs;
    }

    /**
     * Sends a default help message to the sender.
     * @param sender The player or console who sent the command.
     */
    public void displayHelpMessage(CommandSender sender) {
        sender.sendMessage(new LocalizedText("prefix").format());
        for (AbstractCommand subcommand : subcommands) {
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            sender.sendMessage(new LocalizedText(new LocalizedText("command-color").format() +
                    "/" + mainCommandLabel + " " + subcommand.getSyntax() + " &7- ").format() +
                    new LocalizedText("command-" + subcommand.getLabels().get(0) + "-description").format());
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
     * Gets the subcommands for this command.
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
        if (mainCommand == null) {
            return mainCommandLabel;
        } else {
            return mainCommand.getLabels().get(0);
        }
    }

    /**
     * Gets the main command syntax.
     * @return The main command syntax.
     */
    public String getMainSyntax() {
        return mainSyntax;
    }
}

