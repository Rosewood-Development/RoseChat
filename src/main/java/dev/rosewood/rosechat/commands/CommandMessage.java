package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageUtils;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class CommandMessage extends AbstractCommand {

    private RoseChat plugin;
    private DataManager dataManager;
    private LocaleManager localeManager;

    public CommandMessage(RoseChat plugin) {
        super(false, "message", "msg", "m", "pm", "whisper", "w", "tell", "t");
        this.plugin = plugin;
        this.dataManager = plugin.getManager(DataManager.class);
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.localeManager.sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        if (args.length == 1) {
            this.localeManager.sendMessage(sender, "command-message-enter-message");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            this.localeManager.sendMessage(sender, "player-not-found");
            return;
        }

        if (sender instanceof ConsoleCommandSender) {

        }

        Player player = (Player) sender;

        PlayerData playerData = this.dataManager.getPlayerData(player.getUniqueId());
        PlayerData targetData = this.dataManager.getPlayerData(target.getUniqueId());
        String message = getAllArgs(1, args);

        if (message.isEmpty()) {
            this.localeManager.sendMessage(sender, "command-message-enter-message");
            return;
        }

        if (!targetData.canBeMessaged()) {
            this.localeManager.sendMessage(sender, "command-togglemessage-cannot-message");
            return;
        }

        MessageUtils.sendPrivateMessage(this.dataManager, playerData, targetData, message);

        playerData.setReplyTo(target.getUniqueId());
        targetData.setReplyTo(player.getUniqueId());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) tab.add(player.getName());
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.message";
    }

    @Override
    public String getSyntax() {
        return localeManager.getLocaleMessage("command-message-usage");
    }
}
