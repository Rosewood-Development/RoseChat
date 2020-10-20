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

public class CommandToggleSound extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;

    public CommandToggleSound(RoseChat plugin) {
        super(true, "togglesound", "togglesounds", "toggleping", "toggletag");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        DataManager dataManager = this.plugin.getManager(DataManager.class);
        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = dataManager.getPlayerData(uuid);

        if (playerData.hasTagSounds()) {
            localeManager.sendMessage(sender, "command-togglesound-on");
        } else {
            localeManager.sendMessage(sender, "command-togglesound-off");
        }

        playerData.setTagSounds(!playerData.hasTagSounds());
        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            tab.addAll(Arrays.asList("message", "tag"));
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.togglesound";
    }

    @Override
    public String getSyntax() {
        return localeManager.getLocaleMessage("command-togglesound-usage");
    }
}
