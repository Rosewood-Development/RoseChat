package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CommandToggleSound extends AbstractCommand {

    private RoseChat plugin;
    private DataManager dataManager;
    private LocaleManager localeManager;
    private List<String> arguments;

    public CommandToggleSound(RoseChat plugin) {
        super(true, "togglesound", "togglesounds", "toggleping", "toggletag");
        this.plugin = plugin;
        this.dataManager = plugin.getManager(DataManager.class);
        this.localeManager = plugin.getManager(LocaleManager.class);
        arguments = Arrays.asList("messages", "tags", "all");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || !arguments.contains(args[0])) {
            localeManager.sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = dataManager.getPlayerData(uuid);

        switch (args[0].toLowerCase()) {
            case "messages":
                sendToggleMessage(sender, args, playerData.hasMessageSounds());
                playerData.setMessageSounds(!playerData.hasMessageSounds());
                break;
            case "tags":
                sendToggleMessage(sender, args, playerData.hasTagSounds());
                playerData.setTagSounds(!playerData.hasTagSounds());
                break;
            case "all":
                sendToggleMessage(sender, args, playerData.hasTagSounds() || playerData.hasMessageSounds());
                playerData.setMessageSounds(!playerData.hasMessageSounds());
                playerData.setTagSounds(!playerData.hasTagSounds());
                break;
        }

        playerData.save();
    }

    private void sendToggleMessage(CommandSender sender, String[] args, boolean nowOn) {
        if (nowOn)
            localeManager.sendMessage(sender, "command-togglesound-on", StringPlaceholders.single("type", args[0].toLowerCase()));
        else
            localeManager.sendMessage(sender, "command-togglesound-off", StringPlaceholders.single("type", args[0].toLowerCase()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) tab.addAll(arguments);

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
