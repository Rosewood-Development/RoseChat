package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CommandSocialSpy extends AbstractCommand {

    private RoseChat plugin;
    private DataManager dataManager;
    private LocaleManager localeManager;

    public CommandSocialSpy(RoseChat plugin) {
        super(true, "socialspy", "ss", "spy");
        this.plugin = plugin;
        this.dataManager = plugin.getManager(DataManager.class);
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = dataManager.getPlayerData(uuid);

        // TODO: Channel spying, group spying
        if (playerData.hasSocialSpy()) {
            this.localeManager.sendMessage(sender, "command-socialspy-disabled");
        } else {
            this.localeManager.sendMessage(sender, "command-socialspy-enabled");
        }

        playerData.setSocialSpy(!playerData.hasSocialSpy());
        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.spy.message";
    }

    @Override
    public String getSyntax() {
        return localeManager.getLocaleMessage("command-socialspy-usage");
    }
}
