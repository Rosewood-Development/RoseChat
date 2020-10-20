package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CommandToggleMessages extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;

    public CommandToggleMessages(RoseChat plugin) {
        super(true, "togglemessage", "togglemessages", "togglepm", "togglemsg", "togglemsgs");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        DataManager dataManager = this.plugin.getManager(DataManager.class);
        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = dataManager.getPlayerData(uuid);

        if (playerData.canBeMessaged()) {
            localeManager.sendMessage(sender, "command-togglemessage-on");
        } else {
            localeManager.sendMessage(sender, "command-togglemessage-off");
        }

        playerData.setCanBeMessaged(!playerData.canBeMessaged());
        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.togglemessage";
    }

    @Override
    public String getSyntax() {
        return localeManager.getLocaleMessage("command-togglemessage-usage");
    }
}
