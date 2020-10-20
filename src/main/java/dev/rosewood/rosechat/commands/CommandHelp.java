package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.floralapi.CommandManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.command.CommandSender;
import java.util.List;

public class CommandHelp extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;

    public CommandHelp(RoseChat plugin) {
        super(false, "help", "?");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        localeManager.sendMessage(sender, "command-help-title");
        for (CommandManager manager : plugin.getCommandManager().getCommandManagers()) {
            if (manager.getMainCommand() == null) {
                String label = manager.getMainCommandLabel();
                localeManager.sendCustomMessage(sender, localeManager.getLocaleMessage("base-command-color") +
                        localeManager.getLocaleMessage("command-" + label + "-description"));
                return;
            }

            AbstractCommand command = manager.getMainCommand();
            String label = command.getLabels().get(0);
            String colour = localeManager.getLocaleMessage(command.isJuniorCommand() ? label + "-command-color" : "base-command-color");
            localeManager.sendCustomMessage(sender, colour + localeManager.getLocaleMessage("command-" + label + "-description"));
        }

        for (AbstractCommand subcommand : plugin.getCommandManager().getSubcommands()) {
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            String colour = localeManager.getLocaleMessage("base-command-color");
            localeManager.sendCustomMessage(sender, colour + localeManager.getLocaleMessage("command-" + subcommand.getLabels().get(0) + "-description"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "null";
    }
}
