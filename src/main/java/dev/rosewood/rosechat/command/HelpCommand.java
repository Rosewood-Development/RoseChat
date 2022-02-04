package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.command.api.CommandManager;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpCommand extends AbstractCommand {

    private final RoseChat plugin;
    private final List<String> gcPermissions;
    private final List<String> chatPermissions;

    public HelpCommand(RoseChat plugin) {
        super(false, "help", "?");
        this.plugin = plugin;
        this.gcPermissions = new ArrayList<>(Arrays.asList("create", "disband", "invite", "kick", "accept", "deny", "leave", "members", "rename", "message", "admin"));
        this.chatPermissions = new ArrayList<>(Arrays.asList("mute", "clear", "move", "sudo"));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.getAPI().getLocaleManager().sendMessage(sender, "command-help-title");
        for (CommandManager manager : this.plugin.getCommandManager().getCommandManagers()) {
            if (manager.getMainCommandLabel().equalsIgnoreCase("delmsg")) continue;

            if (manager.getMainCommand() == null) {
                String label = manager.getMainCommandLabel();

                boolean hasPerm = false;
                if (label.equals("gc")) {
                    for (String perm : this.gcPermissions) {
                        if (sender.hasPermission("rosechat.group." + perm)) {
                            hasPerm = true;
                            break;
                        }
                    }
                } else if (label.equals("chat")) {
                    for (String perm : this.chatPermissions) {
                        if (sender.hasPermission("rosechat.admin." + perm)) {
                            hasPerm = true;
                            break;
                        }
                    }
                }

                if (!hasPerm) continue;
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-" + label + "-description");
            } else {
                if (!sender.hasPermission(manager.getMainCommand().getPermission())) continue;

                AbstractCommand command = manager.getMainCommand();
                String label = command.getLabels().get(0);
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-" + label + "-description");
            }
        }

        for (AbstractCommand subcommand : this.plugin.getCommandManager().getSubcommands()) {
            if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) continue;
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-" + subcommand.getLabels().get(0) + "-description");
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
