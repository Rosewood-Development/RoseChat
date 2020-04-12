package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.floralapi.root.command.CommandManager;
import dev.rosewood.rosechat.floralapi.root.utils.Language;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class RosechatCommandManager extends CommandManager {

    private List<CommandManager> commandManagers;

    public RosechatCommandManager(String mainCommandLabel, String mainSyntax) {
        super(mainCommandLabel, mainSyntax);
        commandManagers = new ArrayList<>();
    }

    @Override
    public void displayHelpMessage(CommandSender sender) {
        sender.sendMessage(Language.PREFIX.get());
        for (CommandManager manager : commandManagers) {
            if (manager.getPermission() != null && !sender.hasPermission(manager.getPermission())) continue;
            new LocalizedText(Language.COLOR.get() +
                    "/" + manager.getMainCommandLabel() + " &7- " +
                    new LocalizedText("command-" + manager.getMainCommandLabel() + "-description").format())
                    .sendMessage(sender);
        }
    }

    public RosechatCommandManager addCommandManager(CommandManager manager) {
        commandManagers.add(manager);
        return this;
    }

    public List<CommandManager> getCommandManagers() {
        return commandManagers;
    }
}
