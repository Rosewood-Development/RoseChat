package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.command.api.CommandManager;
import org.bukkit.command.CommandSender;

public class ChatCommandManager extends CommandManager {

    public ChatCommandManager(String mainCommandLabel, String mainSyntax) {
        super(mainCommandLabel, mainSyntax);
    }

    @Override
    public void sendHelpMessage(CommandSender sender) {
        this.getLocaleManager().sendMessage(sender, "command-help-title");
        for (AbstractCommand subcommand : getSubcommands()) {
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            this.getLocaleManager().sendCustomMessage(sender, this.getLocaleManager().getLocaleMessage("command-chat-" + subcommand.getLabels().get(0) + "-description"));
        }
    }
}
