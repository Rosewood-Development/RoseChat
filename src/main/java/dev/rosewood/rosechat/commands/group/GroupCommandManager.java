package dev.rosewood.rosechat.commands.group;

import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.floralapi.CommandManager;
import org.bukkit.command.CommandSender;

public class GroupCommandManager extends CommandManager {

    public GroupCommandManager(String mainCommandLabel, String mainSyntax) {
        super(mainCommandLabel, mainSyntax);
    }

    @Override
    public void sendHelpMessage(CommandSender sender) {
        getLocaleManager().sendMessage(sender, "command-help-title");
        for (AbstractCommand subcommand : getSubcommands()) {
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            getLocaleManager().sendCustomMessage(sender, getLocaleManager().getLocaleMessage("command-gc-" + subcommand.getLabels().get(0) + "-description"));
        }
    }
}
