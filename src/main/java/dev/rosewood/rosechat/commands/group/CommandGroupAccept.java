package dev.rosewood.rosechat.commands.group;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.ChannelManager;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import java.util.List;

public class CommandGroupAccept extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;
    private DataManager dataManager;

    public CommandGroupAccept(RoseChat plugin) {
        super(true, "accept", "join");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
        this.dataManager = plugin.getManager(DataManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.accept";
    }

    @Override
    public String getSyntax() {
        return this.localeManager.getLocaleMessage("command-group-accept");
    }
}
