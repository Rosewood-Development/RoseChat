package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.DebugManager;
import org.bukkit.command.CommandSender;
import java.util.List;

public class DebugCommand extends AbstractCommand {

    private final RoseChat plugin;

    public DebugCommand(RoseChat plugin) {
        super(false, "debug");
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        DebugManager debugManager = this.plugin.getManager(DebugManager.class);
        if (debugManager.isEnabled()) {
            debugManager.setEnabled(false);
            debugManager.save();
            this.getAPI().getLocaleManager().sendMessage(sender, "command-debug-off");
        } else {
            debugManager.setEnabled(true);
            this.getAPI().getLocaleManager().sendMessage(sender, "command-debug-on");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.debug";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-debug-usage");
    }

}
