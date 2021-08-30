package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.command.api.CommandManager;
import org.bukkit.command.CommandSender;
import java.util.List;

public class HelpCommand extends AbstractCommand {

    private final RoseChat plugin;

    public HelpCommand(RoseChat plugin) {
        super(false, "help", "?");
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.getAPI().getLocaleManager().sendMessage(sender, "command-help-title");
        for (CommandManager manager : this.plugin.getCommandManager().getCommandManagers()) {
            if (manager.getMainCommandLabel().equalsIgnoreCase("delmsg")) continue;

            if (manager.getMainCommand() == null) {
                String label = manager.getMainCommandLabel();
                this.getAPI().getLocaleManager().sendCustomMessage(sender,
                        this.getAPI().getLocaleManager().getLocaleMessage("command-" + label + "-description"));
            } else {
                if (!sender.hasPermission(manager.getMainCommand().getPermission())) continue;

                AbstractCommand command = manager.getMainCommand();
                String label = command.getLabels().get(0);
                this.getAPI().getLocaleManager().sendCustomMessage(sender, this.getAPI().getLocaleManager().getLocaleMessage("command-" + label + "-description"));
            }
        }

        for (AbstractCommand subcommand : this.plugin.getCommandManager().getSubcommands()) {
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            this.getAPI().getLocaleManager().sendCustomMessage(sender, this.getAPI().getLocaleManager().getLocaleMessage("command-" + subcommand.getLabels().get(0) + "-description"));
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
        return this.getAPI().getLocaleManager().getLocaleMessage("command-help-usage");
    }
}
