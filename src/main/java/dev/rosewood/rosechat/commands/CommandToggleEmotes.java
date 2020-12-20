package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CommandToggleEmotes extends AbstractCommand {

    private RoseChat plugin;
    private DataManager dataManager;
    private LocaleManager localeManager;

    public CommandToggleEmotes(RoseChat plugin) {
        super(true, "toggleemotes", "toggleemote");
        this.plugin = plugin;
        this.dataManager = plugin.getManager(DataManager.class);
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = dataManager.getPlayerData(uuid);

        if (playerData.hasEmotes()) {
            localeManager.sendMessage(sender, "command-toggleemotes-off");
        } else {
            localeManager.sendMessage(sender, "command-toggleemotes-on");
        }

        playerData.setEmotes(!playerData.hasEmotes());
        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getPermission() {
        return "rosechat.toggleemotes";
    }

    @Override
    public String getSyntax() {
        return localeManager.getLocaleMessage("command-toggleemotes-usage");
    }
}
