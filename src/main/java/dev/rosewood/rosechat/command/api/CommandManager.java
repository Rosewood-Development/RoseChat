package dev.rosewood.rosechat.command.api;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
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
    private RoseChat plugin;

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

    private LocaleManager localeManager;

    /**
     * Creates a new instance of the CommandManager. This also creates a new command.
     * @param mainCommandLabel The main command that the player will use. E.g., '/command'
     * @param mainSyntax The main syntax that the player will see. E.g. '/command <reload|help>'
     */
    public CommandManager(String mainCommandLabel, String mainSyntax) {
        this.plugin = RoseChat.getInstance();
        this.mainCommandLabel = mainCommandLabel;
        this.mainSyntax = mainSyntax;
        this.subcommands = new ArrayList<>();
        this.localeManager = plugin.getManager(LocaleManager.class);
        this.plugin.getCommand(mainCommandLabel).setExecutor(this);
        this.plugin.getCommand(mainCommandLabel).setTabCompleter(this);
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
        if (this.mainCommand != null) {
            if (canSend(sender, this.mainCommand)) this.mainCommand.onCommand(sender, args);
            return true;
        }

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))
                || (args.length == 1 && args[0].equalsIgnoreCase("?"))) {
            sendHelpMessage(sender);
            return true;
        }

        for (AbstractCommand subcommand : this.subcommands) {
            if (!subcommand.getLabels().contains(args[0].toLowerCase())) continue;
            if (canSend(sender, subcommand)) {
                subcommand.onCommand(sender, truncateArgs(args));
                return true;
            }
        }

        this.localeManager.sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.mainSyntax));
        return true;
    }

    public boolean canSend(CommandSender sender, AbstractCommand command) {
        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
            this.localeManager.sendMessage(sender, "no-permission");
            return false;
        }

        if (command.isPlayerOnly() && !(sender instanceof Player)) {
            this.localeManager.sendMessage(sender, "player-only");
            return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String cmd, String[] args) {
        List<String> tab = new ArrayList<>();

        if (this.mainCommand != null) {
            List<String> original = this.mainCommand.onTabComplete(sender, args);
            if (original == null) return tab;
            if (this.mainCommand.getPermission() != null && !sender.hasPermission(this.mainCommand.getPermission())) return tab;
            StringUtil.copyPartialMatches(args[args.length - 1], original, tab);
            return tab;
        }

        if (args.length == 1) {
            List<String> labels = new ArrayList<>();

            for (AbstractCommand subcommand : this.subcommands) {
                if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
                if (!subcommand.isPlayerOnly() && !(sender instanceof Player)) continue;
                labels.add(subcommand.getLabels().get(0));
            }

            StringUtil.copyPartialMatches(args[0], labels, tab);
            return tab;
        }

        List<String> temp;

        for (AbstractCommand subcommand : this.subcommands) {
            if (!subcommand.getLabels().contains(args[0].toLowerCase())) continue;
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            if (!subcommand.isPlayerOnly() && !(sender instanceof Player)) continue;
            temp = subcommand.onTabComplete(sender, truncateArgs(args));

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
    public String[] truncateArgs(String[] args) {
        String[] trueArgs = new String[args.length - 1];
        System.arraycopy(args, 1, trueArgs, 0, args.length - 1);
        return trueArgs;
    }

    /**
     * Sends a default help message to the sender.
     * @param sender The player of console who sent the command.
     */
    public void sendHelpMessage(CommandSender sender) {
        this.localeManager.sendMessage(sender, "command-help-title");
        for (AbstractCommand subcommand : this.subcommands) {
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            this.localeManager.sendCustomMessage(sender, this.localeManager.getLocaleMessage("command-" + subcommand.getLabels().get(0) + "-description"));
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
        return this.subcommands;
    }

    /**
     * Gets the overriden main command.
     * @return The overriden main command.
     */
    public AbstractCommand getMainCommand() {
        return this.mainCommand;
    }

    /**
     * Gets the main command label.
     * @return The main command label.
     */
    public String getMainCommandLabel() {
        return this.mainCommandLabel != null ? this.mainCommandLabel : this.mainCommand.getLabels().get(0);
    }

    /**
     * Gets the main command syntax.
     * @return The main command syntax.
     */
    public String getMainSyntax() {
        return this.mainSyntax;
    }

    public LocaleManager getLocaleManager() {
        return this.localeManager;
    }
}

