package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.DebugManager;
import org.bukkit.command.CommandSender;
import java.util.Arrays;
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
            debugManager.save();
            debugManager.setEnabled(false);
            this.getAPI().getLocaleManager().sendMessage(sender, "command-debug-off");
        } else {
            debugManager.setEnabled(true);
            if (args.length > 0) {
                List<String> argsList = Arrays.asList(args);
                debugManager.setWriteToFile(argsList.contains("-log"));
                debugManager.setTimerEnabled(argsList.contains("-timer"));
                debugManager.setDoOnce(argsList.contains("-doOnce"));
            }

            this.getAPI().getLocaleManager().sendMessage(sender, "command-debug-on");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length != 0) return Arrays.asList("-log", "-timer", "-doOnce");
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
